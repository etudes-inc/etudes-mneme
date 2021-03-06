/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/SubmissionImpl.java $
 * $Id: SubmissionImpl.java 12623 2016-02-13 22:27:51Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentAccess;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentSubmissionStatus;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.Expiration;
import org.etudes.mneme.api.GradingSubmissionStatus;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.ReviewTiming;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionCompletionStatus;
import org.etudes.mneme.api.SubmissionEvaluation;
import org.etudes.mneme.api.SubmissionService;
import org.etudes.util.api.AccessAdvisor;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;

/**
 * SubmissionImpl implements Submission.
 */
public class SubmissionImpl implements Submission
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SubmissionImpl.class);

	/** Dependency (optional, self-injected): AccessAdvisor. */
	protected transient AccessAdvisor accessAdvisor = null;

	protected List<Answer> answers = new ArrayList<Answer>();

	protected SubmissionAssessmentImpl assessment = null;

	protected transient AssessmentService assessmentService = null;

	protected transient AttachmentService attachmentService = null;

	protected transient String bestSubmissionId = null;

	protected SubmissionCompletionStatus completionStatus = SubmissionCompletionStatus.unknown;

	protected SubmissionEvaluationImpl evaluation = null;

	protected String id = null;

	protected Boolean isComplete = Boolean.FALSE;

	protected Boolean released = Boolean.FALSE;

	/** Track changes. */
	protected transient Changeable releasedChanged = new ChangeableImpl();

	protected Date reviewedDate = null;

	protected transient SecurityService securityService = null;

	protected transient SessionManager sessionManager = null;

	protected transient Integer siblingCount = 0;

	protected transient boolean staleEdit = false;

	protected Date startDate = null;

	protected transient SubmissionServiceImpl submissionService = null;

	protected Date submittedDate = null;

	protected Boolean testDrive = Boolean.FALSE;

	/** A value sent to setTotalScore before it is applied. */
	protected transient Float totalScoreToBe = null;

	protected transient boolean totalScoreToBeSet = false;

	protected transient Boolean ungradedSiblings = null;

	protected String userId = null;

	/**
	 * Construct.
	 */
	public SubmissionImpl()
	{
	}

	/**
	 * Construct as a deep copy of another
	 */
	protected SubmissionImpl(SubmissionImpl other)
	{
		set(other);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean completeIfOver()
	{
		if (getIsOver(null, 0))
		{
			Date over = getWhenOver();
			submissionService.autoCompleteSubmission(over, this);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void consolidateTotalScore()
	{
		// check if there was a total score set
		if (!this.totalScoreToBeSet) return;
		this.totalScoreToBeSet = false;

		// for phantoms
		if (this.getIsPhantom())
		{
			// if we have a value to set, phantoms get their evaluation set
			if (this.totalScoreToBe != null)
			{
				this.evaluation.setScore(this.totalScoreToBe);
			}

			return;
		}

		// adjust either the submission evaluation, or the single answer's evaluation if
		// there is a single answer only, and the submission's evaluation has not yet been set
		if (!getEvaluationUsed())
		{
			// we need to have an answer
			if (this.answers.size() > 0)
			{
				Answer answer = answers.get(0);

				// if null, clear the evaluation score
				if (this.totalScoreToBe == null)
				{
					answer.getEvaluation().setScore(null);
				}

				else
				{
					// the final score "to be" will contain the auto-score for the answer (if there is one) - remove it
					float evalScore = this.totalScoreToBe.floatValue();
					Float autoScore = answer.getAutoScore();
					if (autoScore != null)
					{
						evalScore -= autoScore.floatValue();
					}

					// round away bogus decimals
					evalScore = Math.round(evalScore * 100.0f) / 100.0f;

					// Note: setting the final to be 0 will not cause a null answer score to become 0 from null
					// (the grade_asssessment UI shows 0 for final score when there is no auto score and no evaluations set)
					if ((evalScore != 0f) || (answer.getEvaluation().getScore() != null))
					{
						answer.getEvaluation().setScore(evalScore);
					}
				}
			}
		}

		else
		{
			// take a null to mean clear the evaluation adjustment
			if (this.totalScoreToBe == null)
			{
				this.evaluation.setScore(null);
			}

			// compute the new adjustment to achieve this final score
			else
			{
				// the current answer total score
				float curAnswerScore = 0;
				for (Answer answer : answers)
				{
					Float answerScore = answer.getTotalScore();
					if (answerScore != null)
					{
						curAnswerScore += answerScore.floatValue();
					}
				}

				// the current total score, including the answer total and any current evaluation
				float curTotalScore = curAnswerScore;
				if (this.evaluation.getScore() != null)
				{
					curTotalScore += this.evaluation.getScore().floatValue();
				}

				float total = this.totalScoreToBe.floatValue();
				this.totalScoreToBe = null;

				// if the current total is the total we want, we are done
				if (curTotalScore == total) return;

				// adjust to remove the current answer score
				total -= curAnswerScore;

				// set this as the new total score
				this.evaluation.setScore(total);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// two SubmissionImpls are equals if they have the same id
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		if ((this.id == null) || (((SubmissionImpl) obj).id == null)) return false;
		return this.id.equals(((SubmissionImpl) obj).id);
	}

	/**
	 * {@inheritDoc}
	 */
	public Answer getAnswer(Question question)
	{
		if (question == null) return null;

		Answer rv = findAnswer(question.getId());
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Answer getAnswer(String answerId)
	{
		for (Answer answer : this.answers)
		{
			if (answer.getId().equals(answerId))
			{
				return answer;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Answer> getAnswers()
	{
		return this.answers;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getAnswersAutoScore()
	{
		// count the answer auto scores
		float total = 0;
		for (Answer answer : this.answers)
		{
			Float auto = answer.getAutoScore();
			if (auto != null)
			{
				total += auto.floatValue();
			}
		}

		// round away bogus decimals
		total = Math.round(total * 100.0f) / 100.0f;

		return Float.valueOf(total);
	}

	/**
	 * {@inheritDoc}
	 */
	public Assessment getAssessment()
	{
		return this.assessment;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentSubmissionStatus getAssessmentSubmissionStatus()
	{
		Date now = new Date();
		Assessment assessment = getAssessment();

		// if not open yet...
		if ((!assessment.getDates().getHideUntilOpen()) && (assessment.getDates().getOpenDate() != null)
				&& now.before(assessment.getDates().getOpenDate()) && (!assessment.getFrozen()))
		{
			return AssessmentSubmissionStatus.future;
		}

		// if in future and hidden
		if ((assessment.getDates().getHideUntilOpen()) && (assessment.getDates().getOpenDate() != null)
				&& now.before(assessment.getDates().getOpenDate()) && (!assessment.getFrozen()))
		{
			return AssessmentSubmissionStatus.hiddenTillOpen;
		}

		// are we past the hard end date? Or frozen?
		boolean over = ((assessment.getFrozen()) || ((assessment.getDates().getSubmitUntilDate() != null) && (now.after(assessment.getDates()
				.getSubmitUntilDate()))));

		// todo (not over, not started)
		if ((getStartDate() == null) && !over)
		{
			// if overdue but ready
			if (assessment.getDates().getIsLate())
			{
				return AssessmentSubmissionStatus.overdueReady;
			}

			// for FCE and non-provided users, call it closed
			if (getAssessment().getFormalCourseEval())
			{
				try
				{
					String userId = this.sessionManager.getCurrentSessionUserId();
					Site site = siteService().getSite(this.getAssessment().getContext());
					Member m = site.getMember(userId);
					if ((m != null) && (!m.isProvided())) return AssessmentSubmissionStatus.over;
				}
				catch (IdUnusedException e)
				{
				}
			}

			return AssessmentSubmissionStatus.ready;
		}

		// if in progress...
		if ((!getIsComplete()) && (getStartDate() != null))
		{
			// if timed, add an alert
			if (assessment.getTimeLimit() != null)
			{
				return AssessmentSubmissionStatus.inProgressAlert;
			}

			return AssessmentSubmissionStatus.inProgress;
		}

		// completed
		if (getIsComplete())
		{
			// if there are fewer sibs than allowed, add the todo image as well
			if (!over && (getSiblingCount() != null) && ((assessment.getTries() == null) || (getSiblingCount().intValue() < assessment.getTries())))
			{
				// if overdue but ready
				if (assessment.getDates().getIsLate())
				{
					return AssessmentSubmissionStatus.overdueCompleteReady;
				}

				return AssessmentSubmissionStatus.completeReady;
			}

			return AssessmentSubmissionStatus.complete;
		}

		// over, not in progress, never completed
		if (over)
		{
			return AssessmentSubmissionStatus.over;
		}

		return AssessmentSubmissionStatus.other;
	}

	/**
	 * {@inheritDoc}
	 */
	public Submission getBest()
	{
		return ((this.bestSubmissionId == null) || (this.bestSubmissionId == this.id)) ? this : this.submissionService
				.getSubmission(this.bestSubmissionId);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBlockedByDetails()
	{
		// if no advisor, not blocked
		if (this.accessAdvisor == null) return null;

		// check if we have blocked access - we will return this message if blocked (null if not blocked).
		String blockedByTitle = this.accessAdvisor.details("sakai.mneme", getAssessment().getContext(), getAssessment().getId(), getUserId());
		return blockedByTitle;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBlockedByTitle()
	{
		// if no advisor, not blocked
		if (this.accessAdvisor == null) return null;

		// check if we have blocked access - we will return this message if blocked (null if not blocked).
		String blockedByTitle = this.accessAdvisor.message("sakai.mneme", getAssessment().getContext(), getAssessment().getId(), getUserId());
		return blockedByTitle;
	}

	/**
	 * {@inheritDoc}
	 */
	public Reference getCertReference()
	{
		Reference ref = EntityManager.newReference("/mneme/" + AttachmentService.DOWNLOAD + "/" + AttachmentService.ASMT_CERT + "/"
				+ this.getAssessment().getContext() + "/" + this.getId());
		return ref;
	}

	/**
	 * {@inheritDoc}
	 */
	public SubmissionCompletionStatus getCompletionStatus()
	{
		return this.completionStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getElapsedTime()
	{
		if ((submittedDate == null) || (startDate == null)) return null;

		return new Long(submittedDate.getTime() - startDate.getTime());
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getEvaluatedDate()
	{
		Date rv = this.getEvaluation().getAttribution().getDate();
		for (Answer answer : this.answers)
		{
			Date d = answer.getEvaluation().getAttribution().getDate();
			if (d != null)
			{
				if (rv == null)
				{
					rv = d;
				}
				else if (d.after(rv))
				{
					rv = d;
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public SubmissionEvaluation getEvaluation()
	{
		return this.evaluation;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getEvaluationNotReviewed()
	{
		if (!getIsComplete()) return Boolean.FALSE;
		if (getEvaluatedDate() == null) return Boolean.FALSE;
		if (!getStudentMayReview()) return Boolean.FALSE;

		// submissions that are non-eval need to have a comment, and to be released, to allow a review - without this, don't mark it as eval not reviewed
		// was this submission completed by other than the evaluation process for a non-submit?
		if (this.completionStatus == SubmissionCompletionStatus.evaluationNonSubmit)
		{
			// not released
			if (!getIsReleased()) return Boolean.FALSE;

			// find a comment
			boolean foundComment = false;
			if (getEvaluation().getComment() != null)
			{
				foundComment = true;
			}
			else
			{
				for (Answer answer : getAnswers())
				{
					if (answer.getEvaluation().getComment() != null)
					{
						foundComment = true;
						break;
					}
				}
			}

			// if we find no comment
			if (!foundComment) return Boolean.FALSE;
		}

		// not released
		if (!getIsReleased()) return Boolean.FALSE;

		// never reviewed
		if (getReviewedDate() == null) return Boolean.TRUE;

		// needs review if reviewed before the latest evaluation date
		return getReviewedDate().before(getEvaluatedDate());
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getEvaluationUsed()
	{
		// non-single answer or evaluation already in use (or phantom)
		if ((this.answers.size() != 1) || (this.getEvaluation().getDefined()) || this.getIsPhantom()) return Boolean.TRUE;

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEvaluationVersion()
	{
		// start with the overall submission date (could be null)
		Date rv = getEvaluation().getAttribution().getDate();

		// see if any answer evaluation date is later
		for (Answer answer : getAnswers())
		{
			if (answer.getEvaluation().getAttribution().getDate() != null)
			{
				if (rv == null)
				{
					rv = answer.getEvaluation().getAttribution().getDate();
				}
				else if (answer.getEvaluation().getAttribution().getDate().after(rv))
				{
					rv = answer.getEvaluation().getAttribution().getDate();
				}
			}
		}

		if (rv == null) return null;
		return Long.toString(rv.getTime());
	}

	/**
	 * {@inheritDoc}
	 */
	public Expiration getExpiration()
	{
		// TODO: thread caching
		ExpirationImpl rv = new ExpirationImpl();

		Date now = new Date();

		// the end might be from a time limit, or because we are near the closed date
		long endTime = 0;

		// see if the assessment has a hard submission cutoff date (ignore for test drive)

		Date closedDate = null;
		if (!getIsTestDrive())
		{
			closedDate = getAssessment().getDates().getSubmitUntilDate();
		}
		rv.time = closedDate;

		// if we have a time limit, compute the end time based on that limit
		Long limit = getAssessment().getTimeLimit();
		if (limit != null)
		{
			rv.limit = limit;

			// if we have started, compute the end from the start
			long startTime = 0;
			Date startDate = getStartDate();
			if (startDate != null)
			{
				startTime = startDate.getTime();
			}

			// if we have not started, compute the end from now
			else
			{
				startTime = now.getTime();
			}

			// a full time limit duration would end here
			endTime = startTime + limit.longValue();

			// if there's a closed date on the assessment, that falls before that full duration would be, that's the end time
			if ((closedDate != null) && (closedDate.getTime() < endTime))
			{
				endTime = closedDate.getTime();
				rv.cause = Expiration.Cause.closedDate;
			}

			else
			{
				rv.cause = Expiration.Cause.timeLimit;
			}
		}

		// if we are not timed, compute an end time based on the assessment's closed date
		else
		{
			// not timed, no close date, we don't expire
			if (closedDate == null) return null;

			// the closeDate is the end time
			endTime = closedDate.getTime();

			// if this closed date is more than 2 hours from now, ignore it and say we have no expiration
			if (endTime > now.getTime() + (2l * 60l * 60l * 1000l)) return null;

			// set the limit to 2 hours
			rv.limit = 2l * 60l * 60l * 1000l;

			rv.cause = Expiration.Cause.closedDate;
		}

		// how long from now till endTime?
		long tillExpires = endTime - now.getTime();
		if (tillExpires <= 0) tillExpires = 0;

		rv.duration = new Long(tillExpires);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getFirstIncompleteQuestion()
	{
		Assessment assessment = getAssessment();

		for (Part part : assessment.getParts().getParts())
		{
			for (Question question : part.getQuestions())
			{
				if (!getIsCompleteQuestion(question).booleanValue())
				{
					return question;
				}
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Question getFirstQuestion()
	{
		Assessment assessment = getAssessment();
		if (assessment.getParts().getParts().isEmpty()) return null;
		Part part = assessment.getParts().getParts().get(0);
		List<Question> questions = part.getQuestions();
		if (questions.isEmpty()) return null;
		return questions.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public GradingSubmissionStatus getGradingStatus()
	{
		Date now = new Date();
		Assessment assessment = getAssessment();

		// not started yet: future or notStarted or closed
		if (getStartDate() == null)
		{
			// if not open yet...
			if ((assessment.getDates().getOpenDate() != null) && now.before(assessment.getDates().getOpenDate()))
			{
				return GradingSubmissionStatus.future;
			}

			// // if closed for submission
			// if (assessment.getIsClosed())
			// {
			// return GradingSubmissionStatus.closed;
			// }

			return GradingSubmissionStatus.notStarted;
		}

		// if in progress...
		if (!getIsComplete())
		{
			return GradingSubmissionStatus.inProgress;
		}

		// complete: released, evaluated or submitted

		// released?
		if (getIsReleased())
		{
			return GradingSubmissionStatus.released;
		}

		// evaluated?
		if (getEvaluation().getEvaluated())
		{
			return GradingSubmissionStatus.evaluated;
		}

		// submitted
		return GradingSubmissionStatus.submitted;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasUngradedSiblings()
	{
		return this.ungradedSiblings;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasUnscoredAnswers()
	{
		if (!getIsComplete()) return Boolean.FALSE;

		// if marked as evaluated, it is no longer candidate for unscored
		if (getEvaluation().getEvaluated()) return Boolean.FALSE;

		// submissions created in the evaluation process are never considered unscored
		if (getCompletionStatus() == SubmissionCompletionStatus.evaluationNonSubmit) return Boolean.FALSE;

		// if the overall score has been set, none of the answers are considered unscored
		if (getEvaluation().getScore() != null) return Boolean.FALSE;

		for (Answer answer : getAnswers())
		{
			// check answered non-survey that have not been evaluation scored
			if ((answer.getIsAnswered()) && (!answer.getQuestion().getIsSurvey()) && (answer.getEvaluation().getScore() == null))
			{
				// handles unscored subjectives (essays)
				if (answer.getTotalScore() == null)
				{
					return Boolean.TRUE;
				}

				// handles the objectives that have reason, are not released, and have not been commented on
				else if ((!getIsReleased()) && (answer.getReason() != null) && (getEvaluation().getComment() == null)
						&& (answer.getEvaluation().getComment() == null))
				{
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsAnswered()
	{
		Assessment a = getAssessment();

		// for each section / question, make sure we have an answer and not a mark for review
		// only consider questions that are selected to be part of the test for this submission!
		for (Part part : a.getParts().getParts())
		{
			for (Question question : part.getQuestions())
			{
				Answer answer = this.findAnswer(question.getId());
				if (answer == null) return Boolean.FALSE;
				if (answer.getSubmittedDate() == null) return Boolean.FALSE;
				if ((answer.getIsAnswered() == null) || (!answer.getIsAnswered().booleanValue())) return Boolean.FALSE;
				if ((answer.getMarkedForReview() != null && (answer.getMarkedForReview().booleanValue()))) return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsAutoCompleted()
	{
		if (!getIsComplete()) return Boolean.FALSE;

		if (this.completionStatus == SubmissionCompletionStatus.unknown)
		{
			// TODO: try to determine
			return Boolean.FALSE;
		}

		return this.completionStatus == SubmissionCompletionStatus.autoComplete;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsComplete()
	{
		return this.isComplete;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsCompletedLate()
	{
		// must be completed
		if (!getIsComplete()) return Boolean.FALSE;

		// assessment must have accept until and due dates
		if ((getAssessment().getDates().getAcceptUntilDate() == null) || (getAssessment().getDates().getDueDate() == null)) return Boolean.FALSE;

		// if after the due date, we were completed late
		if ((getSubmittedDate() != null) && getSubmittedDate().after(getAssessment().getDates().getDueDate())) return Boolean.TRUE;

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsCompleteQuestion(Question question)
	{
		if (question == null) throw new IllegalArgumentException();

		Answer answer = findAnswer(question.getId());
		if ((answer != null) && (answer.getIsComplete().booleanValue()))
		{
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsNonEvalOrCommented()
	{
		// was this submission completed by other than the evaluation process for a non-submit?
		if (this.completionStatus != SubmissionCompletionStatus.evaluationNonSubmit) return Boolean.TRUE;

		// does this submission have any comments? Consider both at the submission and question level... only if released
		if (getIsReleased())
		{
			if (getEvaluation().getComment() != null) return Boolean.TRUE;

			for (Answer answer : getAnswers())
			{
				if (answer.getEvaluation().getComment() != null)
				{
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsNonSubmit()
	{
		if (!getIsComplete()) return Boolean.FALSE;

		if (this.completionStatus == SubmissionCompletionStatus.unknown)
		{
			// TODO: try to determine
			return Boolean.FALSE;
		}

		return this.completionStatus == SubmissionCompletionStatus.evaluationNonSubmit;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsOver(Date asOf, long grace)
	{
		Date over = getWhenOver();
		if (over == null) return Boolean.FALSE;

		// set the time to now if missing
		if (asOf == null) asOf = new Date();

		// if frozen, it's over
		if (getAssessment().getFrozen()) return Boolean.TRUE;

		// otherwise check the date
		return Boolean.valueOf(asOf.getTime() > over.getTime() + grace);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsPhantom()
	{
		return this.id.startsWith(SubmissionService.PHANTOM_PREFIX);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsReleased()
	{
		return this.released;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsStaleEdit()
	{
		return this.staleEdit;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsStarted()
	{
		return this.startDate != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsTestDrive()
	{
		return this.testDrive;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsUnanswered()
	{
		Assessment a = getAssessment();

		// phantoms are unanswered
		if (getIsPhantom()) return Boolean.TRUE;

		// offlines are never considered unanswered
		if (getAssessment().getType() == AssessmentType.offline) return Boolean.FALSE;

		// real submissions with no answers are unanswered
		if (getAnswers().isEmpty()) return Boolean.TRUE;

		// look for a question that is answered
		for (Part part : a.getParts().getParts())
		{
			for (Question question : part.getQuestions())
			{
				Answer answer = this.findAnswer(question.getId());
				if (answer == null) continue;
				if (answer.getSubmittedDate() == null) continue;
				if (answer.getIsAnswered() == Boolean.TRUE) return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayBegin()
	{
		// ASSSUMPTION: this will be a submission with sibling count, and it will be:
		// - the placeholder, if none other exist
		// - the one in progress, if there is one
		// - the "official" completed one, if any

		// not for offline
		// TODO: if (getAssessment().getType() == AssessmentType.offline) return Boolean.FALSE;

		boolean offline = getAssessment().getType() == AssessmentType.offline;

		// not yet started (skip if an offline)
		if (!offline)
		{
			if (getStartDate() != null) return Boolean.FALSE;
		}

		// published (test drive need not be)
		if (!getIsTestDrive())
		{
			if (!getAssessment().getPublished()) return Boolean.FALSE;
		}

		// valid
		if (!getAssessment().getIsValid()) return Boolean.FALSE;

		// not frozen
		if (!getIsTestDrive())
		{
			if (getAssessment().getFrozen()) return Boolean.FALSE;
		}

		// assessment is open (allow test drive submissions to skip this)
		if (!getIsTestDrive())
		{
			if (!getAssessment().getDates().getIsOpen(Boolean.FALSE)) return Boolean.FALSE;
		}

		// permission - userId must have SUBMIT_PERMISSION in the context of the assessment
		// test drive can instead use manage permission
		if (!getIsTestDrive())
		{
			if (!this.securityService.checkSecurity(this.sessionManager.getCurrentSessionUserId(), MnemeService.SUBMIT_PERMISSION, getAssessment()
					.getContext())) return Boolean.FALSE;
		}
		else
		{
			if (!this.securityService.checkSecurity(this.sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, getAssessment()
					.getContext())) return Boolean.FALSE;
		}

		// under limit (test drive and offline can skip this)
		if ((!getIsTestDrive()) && (!offline))
		{
			if ((getAssessment().getTries() != null) && (this.getSiblingCount() >= getAssessment().getTries())) return Boolean.FALSE;
		}

		// one last test! If not in test drive or offline, and we have an access advisor, see if it wants to block things
		if ((this.accessAdvisor != null) && (!getIsTestDrive()))
		{
			if (this.accessAdvisor.denyAccess("sakai.mneme", getAssessment().getContext(), getAssessment().getId(),
					this.sessionManager.getCurrentSessionUserId()))
			{
				return Boolean.FALSE;
			}
		}

		if (getAssessment().getFormalCourseEval())
		{
			// user must be provided for FCE
			try
			{
				String userId = this.sessionManager.getCurrentSessionUserId();
				Site site = siteService().getSite(this.getAssessment().getContext());
				Member m = site.getMember(userId);
				if (m == null) return Boolean.FALSE;
				if (!m.isProvided()) return Boolean.FALSE;
			}
			catch (IdUnusedException e)
			{
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayBeginAgain()
	{
		return getMayBeginAgain(this.sessionManager.getCurrentSessionUserId());
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayBeginAgain(String userId)
	{
		// ASSSUMPTION: this will be a submission with sibling count, and it will be:
		// - the placeholder, if none other exist
		// - the one in progress, if there is one
		// - the "official" completed one, if any

		// not for offline
		if (getAssessment().getType() == AssessmentType.offline) return Boolean.FALSE;

		// submission is complete
		if (!getIsComplete()) return Boolean.FALSE;

		// published (test drive need not be)
		if (!getIsTestDrive())
		{
			if (!getAssessment().getPublished()) return Boolean.FALSE;
		}

		// valid
		if (!getAssessment().getIsValid()) return Boolean.FALSE;

		// not frozen
		if (!getIsTestDrive())
		{
			if (getAssessment().getFrozen()) return Boolean.FALSE;
		}

		// assessment is open (allow test drive submissions to skip this)
		if (!getIsTestDrive())
		{
			if (!getAssessment().getDates().getIsOpen(Boolean.FALSE)) return Boolean.FALSE;
		}

		// under limit (test drive can skip this)
		if (!getIsTestDrive())
		{
			if ((getAssessment().getTries() != null) && (this.getSiblingCount().intValue() >= getAssessment().getTries().intValue()))
				return Boolean.FALSE;
		}

		// permission - userId must have SUBMIT_PERMISSION in the context of the assessment
		// test drive can instead use manage permission
		if (!getIsTestDrive())
		{
			if (!this.securityService.checkSecurity(userId, MnemeService.SUBMIT_PERMISSION, getAssessment().getContext())) return Boolean.FALSE;
		}
		else
		{
			if (!this.securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, getAssessment().getContext())) return Boolean.FALSE;
		}

		// one last test! If not in test drive, and we have an access advisor, see if it wants to block things
		if ((this.accessAdvisor != null) && (!getIsTestDrive()))
		{
			if (this.accessAdvisor.denyAccess("sakai.mneme", getAssessment().getContext(), getAssessment().getId(), userId))
			{
				return Boolean.FALSE;
			}
		}

		if (getAssessment().getFormalCourseEval())
		{
			// user must be provided for FCE
			try
			{
				Site site = siteService().getSite(this.getAssessment().getContext());
				Member m = site.getMember(userId);
				if (m == null) return Boolean.FALSE;
				if (!m.isProvided()) return Boolean.FALSE;
			}
			catch (IdUnusedException e)
			{
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayContinue()
	{
		// submission has been started
		if (getStartDate() == null) return Boolean.FALSE;

		// submission not complete
		if (getIsComplete()) return Boolean.FALSE;

		// same user
		if (!this.sessionManager.getCurrentSessionUserId().equals(getUserId())) return Boolean.FALSE;

		// published (test drive need not be)
		if (!getIsTestDrive())
		{
			if (!getAssessment().getPublished()) return Boolean.FALSE;
		}

		// valid
		if (!getAssessment().getIsValid()) return Boolean.FALSE;

		// not frozen
		if (!getIsTestDrive())
		{
			if (getAssessment().getFrozen()) return Boolean.FALSE;
		}

		// assessment is open (allow test drive submissions to skip this)
		if (!getIsTestDrive())
		{
			if (!getAssessment().getDates().getIsOpen(Boolean.FALSE)) return Boolean.FALSE;
		}

		// permission - userId must have SUBMIT_PERMISSION in the context of the assessment
		// test drive can instead use manage permission
		if (!getIsTestDrive())
		{
			if (!this.securityService.checkSecurity(this.sessionManager.getCurrentSessionUserId(), MnemeService.SUBMIT_PERMISSION, getAssessment()
					.getContext())) return Boolean.FALSE;
		}
		else
		{
			if (!this.securityService.checkSecurity(this.sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, getAssessment()
					.getContext())) return Boolean.FALSE;
		}

		// one last test! If not in test drive, and we have an access advisor, see if it wants to block things
		if ((this.accessAdvisor != null) && (!getIsTestDrive()))
		{
			if (this.accessAdvisor.denyAccess("sakai.mneme", getAssessment().getContext(), getAssessment().getId(),
					this.sessionManager.getCurrentSessionUserId()))
			{
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayGuestView()
	{
		// ASSSUMPTION: this will be a placeholder submission

		// published
		if (!getAssessment().getPublished()) return Boolean.FALSE;

		// valid
		if (!getAssessment().getIsValid()) return Boolean.FALSE;

		// assessment is open, unless the user has observer permission
		boolean observer = this.securityService.checkSecurity(this.sessionManager.getCurrentSessionUserId(), "site.observer", getAssessment()
				.getContext());
		if ((!observer) && (!getAssessment().getDates().getIsOpen(Boolean.FALSE))) return Boolean.FALSE;

		// permission - userId must have GUEST_PERMISSION in the context of the assessment
		if (!this.securityService.checkSecurity(this.sessionManager.getCurrentSessionUserId(), MnemeService.GUEST_PERMISSION, getAssessment()
				.getContext())) return Boolean.FALSE;

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayReview()
	{
		// same user
		if (!this.sessionManager.getCurrentSessionUserId().equals(getUserId())) return Boolean.FALSE;

		return getStudentMayReview();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayReviewLater()
	{
		// same user
		if (!this.sessionManager.getCurrentSessionUserId().equals(getUserId())) return Boolean.FALSE;

		// submission complete
		if (!getIsComplete().booleanValue()) return Boolean.FALSE;

		// published (test drive need not be)
		if (!getIsTestDrive())
		{
			if (!getAssessment().getPublished()) return Boolean.FALSE;
		}

		// valid
		if (!getAssessment().getIsValid()) return Boolean.FALSE;

		// assessment not set to no review
		if (getAssessment().getReview().getTiming() == ReviewTiming.never) return Boolean.FALSE;

		// or that it is set to date with no date
		// Note: I don't like the redundancy of this code with AssessmentReviewImpl -ggolden
		if ((getAssessment().getReview().getTiming() == ReviewTiming.date) && (getAssessment().getReview().getDate() == null)) return Boolean.FALSE;

		// TODO: permission?

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayViewWork()
	{
		// user must have manage permission
		if (!this.securityService.checkSecurity(this.sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, getAssessment()
				.getContext())) return Boolean.FALSE;

		// submission must be complete
		if (!getIsComplete()) return Boolean.FALSE;

		// assessment must not be formal eval or survey or testdrive
		if (getAssessment().getFormalCourseEval()) return Boolean.FALSE;
		if (getAssessment().getType() == AssessmentType.survey) return Boolean.FALSE;
		if (getIsTestDrive()) return Boolean.FALSE;

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReference()
	{
		return this.submissionService.getSubmissionReference(this.id);
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getReviewedDate()
	{
		return this.reviewedDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getSiblingCount()
	{
		return this.siblingCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssessmentAccess getSpecialAccess()
	{
		return getAssessment().getSpecialAccess().getUserAccess(getUserId());
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getStartDate()
	{
		return this.startDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getSubmittedDate()
	{
		return this.submittedDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getTotalScore()
	{
		// phantoms don't have a total score
		if (getIsPhantom()) return null;

		// add up the scores from the answers
		float total = 0;
		for (Answer answer : answers)
		{
			Float score = answer.getTotalScore();
			if (score != null)
			{
				total += score.floatValue();
			}
		}

		// add in the submission evaluation score if set
		if (this.evaluation.getScore() != null)
		{
			total += this.evaluation.getScore().floatValue();
		}

		// round away bogus decimals
		total = Math.round(total * 100.0f) / 100.0f;

		return Float.valueOf(total);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserId()
	{
		return this.userId;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	public String getUserSection()
	{
		String rv = null;

		try
		{
			Site site = siteService().getSite(this.getAssessment().getContext());

			String titleActive = null;
			String titleInactive = null;
			Collection groups = site.getGroups();
			for (Object groupO : groups)
			{
				Group g = (Group) groupO;

				// skip non-section groups
				if (g.getProperties().getProperty("sections_category") == null) continue;

				// we want to find the user even if not active, so we cannot use g.getUsers(), which only returns active users -ggolden
				// if (g.getUsers().contains(userId))

				// since we are getting all the groups for the site to get the section for this submission, and we are likely to be doing this for all the submissions / users in the site, cache the groups.
				Set<Member> groupMemebers = (Set<Member>) ThreadLocalManager.get("mneme:submission:group:" + g.getId());
				if (groupMemebers == null)
				{
					groupMemebers = g.getMembers();
					ThreadLocalManager.set("mneme:submission:group:" + g.getId(), groupMemebers);
				}

				for (Member gm : groupMemebers)
				{
					if (gm.getUserId().equals(this.userId))
					{
						if (gm.isActive())
						{
							if (titleActive == null) titleActive = g.getTitle();
						}
						else
						{
							if (titleInactive == null) titleInactive = g.getTitle();
						}
					}
				}
			}
			if (titleActive != null)
			{
				rv = titleActive;
			}
			else if (titleInactive != null)
			{
				rv = titleInactive;
			}
		}
		catch (IdUnusedException e)
		{
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getWhenOver()
	{
		// if we have not been started, we are not over
		if (getStartDate() == null) return null;

		// if we are complete, we are not over
		if (getIsComplete()) return null;

		Assessment a = getAssessment();
		Date rv = null;

		// for timed
		if ((a.getTimeLimit() != null) && (a.getTimeLimit() > 0))
		{
			// pick up the end time
			rv = new Date(getStartDate().getTime() + a.getTimeLimit());
		}

		// for hard submit-until date (for test drive, ignore)
		if (!getIsTestDrive())
		{
			if (a.getDates().getSubmitUntilDate() != null)
			{
				if ((rv == null) || (a.getDates().getSubmitUntilDate().before(rv)))
				{
					rv = a.getDates().getSubmitUntilDate();
				}
			}
		}

		// if frozen
		if (a.getFrozen())
		{
			// use now
			rv = new Date();
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		return getId() == null ? "null".hashCode() : getId().hashCode();
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		this.assessment = new SubmissionAssessmentImpl(null, this, this.assessmentService);
		this.evaluation = new SubmissionEvaluationImpl(this, this.attachmentService);

		// check if there is an access advisor - if not, that's ok.
		this.accessAdvisor = (AccessAdvisor) ComponentManager.get(AccessAdvisor.class);
	}

	/**
	 * Dependency: AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Dependency: AttachmentService.
	 * 
	 * @param service
	 *        The AttachmentService.
	 */
	public void setAttachmentService(AttachmentService service)
	{
		this.attachmentService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCompletionStatus(SubmissionCompletionStatus status)
	{
		this.completionStatus = status;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEvaluationVersion(String version)
	{
		// if this date is not the same as the current computed evaluation date, we may be dealing with old data being edited
		String expected = getEvaluationVersion();
		if (Different.different(version, expected))
		{
			this.staleEdit = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIsComplete(Boolean complete)
	{
		if (complete == null) throw new IllegalArgumentException();
		this.isComplete = complete;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIsReleased(Boolean released)
	{
		if (released == null) throw new IllegalArgumentException();
		if (this.released.equals(released)) return;

		this.released = released;

		this.releasedChanged.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setReviewedDate(Date date)
	{
		this.reviewedDate = date;
	}

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		this.sessionManager = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * Dependency: SubmissionService.
	 * 
	 * @param service
	 *        The SubmissionService.
	 */
	public void setSubmissionService(SubmissionServiceImpl service)
	{
		this.submissionService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSubmittedDate(Date submittedDate)
	{
		this.submittedDate = submittedDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTotalScore(Float score)
	{
		// save to process in consolidate
		this.totalScoreToBe = score;
		this.totalScoreToBeSet = true;
	}

	/**
	 * Clear all answer information.
	 */
	protected void clearAnswers()
	{
		this.answers.clear();
	}

	/**
	 * Clear the changed flags.
	 */
	protected void clearIsChanged()
	{
		this.releasedChanged.clearChanged();
		this.evaluation.clearIsChanged();
		if (this.answers != null)
		{
			for (Answer a : this.answers)
			{
				((AnswerImpl) a).clearIsChanged();
			}
		}
	}

	/**
	 * Clear the released changed flags.
	 */
	protected void clearReleasedIsChanged()
	{
		this.releasedChanged.clearChanged();
	}

	/**
	 * Find an existing answer in the submission for this question id.
	 * 
	 * @param questionId
	 *        The question id.
	 * @return The existing answer in the submission for this question id, or null if not found.
	 */
	protected Answer findAnswer(String questionId)
	{
		// find the answer to this assessment question
		for (Answer answer : this.answers)
		{
			if (((AnswerImpl) answer).questionId.equals(questionId))
			{
				return answer;
			}
		}

		return null;
	}

	/**
	 * Access the assessment id.
	 * 
	 * @return The assessment id.
	 */
	protected String getAssessmentId()
	{
		return this.assessment.getId();
	}

	/**
	 * Check if there were any changes.
	 * 
	 * @return TRUE if any changes, FALSE if not.
	 */
	protected Boolean getIsChanged()
	{
		return this.releasedChanged.getChanged() || this.evaluation.getIsChanged();
	}

	/**
	 * Check if there were any changes to the released setting.
	 * 
	 * @return TRUE if any changes to the released settings, FALSE if not.
	 */
	protected Boolean getIsReleasedChanged()
	{
		return this.releasedChanged.getChanged();
	}

	/**
	 * @return getMayReview test, but don't check if the current user is the student.
	 */
	protected Boolean getStudentMayReview()
	{
		// submission complete
		if (!getIsComplete()) return Boolean.FALSE;

		// published (test drive need not be)
		if (!getIsTestDrive())
		{
			if (!getAssessment().getPublished()) return Boolean.FALSE;
		}

		// valid
		if (!getAssessment().getIsValid()) return Boolean.FALSE;

		// assessment review enabled
		if (!getAssessment().getReview().getNowAvailable()) return Boolean.FALSE;

		// TODO: permission?

		return Boolean.TRUE;
	}

	/**
	 * Establish another answer.
	 * 
	 * @param answer
	 *        The answer.
	 */
	protected void initAnswer(AnswerImpl answer)
	{
		answer.initSubmission(this);
		this.answers.add(answer);
	}

	/**
	 * Initialize the assessment id property.
	 * 
	 * @param id
	 *        The assessment id property.
	 */
	protected void initAssessmentId(String id)
	{
		this.assessment.assessmentId = id;
	}

	/**
	 * Initialize the best.
	 */
	protected void initBest(Submission best)
	{
		this.bestSubmissionId = best.getId();
	}

	/**
	 * Initialize the completion status.
	 */
	protected void initCompletionStatus(String statusCode)
	{
		if ((statusCode != null) && (statusCode.length() > 0))
		{
			this.completionStatus = SubmissionCompletionStatus.getFromEncoding(statusCode);
		}
		else
		{
			this.completionStatus = SubmissionCompletionStatus.unknown;
		}
	}

	/**
	 * Initialize the id property.
	 * 
	 * @param id
	 *        The id property.
	 */
	protected void initId(String id)
	{
		this.id = id;
	}

	/**
	 * Initialize the released setting.
	 * 
	 * @param released
	 *        The released setting.
	 */
	protected void initReleased(Boolean released)
	{
		this.released = released;
	}

	/**
	 * Initialize the sibling count.
	 */
	protected void initSiblingCount(Integer count)
	{
		this.siblingCount = count;
	}

	/**
	 * Initialize the test-drive setting.
	 * 
	 * @param testDrive
	 *        The test-drive setting.
	 */
	protected void initTestDrive(Boolean testDrive)
	{
		this.testDrive = testDrive;
	}

	/**
	 * Initialize the ungraded siblings setting.
	 */
	protected void initUngradedSiblings(Boolean unscoredSiblings)
	{
		this.ungradedSiblings = unscoredSiblings;
	}

	/**
	 * Initialize the user id property.
	 * 
	 * @param userId
	 *        The user id property.
	 */
	protected void initUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * Replace an existing answer with this one, or add it if there is no existing one.
	 * 
	 * @param answer
	 *        The answer.
	 */
	protected void replaceAnswer(AnswerImpl answer)
	{
		// preserve the (question) order
		for (Answer current : this.answers)
		{
			if (((AnswerImpl) current).questionId.equals(answer.questionId))
			{
				((AnswerImpl) current).set(answer, this);
				return;
			}
		}

		// add it
		answer.initSubmission(this);
		this.answers.add(answer);
	}

	/**
	 * Set as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(SubmissionImpl other)
	{
		this.answers.clear();
		for (Answer answer : other.answers)
		{
			AnswerImpl a = new AnswerImpl((AnswerImpl) answer, this);
			this.answers.add(a);
		}

		setMain(other);
	}

	/**
	 * Set as a copy of another - main parts only, no answers.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void setMain(SubmissionImpl other)
	{
		this.assessment = new SubmissionAssessmentImpl(other.assessment, this);
		this.assessmentService = other.assessmentService;
		this.attachmentService = other.attachmentService;
		this.bestSubmissionId = other.bestSubmissionId;
		this.completionStatus = other.completionStatus;
		this.evaluation = new SubmissionEvaluationImpl(other.evaluation, this);
		this.released = other.released;
		this.releasedChanged = new ChangeableImpl(other.releasedChanged);
		this.id = other.id;
		this.isComplete = other.isComplete;
		this.reviewedDate = other.reviewedDate;
		this.securityService = other.securityService;
		this.sessionManager = other.sessionManager;
		this.siblingCount = other.siblingCount;
		this.startDate = other.startDate;
		this.submissionService = other.submissionService;
		this.submittedDate = other.submittedDate;
		this.testDrive = other.testDrive;
		this.ungradedSiblings = other.ungradedSiblings;
		this.userId = other.userId;
	}

	/**
	 * @return The SiteService, via the component manager.
	 */
	private SiteService siteService()
	{
		return (SiteService) ComponentManager.get(SiteService.class);
	}
}

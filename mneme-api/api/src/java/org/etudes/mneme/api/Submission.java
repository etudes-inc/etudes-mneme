/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-api/api/src/java/org/etudes/mneme/api/Submission.java $
 * $Id: Submission.java 9770 2015-01-09 03:49:34Z ggolden $
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

package org.etudes.mneme.api;

import java.util.Date;
import java.util.List;

import org.sakaiproject.entity.api.Reference;

/**
 * Submission is a user's response to an assessment. Each time the user takes the assessment, a new submission is made.
 */
public interface Submission
{
	/**
	 * If the submission is "over" (as defined by getIsOver()), close it. Update the submission to reflect its changed status.
	 * 
	 * @return TRUE if it was over and is now closed, FALSE if not.
	 */
	Boolean completeIfOver();

	/**
	 * Process the total score set by setTotalScore.<br />
	 * Call after all answer scores are set, or automatically when saved (SubmissionService.evaluateSubmission).
	 */
	void consolidateTotalScore();

	/**
	 * Find the answer for this question.
	 * 
	 * @param question
	 *        The assessment question for which this will be an answer.
	 * @return The answer for this question, or null if there is none.
	 */
	Answer getAnswer(Question question);

	/**
	 * Find this answer in the submission.
	 * 
	 * @param answerId
	 *        The answer ID.
	 * @return The answer with this id that is part of the submission, or null if not found.
	 */
	Answer getAnswer(String answerId);

	/**
	 * Access the submission's answers in assessment order.
	 * 
	 * @return The submission's answers in order.
	 */
	List<Answer> getAnswers();

	/**
	 * Access the sum of all auto score's for this answers of this submission.
	 * 
	 * @return The auto score for this submission, or 0 if there is none.
	 */
	Float getAnswersAutoScore();

	/**
	 * Access the assessment that this is a submission to.
	 * 
	 * @return The assessment this is a submission to.
	 */
	Assessment getAssessment();

	/**
	 * Get the assessment submission status for a user's submission to an assessment (the submissions's user and assessment)
	 * 
	 * @return The assessment submission status for a user's submission to an assessment.
	 */
	AssessmentSubmissionStatus getAssessmentSubmissionStatus();

	/**
	 * Get the best submission to this same assignment from the same user. May not be known.
	 * 
	 * @return The best submission to this same assignment from the same user, or NULL if not known.
	 */
	Submission getBest();

	/**
	 * @return The detail message of the item that is blocking access to this submission, if any, or null if not blocked.
	 */
	String getBlockedByDetails();

	/**
	 * @return The title of the item that is blocking access to this submission, if any, or null if not blocked.
	 */
	String getBlockedByTitle();

	/**
	 * Get reference for certificate
	 * 
	 * @return Reference object
	 */
	Reference getCertReference();

	/**
	 * @return The submissions's completion status.
	 */
	SubmissionCompletionStatus getCompletionStatus();

	/**
	 * Access the time taken to make this submission, in ms, based on the start and latest submission dates.
	 * 
	 * @return The time taken to make this submission, in ms (or null if it was not timed or not available).
	 */
	Long getElapsedTime();

	/**
	 * @return The date of the latest evaluation in any answer or overall. May return NULL if there have been none.
	 */
	Date getEvaluatedDate();

	/**
	 * Access the evaluation for the overall submission.
	 * 
	 * @return The evaluation for the overall submission, or null if there is none.
	 */
	SubmissionEvaluation getEvaluation();

	/**
	 * @return TRUE if there is an evaluation date, and there was no review, or the review was before the evaluation.
	 */
	Boolean getEvaluationNotReviewed();

	/**
	 * Check if the submission evaluation is used.<br />
	 * This is not so if it has not yet been set and the assessment has only a single question.
	 * 
	 * @return TRUE if the evaluation is used, FALSE if not.
	 */
	Boolean getEvaluationUsed();

	/**
	 * @return a code representing the time stamp of the latest evaluation of the submission or any answer.
	 */
	String getEvaluationVersion();

	/**
	 * Access the expiration information for the submission.
	 * 
	 * @return The expiration information for the submission.
	 */
	Expiration getExpiration();

	/**
	 * Find the first assessment question that has not been marked as complete.
	 * 
	 * @return The first incomplete assessment question, or null if they have all been completed.
	 */
	Question getFirstIncompleteQuestion();

	/**
	 * Find the first assessment question.
	 * 
	 * @return The first assessment question, or null if there is none.
	 */
	Question getFirstQuestion();

	/**
	 * Access the submission's grading status.
	 * 
	 * @return The submissions's grading status.
	 */
	GradingSubmissionStatus getGradingStatus();

	/**
	 * Get if any of the submissions, including this one, to this same assignment from the same user, are not fully graded and released. May not be known.
	 * 
	 * @return The TRUE if any of the submissions, including this one, to this same assignment from the same user, are not fully graded and released, FALSE if not, or NULL if not known.
	 */
	Boolean getHasUngradedSiblings();

	/**
	 * Check if there are any null scores for answered answers for this assessment (only if complete).
	 * 
	 * @return TRUE if there are unscored answers, FALSE if not.
	 */
	Boolean getHasUnscoredAnswers();

	/**
	 * Access the id of this submission.
	 * 
	 * @return The submission's id.
	 */
	String getId();

	/**
	 * Check if all the questions have been answered and not marked for review.
	 * 
	 * @return TRUE if the asssessment's questions are all answered, FALSE if not.
	 */
	Boolean getIsAnswered();

	/**
	 * Check if the submission was automatically completed by Mneme instead of by the user.
	 * 
	 * @return TRUE if the submission was automatically completed, FALSE if is was completed by the user (or not even completed).
	 */
	Boolean getIsAutoCompleted();

	/**
	 * Check if the submission is complete.
	 * 
	 * @return TRUE if the submission is complete, FALSE if still in progress.
	 */
	Boolean getIsComplete();

	/**
	 * Check if the submission is completed after the due date.
	 * 
	 * @return TRUE if the submission is completed after the due date, FALSE if not.
	 */
	Boolean getIsCompletedLate();

	/**
	 * Check if this the answer to this question has been marked "complete" - this is different than "fully answered" as in getIsAnswered().
	 * 
	 * @param question
	 *        The assessment question
	 * @return TRUE if the question has been marked "complete", FALSE if not.
	 */
	Boolean getIsCompleteQuestion(Question question);

	/**
	 * Check if the submission represents a user submission (user finished or auto) or if evaluator has specified comments for the submission
	 * 
	 * @return TRUE if the submission is a non-submit, FALSE if not.
	 */
	Boolean getIsNonEvalOrCommented();

	/**
	 * Check if the submission represents a non-submit, created in grading only to hold evaluation.
	 * 
	 * @return TRUE if the submission is a non-submit, FALSE if not.
	 */
	Boolean getIsNonSubmit();

	/**
	 * Check if the submission is past its time limit, due or accept until date, or is to an assessment that is inactive.
	 * 
	 * @param qasOf
	 *        The effective time of the check.
	 * @param grace
	 *        A grace period (in ms) to extend any hard deadline or timeout.
	 * @return TRUE if the submission is over, FALSE if not.
	 */
	Boolean getIsOver(Date asOf, long grace);

	/**
	 * Check if the submission is a phantom; a place-holder for a user who has not yet submitted.
	 * 
	 * @return TRUE if the submission is phantom, FALSE if not.
	 */
	Boolean getIsPhantom();

	/**
	 * Check if the submission has been released or not.
	 * 
	 * @return TRUE if the submission has been released, FALSE if not.
	 */
	Boolean getIsReleased();

	/**
	 * Check if the submission (evaluation) was edited based on stale data.
	 * 
	 * @return TRUE if the submission (evaluation) was edited based on stale data, FALSE if not.
	 */
	Boolean getIsStaleEdit();

	/**
	 * Check if the submission has been started.
	 * 
	 * @return TRUE if the submission has been started, FALSE if not.
	 */
	Boolean getIsStarted();

	/**
	 * Check if the submission is a test-drive submission.
	 * 
	 * @return TRUE if a test-drive submission, false if not.
	 */
	Boolean getIsTestDrive();

	/**
	 * Check if all the questions have NOT been answered.
	 * 
	 * @return TRUE if the asssessment's questions are all NOT answered, FALSE if not.
	 */
	Boolean getIsUnanswered();

	/**
	 * Check if the submission may be started - the user must have permission, the submission must be a placeholder, the assessment must be open.
	 * 
	 * @return TRUE if the submission may be started, FALSE if not.
	 */
	Boolean getMayBegin();

	/**
	 * Check if the submission may be started for an nth attempt - the user must have permission, the submission must be a complete, the sibling count must be fewer than the assessment's tries limit, and the assessment must be open.
	 * 
	 * @return TRUE if the submission may be started, FALSE if not.
	 */
	Boolean getMayBeginAgain();

	/**
	 * Check if the submission may be started for an nth attempt for the given user - the user must have permission, the submission must be a complete, the sibling count must be fewer than the assessment's tries limit, and the assessment must be open.
	 * 
	 * @param userId
	 *        the id of the user to check.
	 * @return TRUE if the submission may be started, FALSE if not.
	 */
	Boolean getMayBeginAgain(String userId);

	/**
	 * Check if the submission may be re-entered for more work - the user must be the submission user, the submission must be incomplete.
	 * 
	 * @return TRUE if the submission may be re-entered, FALSE if not.
	 */
	Boolean getMayContinue();

	/**
	 * Check if the submission's may be viewed by a guest. The submission is assumed to be a placeholder.
	 * 
	 * @return TRUE if the submission's assessment may be viewed as a guest, FALSE if not.
	 */
	Boolean getMayGuestView();

	/**
	 * Check if the submission may be reviewed - the user must be the submission user, and the submission must be complete and not from a retracted assessment.
	 * 
	 * @return TRUE if the submission may be reviewed, FALSE if not.
	 */
	Boolean getMayReview();

	/**
	 * Check if the submission may be reviewed at some later point - the user must be the submission user, and the submission must be complete and not from a retracted assessment, and the assessment must be set to eventually allow review.
	 * 
	 * @return TRUE if the submission may be reviewed later, FALSE if not.
	 */
	Boolean getMayReviewLater();

	/**
	 * Check if the current user may view the work of the submission (instructor review).
	 * 
	 * @return TRUE if the submission may be viewed, FALSE if not.
	 */
	Boolean getMayViewWork();

	/**
	 * Access the reference of this submission.
	 * 
	 * @return The submission's reference.
	 */
	String getReference();

	/**
	 * Access the last date the submitter reviewed this submission.
	 * 
	 * @return the review date for this submission, or null if there is none.
	 */
	Date getReviewedDate();

	/**
	 * Get the total count of submissions, including this one, to this same assignment from the same user. May not be known.
	 * 
	 * @return The total count of submissions to the assignment by the user, or NULL if not known.
	 */
	Integer getSiblingCount();

	/**
	 * @return The defined special access for the submitter to this assessment, or null if not defined.
	 */
	AssessmentAccess getSpecialAccess();

	/**
	 * Access the start date for this submission.
	 * 
	 * @return the start date for this submission, or null if there is none.
	 */
	Date getStartDate();

	/**
	 * Access the submission date for this submission.
	 * 
	 * @return the submission date for this submission, or null if there is none.
	 */
	Date getSubmittedDate();

	/**
	 * Access the total score of the submission - the total of the auto scores from the answers and the evaluation scores from the answers and overall.<br />
	 * If the submission is "phantom" the total score will be null.
	 * 
	 * @return The total score of the submission, or 0 if there is no score, or null if the submission is phantom.
	 */
	Float getTotalScore();

	/**
	 * Access the user who made this submission.
	 * 
	 * @return The user id of the user who made the submission.
	 */
	String getUserId();

	/**
	 * @return The section name of the submission's user's section, if any (null if not).
	 */
	String getUserSection();

	/**
	 * Compute the 'over' date for the submission - when it would be over based on time limit, due or accept-until dates.
	 * 
	 * @return The 'over' time for the submission, or NULL if there is none.
	 */
	Date getWhenOver();

	/**
	 * Set the submission's completion status.
	 * 
	 * @param status
	 *        The submissions's completion status.
	 */
	void setCompletionStatus(SubmissionCompletionStatus status);

	/**
	 * Set the evaluation version; to compare to the computed evaluation version - if they do not match, then we might be dealing with old data being edited.
	 * 
	 * @param version
	 *        the evaluation version expected for this submission.
	 */
	void setEvaluationVersion(String version);

	/**
	 * Set the complete flag for the submission.
	 * 
	 * @param complete
	 *        True if the submission is complete, False if it is still in progress.
	 */
	void setIsComplete(Boolean complete);

	/**
	 * Set the released flag for the submission.
	 * 
	 * @param released
	 *        True if the submission is released, False if it is not yet.
	 */
	void setIsReleased(Boolean released);

	/**
	 * Set the reviewed date for this submission - the most recent date the submitter reviewed the submission.
	 * 
	 * @param date
	 *        the reviewed date for this submission.
	 */
	void setReviewedDate(Date date);

	/**
	 * Set the start date for this submission - the earliest date that this submission was altered by the submitter.
	 * 
	 * @param startDate
	 *        the submission date for this submission.
	 */
	void setStartDate(Date startDate);

	/**
	 * Set the submission date for this submission - the latest date that this submission was altered by the submitter.
	 * 
	 * @param submittedDate
	 *        the submission date for this submission.
	 */
	void setSubmittedDate(Date submittedDate);

	/**
	 * Set the total score of the submission. The Evaluation will be adjusted so that this becomes the total score given the current answer auto scores and evaluations.
	 * 
	 * @param score
	 *        The new total score desired for the submission.
	 */
	void setTotalScore(Float score);
}

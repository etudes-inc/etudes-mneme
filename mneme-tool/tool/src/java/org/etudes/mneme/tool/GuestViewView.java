/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/GuestViewView.java $
 * $Id: GuestViewView.java 9341 2014-11-24 23:59:36Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
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

package org.etudes.mneme.tool;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /guest_view view for the mneme tool.
 */
public class GuestViewView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(GuestViewView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** tool manager reference. */
	protected ToolManager toolManager = null;

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// we need an aid, then any number of parameters to form the return destination
		if (params.length < 3)
		{
			throw new IllegalArgumentException();
		}

		String assessmentId = params[2];

		String destination = null;
		if (params.length > 3)
		{
			destination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");
		}

		// if not specified, go to the main guest page
		else
		{
			destination = "/guest_view";
		}

		Assessment assessment = this.assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// for offline, guest, submit, or fce permission
		if (assessment.getType() == AssessmentType.offline)
		{
			// security check - guest view, manage, or, fce permission
			if ((!this.assessmentService.allowGuest(this.toolManager.getCurrentPlacement().getContext()))
					&& (!this.assessmentService.allowSetFormalCourseEvaluation(this.toolManager.getCurrentPlacement().getContext()))
					&& (!this.assessmentService.allowSubmit(this.toolManager.getCurrentPlacement().getContext()))
					&& (!this.assessmentService.allowManageAssessments(this.toolManager.getCurrentPlacement().getContext())))
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}
		}
		else
		{
			// security check - guest view, or, FCE permission
			if ((!this.assessmentService.allowGuest(this.toolManager.getCurrentPlacement().getContext()))
					&& (!this.assessmentService.allowSetFormalCourseEvaluation(this.toolManager.getCurrentPlacement().getContext())))
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}
		}

		context.put("assessment", assessment);
		context.put("return", destination);

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// we need an aid, then any number of parameters to form the return destination
		if (params.length < 2)
		{
			throw new IllegalArgumentException();
		}

		// read form
		String destination = this.uiService.decode(req, context);

		// go there!
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the assessment service.
	 * 
	 * @param service
	 *        The assessment service.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Set the tool manager.
	 * 
	 * @param manager
	 *        The tool manager.
	 */
	public void setToolManager(ToolManager manager)
	{
		toolManager = manager;
	}
}

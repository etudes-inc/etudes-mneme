/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/FormatMatchChoiceDelegate.java $
 * $Id: FormatMatchChoiceDelegate.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.etudes.mneme.impl.MatchQuestionImpl.MatchQuestionPair;

/**
 * The "FormatMatchChoice" format delegate for the mneme tool.
 */
public class FormatMatchChoiceDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatMatchChoiceDelegate.class);

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
	public String format(Context context, Object value)
	{
		// value is the choice id
		if (value == null) return null;
		if (!(value instanceof String)) return value.toString();
		String choiceId = (String) value;

		// "question" is the Question
		Object o = context.get("question");
		if (o == null) return value.toString();
		if (!(o instanceof Question)) return value.toString();
		Question question = (Question) o;

		TypeSpecificQuestion tsq = question.getTypeSpecificQuestion();
		if (!(tsq instanceof MatchQuestionImpl)) return value.toString();

		MatchQuestionImpl plugin = (MatchQuestionImpl) tsq;
		List<MatchQuestionPair> pairs = plugin.getPairs();

		if ((plugin.distractor != null) && (plugin.distractor.getChoiceId().equals(choiceId)))
		{
			return plugin.distractor.getChoice();
		}

		for (MatchQuestionPair pair : pairs)
		{
			if (pair.getChoiceId().equals(choiceId))
			{
				return pair.getChoice();
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object formatObject(Context context, Object value)
	{
		return value;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}
}

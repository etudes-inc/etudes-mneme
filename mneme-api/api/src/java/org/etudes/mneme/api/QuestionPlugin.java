/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-api/api/src/java/org/etudes/mneme/api/QuestionPlugin.java $
 * $Id: QuestionPlugin.java 3635 2012-12-02 21:26:23Z ggolden $
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

package org.etudes.mneme.api;

/**
 * QuestionPlugin defines a plugin question type.
 */
public interface QuestionPlugin
{
	/**
	 * Access the question type popularity - 0 for unpopular, 100 for most popular.<br />
	 * Types are presented in popularity order.
	 * 
	 * @return The question type popularity, 0 .. 100
	 */
	Integer getPopularity();

	/**
	 * Access the question type string for this plugin.
	 * 
	 * @return The question type string for this plugin.
	 */
	String getType();

	/**
	 * Access the question type (human readable) string for this plugin.
	 * 
	 * @return The question type (human readable) string for this plugin.
	 */
	String getTypeName();

	/**
	 * Create a new TypeSpecificAnswer.
	 * 
	 * @param answer
	 *        The answer this will be working for.
	 * @return the answer helper.
	 */
	TypeSpecificAnswer newAnswer(Answer answer);

	/**
	 * Create a new TypeSpecificQuestion.
	 * 
	 * @param question
	 *        The question this will be working for.
	 * @return the question helper.
	 */
	TypeSpecificQuestion newQuestion(Question question);
}

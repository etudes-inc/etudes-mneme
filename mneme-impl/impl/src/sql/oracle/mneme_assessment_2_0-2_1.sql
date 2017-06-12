--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/sql/oracle/mneme_assessment_2_0-2_1.sql $
-- $Id: mneme_assessment_2_0-2_1.sql 3635 2012-12-02 21:26:23Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2010, 2011 Etudes, Inc.
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
--*********************************************************************************/

-----------------------------------------------------------------------------
-- Mneme Assessment DDL changes between 2.0 and 2.1
-----------------------------------------------------------------------------

ALTER TABLE MNEME_ASSESSMENT ADD (FORMAL_EVAL CHAR (1), RESULTS_EMAIL VARCHAR2(255), RESULTS_SENT NUMBER);

CREATE INDEX MNEME_ASSESSMENT_IDX_RESULTS ON MNEME_ASSESSMENT
(
	RESULTS_EMAIL	ASC,
	PUBLISHED		ASC,
	RESULTS_SENT	ASC
);

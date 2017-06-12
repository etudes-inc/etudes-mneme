--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/sql/oracle/mneme_submission_2.1.2_2.1.3.sql $
-- $Id: mneme_submission_2.1.2_2.1.3.sql 3669 2012-12-03 20:48:41Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2011, 2012 Etudes, Inc.
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
-- Mneme Submission DDL changes between 2.1.2 and 2.1.3
-----------------------------------------------------------------------------

ALTER TABLE MNEME_SUBMISSION ADD (REVIEWED_DATE NUMBER);

--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/sql/mysql/mneme_assessment_2.1.40.sql $
-- $Id: mneme_assessment_2.1.40.sql 9387 2014-11-30 20:24:08Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2014 Etudes, Inc.
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
-- Mneme Assessment DDL changes for 2.1.40
-----------------------------------------------------------------------------

ALTER TABLE MNEME_ASSESSMENT ADD (POINTS FLOAT, PRESENTATION_ATTACHMENTS LONGTEXT);

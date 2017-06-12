--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/sql/mysql/mneme_question_1_1-1_2.sql $
-- $Id: mneme_question_1_1-1_2.sql 3635 2012-12-02 21:26:23Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2009 Etudes, Inc.
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
-- Mneme Question DDL changes between 1.1 and 1.2
-----------------------------------------------------------------------------

ALTER TABLE MNEME_QUESTION ADD (PRESENTATION_ATTACHMENTS LONGTEXT);

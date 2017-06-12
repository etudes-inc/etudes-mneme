--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/sql/mysql/mneme_question_2.1.42.sql $
-- $Id: mneme_question_2.1.42.sql 9742 2015-01-07 19:56:05Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2015 Etudes, Inc.
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
-- Mneme Question DDL changes for 2.1.42
-----------------------------------------------------------------------------

ALTER TABLE MNEME_QUESTION
ADD INDEX MNEME_QUESTION_IDX_POOL (POOL_ID),
ADD INDEX MNEME_QUESTION_IDX_MINT (MINT),
DROP INDEX MNEME_QUESTION_IDX_MHPSV;

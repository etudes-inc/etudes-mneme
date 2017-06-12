--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/sql/mysql/mneme_assessment_2.1.14_2.1.15.sql $
-- $Id: mneme_assessment_2.1.14_2.1.15.sql 3673 2012-12-03 23:48:35Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2012 Etudes, Inc.
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
-- Mneme Assessment DDL changes between 2.1.14 and 2.1.15
-----------------------------------------------------------------------------

ALTER TABLE MNEME_ASSESSMENT ADD (SHUFFLE_CHOICES CHAR (1));

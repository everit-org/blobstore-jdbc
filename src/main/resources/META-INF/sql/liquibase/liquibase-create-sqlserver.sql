--
-- Copyright (C) 2011 Everit Kft. (http://www.everit.org)
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- Create Database Lock Table
CREATE TABLE [DATABASECHANGELOGLOCK] ([ID] [int] NOT NULL, [LOCKED] [bit] NOT NULL, [LOCKGRANTED] [datetime2](3) NULL, [LOCKEDBY] [nvarchar](255) NULL, CONSTRAINT [PK_DATABASECHANGELOGLOCK] PRIMARY KEY ([ID]))
GO

-- Initialize Database Lock Table
DELETE FROM [DATABASECHANGELOGLOCK]
GO

INSERT INTO [DATABASECHANGELOGLOCK] ([ID], [LOCKED]) VALUES (1, 0)
GO

-- Create Database Change Log Table
CREATE TABLE [DATABASECHANGELOG] ([ID] [nvarchar](255) NOT NULL, [AUTHOR] [nvarchar](255) NOT NULL, [FILENAME] [nvarchar](255) NOT NULL, [DATEEXECUTED] [datetime2](3) NOT NULL, [ORDEREXECUTED] [int] NOT NULL, [EXECTYPE] [nvarchar](10) NOT NULL, [MD5SUM] [nvarchar](35) NULL, [DESCRIPTION] [nvarchar](255) NULL, [COMMENTS] [nvarchar](255) NULL, [TAG] [nvarchar](255) NULL, [LIQUIBASE] [nvarchar](20) NULL, [CONTEXTS] [nvarchar](255) NULL, [LABELS] [nvarchar](255) NULL)
GO

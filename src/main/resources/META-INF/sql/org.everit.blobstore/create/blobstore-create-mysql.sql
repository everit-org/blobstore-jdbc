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

-- Before running this SQL script, you might need to initialize liqiubase tables, too

--  Changeset META-INF/liquibase/org.everit.blobstore.jdbc.changelog.xml::1.0.0::everit
CREATE TABLE `BLOBSTORE_BLOB` (`BLOB_ID` BIGINT AUTO_INCREMENT NOT NULL, `VERSION_` BIGINT NOT NULL, `BLOB_` LONGBLOB NOT NULL, CONSTRAINT `PK_BLOBSTORE_BLOB` PRIMARY KEY (`BLOB_ID`));

INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1.0.0', 'everit', 'META-INF/liquibase/org.everit.blobstore.jdbc.changelog.xml', NOW(), 1, '7:d3bd308ecd2b4b7363ad5a88a910f5b4', 'createTable, createProcedure, sql (x2)', '', 'EXECUTED', NULL, NULL, '3.4.0');

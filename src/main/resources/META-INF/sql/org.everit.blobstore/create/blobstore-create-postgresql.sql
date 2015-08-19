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

-- Changeset META-INF/liquibase/org.everit.blobstore.jdbc.changelog.xml::1.0.0::everit
CREATE TABLE "BLOBSTORE_BLOB" ("BLOB_ID" BIGSERIAL NOT NULL, "VERSION_" BIGINT NOT NULL, "BLOB_" OID NOT NULL, CONSTRAINT "PK_BLOBSTORE_BLOB" PRIMARY KEY ("BLOB_ID"));

CREATE OR REPLACE FUNCTION blobstore_manage() RETURNS trigger AS $BODY$
DECLARE
    blobstore_tmp_row RECORD;
BEGIN
IF (TG_OP = 'UPDATE') THEN
  IF (NEW."BLOB_" != OLD."BLOB_") THEN
    PERFORM lo_unlink(OLD."BLOB_");
  END IF;
  RETURN NEW;
ELSIF (TG_OP = 'DELETE') THEN
  PERFORM lo_unlink(OLD."BLOB_");
  RETURN OLD;
ELSIF (TG_OP = 'TRUNCATE') THEN
  FOR blobstore_tmp_row IN SELECT * FROM "BLOBSTORE_BLOB" LOOP
    PERFORM lo_unlink(blobstore_tmp_row."BLOB_");
  END LOOP;
  DELETE FROM pg_largeobject WHERE loid IN (SELECT "BLOB_" FROM "BLOBSTORE_BLOB");
END IF;
RETURN null;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER blobstore_manage BEFORE UPDATE OR DELETE ON "BLOBSTORE_BLOB"
      FOR EACH ROW EXECUTE PROCEDURE blobstore_manage();

CREATE TRIGGER blobstore_manage_truncate BEFORE TRUNCATE ON "BLOBSTORE_BLOB"
      EXECUTE PROCEDURE blobstore_manage();

INSERT INTO databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1.0.0', 'everit', 'META-INF/liquibase/org.everit.blobstore.jdbc.changelog.xml', NOW(), 1, '7:e082829cb51b1be812ca14af8d1b1f77', 'createTable, createProcedure, sql (x2)', '', 'EXECUTED', NULL, NULL, '3.4.0');

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

CREATE TABLE blobstore_blob (blob_id BIGSERIAL NOT NULL, version_ BIGINT NOT NULL, blob_ OID NOT NULL, CONSTRAINT pk_blobstore_blob PRIMARY KEY (blob_id));

CREATE OR REPLACE FUNCTION blobstore_manage() RETURNS trigger AS $BODY$
DECLARE
    blobstore_tmp_row RECORD;
BEGIN
IF (TG_OP = 'UPDATE') THEN
  IF (NEW.blob_ != OLD.blob_) THEN
    PERFORM lo_unlink(OLD.blob_);
  END IF;
  RETURN NEW;
ELSIF (TG_OP = 'DELETE') THEN
  PERFORM lo_unlink(OLD.blob_);
  RETURN OLD;
ELSIF (TG_OP = 'TRUNCATE') THEN
  FOR blobstore_tmp_row IN SELECT * FROM blobstore_blob LOOP
    PERFORM lo_unlink(blobstore_tmp_row.blob_);
  END LOOP;
  DELETE FROM pg_largeobject WHERE loid IN (SELECT blob_ FROM blobstore_blob);
END IF;
RETURN null;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER blobstore_manage BEFORE UPDATE OR DELETE ON blobstore_blob
      FOR EACH ROW EXECUTE PROCEDURE blobstore_manage();

CREATE TRIGGER blobstore_manage_truncate BEFORE TRUNCATE ON blobstore_blob
      EXECUTE PROCEDURE blobstore_manage();

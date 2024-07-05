ALTER TABLE summary RENAME TO exports;
ALTER TABLE exports ADD COLUMN type character varying(255);
-- ALTER TABLE export ADD COLUMN type character varying(255);
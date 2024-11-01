BEGIN;

UPDATE documents
SET data = jsonb_set(
        data,
        '{sub_title}',
        data -> 'subTitle',
        true
           );

COMMIT;

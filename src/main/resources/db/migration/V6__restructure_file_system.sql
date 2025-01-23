BEGIN;

DELETE FROM public.files;
DELETE FROM public.exports;

ALTER TABLE public.files
    ADD COLUMN owner_id VARCHAR(255);

ALTER TABLE public.files
    ADD COLUMN deployment_id VARCHAR(255);

ALTER TABLE public.files
    ADD COLUMN relative_path VARCHAR(1000);

ALTER TABLE public.files
    RENAME COLUMN storage_name TO file_name;

ALTER TABLE public.exports
    ADD COLUMN relative_path VARCHAR(1000);

COMMIT;

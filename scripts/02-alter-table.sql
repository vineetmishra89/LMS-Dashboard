ALTER TABLE lms_Schema.lms_user_trng_enrollment_mapping
ADD COLUMN completion_review_status VARCHAR(255)
CHECK (completion_review_status IN (null,'PENDING_LND_REVIEW','COMPLETED_LND_REVIEW','REJECTED_LND_REVIEW'));


ALTER TABLE lms_Schema.lms_trng_summary
ADD COLUMN trng_skill_area VARCHAR(255);

update lms_Schema.lms_trng_summary 
set trng_skill_area = category;

commit;
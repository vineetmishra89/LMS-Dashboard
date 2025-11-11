DROP TABLE LMS_SCHEMA.SPLIT_EMAIL;
CREATE TABLE LMS_SCHEMA.SPLIT_EMAIL(EMAIL_ID VARCHAR(4000));
DELETE FROM LMS_SCHEMA.SPLIT_EMAIL;
INSERT INTO LMS_SCHEMA.SPLIT_EMAIL(EMAIL_ID)
VALUES ('nishant.soni@irissoftware.com, abhishek.sharma05@irissoftware.com'),
('sujit.kumar@irissoftware.com, shiv.agarwal@irissoftware.com, sumisha.k@irissoftware.com, aakanksha.pandey@irissoftware.com, ketan.valsangkar@irissoftware.com'),
('ankit.shah01@irissoftware.com, jitendra.khedar@irissoftware.com, ruchira.shukla@irissoftware.com'),
('amit.sharma03@irissoftware.com'),
('astha.jain@irissoftware.com'),
('srinivasan.p@irissoftware.com'),
('pankaj.bhandari@irissoftware.com'),
('nitesh.dubey@irissoftware.com'),
('joginder.kumar@irissoftware.com'),
('sonam.kaushal@irissoftware.com, rahul.malkoti@irissoftware.com'),
('nishant.chauhan@irissoftware.com'),
('nipun.kansal@irissoftware.com, vemala.obireddy@irissoftware.com, ayushi.gupta@irissoftware.com, varun.sharma@irissoftware.com, chanchal.shukla@irissoftware.com, chitragandha.chute@irissoftware.com'),
('nitesh.dubey@irissoftware.com'),
('pankaj.bhandari@irissoftware.com'),
('nipun.batra@irissoftware.com'),
('siddharth.singh@irissoftware.com, vikas.tewari@irissoftware.com'),
('rajeesh.k@irissoftware.com, shivam.gupta@irissoftware.com'),
('madhu.babu@irissoftware.com'),
('nikhil.batra@irissoftware.com'),
('amit.bhagra@irissoftware.com'),
('nipun.batra@irissoftware.com'),
('saurabh.kanoongo@irissoftware.com, ashwin.dev@irissoftware.com, simpy.kumari@irissoftware.com, balakumaran.arumugam@irissoftware.com'),
('sakshi.bansal@irissoftware.com, vikas.jayara@irissoftware.com'),
('prakash.pandey@irissoftware.com'),
('shiv.agarwal@irissoftware.com'),
('sajjan.barla@irissoftware.com, nitin.kumar05@irissoftware.com'),
('ravindra.bhadoriya@irissoftware.com'),
('alok.bansal@irissoftware.com'),
('mahesh.gupta@irissoftware.com'),
('vipin.mittal@irissoftware.com'),
('naresh.sharma@irissoftware.com, abhijeet.srivastava@irissoftware.com'),
('nipun.grover@irissoftware.com, sanjeev.kumar@irissoftware.com, millanpreet.kaur@irissoftware.com'),
('geetha.panneerselvam@irissoftware.com'),
('ankit.shah01@irissoftware.com; jitendra.khedar@irissoftware.com; akanksha.chhabra@irissoftware.com'),
('aakanksha.pandey@irissoftware.com, ashish.mishra01@irissoftware.com'),
('ashish.pal@irissoftware.com');

COMMIT;

INSERT INTO lms_schema.lms_trainer_dtls (TRAINER_NAME,EMAIL_ID,TRAINER_TYPE,CREATED_BY,CREATED_TS,UPDATED_BY,UPDATED_TS)
 SELECT 'Test', TRIM(UNNEST(STRING_TO_ARRAY(EMAIL_ID,','))),
 'INTERNAL', 'SYSTEM',CURRENT_TIMESTAMP,'SYSTEM', CURRENT_TIMESTAMP 
 FROM LMS_SCHEMA.SPLIT_EMAIL;
 COMMIT;

WITH O AS (
	SELECT TRNG_ID,TRIM(UNNEST(STRING_TO_ARRAY(TRAINER_EMAILS,','))) EMAIL_ID
	FROM LMS_SCHEMA.LMS_TRNG_SUMMARY LS
	WHERE TRAINER_EMAILS <> 'NA'
)
INSERT INTO lms_schema.LMS_TRAINER_TRNG_MAPPING
SELECT O.TRNG_ID, LT.TRAINER_ID, LT.EMAIL_ID, LT.TRAINER_NAME
FROM O, LMS_SCHEMA.LMS_TRAINER_DTLS LT
WHERE O.EMAIL_ID = LT.EMAIL_ID;
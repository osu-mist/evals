-- ------------------------------------------------------
--  File created - Friday-August-16-2019   
-- ------------------------------------------------------
-- ------------------------------------------------------
--  DDL for Sequence PASS_SEQ
-- ------------------------------------------------------

-- CALL  CreateSequence('`PASS_SEQ`', 643847, 1)  ORDER  NOCYCLE

-- ------------------------------------------------------
-- DDL for drop tables
-- ------------------------------------------------------

  DROP TABLE IF EXISTS
    ACTIONS,
    ADMINS,
    APPOINTMENT_TYPES,
    APPRAISALS,
    APPRAISAL_STEPS,
    ASSESSMENTS,
    ASSESSMENTS_CRITERIA,
    BUSINESS_CENTERS,
    CLOSEOUT_REASONS,
    CONFIGURATIONS,
    CONFIG_TIMES,
    CRITERIA_AREAS,
    CRITERIA_DETAILS,
    EMAILS,
    EMAIL_TYPES,
    GOALS_LOGS,
    GOALS_VERSIONS,
    NOLIJ_COPIES,
    NOTICES,
    PERMISSION_RULES,
    PYVPASE,
    PYVPASJ,
    PYVPDES,
    PYVPDLW,
    RATINGS,
    REVIEWERS,
    REVIEW_CYCLE_OPTIONS,
    SALARIES,
    `STATUS`;

-- ------------------------------------------------------
--  DDL for Table ACTIONS
-- ------------------------------------------------------

  CREATE TABLE `ACTIONS` (`ACTION` VARCHAR(32) NOT NULL);

   ALTER TABLE `ACTIONS`  COMMENT 'Possible actions on the appraisal records';
-- ------------------------------------------------------
--  DDL for Table ADMINS
-- ------------------------------------------------------

  CREATE TABLE `ADMINS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `EMPLOYEE_PIDM` BIGINT NOT NULL,
    `IS_MASTER` BOOLEAN NOT NULL,
    `CREATE_DATE` DATETIME NOT NULL,
    `CREATOR_PIDM` BIGINT NOT NULL,
    `MODIFIED_DATE` DATETIME DEFAULT NULL);

   ALTER TABLE `ADMINS`  COMMENT 'PASS administrators';
-- ------------------------------------------------------
--  DDL for Table APPOINTMENT_TYPES
-- ------------------------------------------------------

  CREATE TABLE `APPOINTMENT_TYPES` (`NAME` VARCHAR(45) NOT NULL);

   ALTER TABLE `APPOINTMENT_TYPES`  COMMENT 'Possible types of appointments';
-- ------------------------------------------------------
--  DDL for Table APPRAISALS
-- ------------------------------------------------------

  CREATE TABLE `APPRAISALS` (
    `ID` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `STATUS` VARCHAR(45) NOT NULL,
    `START_DATE` DATETIME NOT NULL,
    `END_DATE` DATETIME NOT NULL,
    `JOB_PIDM` BIGINT NOT NULL,
    `POSITION_NUMBER` VARCHAR(8) NOT NULL,
    `JOB_SUFFIX` VARCHAR(2) NOT NULL,
    `CREATE_DATE` DATETIME NOT NULL,
    `RESULT_SUBMIT_DATE` DATETIME,
    `EVALUATOR_PIDM` BIGINT COMMENT 'pidm of the employee who performed the evaluation.',
    `EVALUATION` LONGTEXT,
    `RATING` TINYINT DEFAULT 4,
    `REVIEWER_PIDM` BIGINT COMMENT 'pidm of the employee that performed the review',
    `REVIEW_SUBMIT_DATE` DATETIME,
    `REVIEW` VARCHAR(4000),
    `REBUTTAL` LONGTEXT,
    `EMPLOYEE_SIGNED_DATE` DATETIME COMMENT 'Reason for closing out.',
    `REBUTTAL_DATE` DATETIME,
    `CLOSEOUT_DATE` DATETIME COMMENT 'This is closing out without finishing the review.',
    `CLOSEOUT_PIDM` BIGINT COMMENT 'The pidm of the employee who performed the closeout.',
    `TYPE` VARCHAR(16) COMMENT 'possible values are: - trial - annual - special' NOT NULL,
    `ORIGINAL_STATUS` VARCHAR(32) COMMENT 'This is only used for the closeAppraisal and reopenAppraisal action.CloseApraisal sets it to the original status before closing. ReopenStatus sets the status to the value of this column.',
    `RELEASE_DATE` DATETIME COMMENT 'The date when supervisor releases the appraisal for employee to sign',
    `SUPERVISOR_REBUTTAL_READ` DATETIME, `EVALUATION_SUBMIT_DATE` DATETIME, `GRANT_DRAFT_RESULTS_PERMISSION` INT,
    `CLOSEOUT_REASON_ID` BIGINT, `OVERDUE` SMALLINT, `GOALS_OVERDUE` SMALLINT, `GOALS_APPROVAL_OVERDUE` SMALLINT,
    `RESULTS_OVERDUE` SMALLINT, `APPRAISAL_OVERDUE` SMALLINT, `REVIEW_OVERDUE` SMALLINT, `RELEASE_OVERDUE` SMALLINT,
    `SIGNATURE_OVERDUE` SMALLINT,
    `REBUTTAL_READ_OVERDUE` SMALLINT);
    
    /* BLOB (`EVALUATION`) STORE AS BASICFILE `EVALUATION_STORAGE`(ENABLE STORAGE IN ROW CHUNK 8192 RETENTION  NOCACHE LOGGING )
    BLOB (`REBUTTAL`) STORE AS BASICFILE `REBUTTAL_STORAGE`(ENABLE STORAGE IN ROW CHUNK 8192 RETENTION  NOCACHE LOGGING ) */

   ALTER TABLE `APPRAISALS`  COMMENT 'Performance appraisal records';
-- ------------------------------------------------------
--  DDL for Table APPRAISAL_STEPS
-- ------------------------------------------------------

  CREATE TABLE `APPRAISAL_STEPS` (
    `ID` BIGINT NOT NULL,
    `ACTION` VARCHAR(32) NOT NULL,
    `APPOINTMENT_TYPE` VARCHAR(45) NOT NULL,
    `NEW_STATUS` VARCHAR(32) NOT NULL,
    `EMAIL_TYPE` VARCHAR(64)) ;

   ALTER TABLE `APPRAISAL_STEPS`  COMMENT 'Consequences of actions taken on the appraisals';
-- ------------------------------------------------------
--  DDL for Table ASSESSMENTS
-- ------------------------------------------------------

  CREATE TABLE `ASSESSMENTS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `GOAL` LONGTEXT,
    `EMPLOYEE_RESULT` LONGTEXT,
    `SUPERVISOR_RESULT` LONGTEXT COMMENT 'supervisor''s result box',
    `CREATE_DATE` DATETIME NOT NULL,
    `MODIFIED_DATE` DATETIME DEFAULT NULL,
    `GOAL_VERSION_ID` BIGINT NOT NULL,
    `SEQUENCE` BIGINT NOT NULL, `DELETER_PIDM` BIGINT, `DELETE_DATE` DATETIME);
  
  -- RETENTION  NOCACHE LOGGING )  LOB (`EMPLOYEE_RESULT`) STORE AS BASICFILE "EMPLOYEE_RESULT_STORAGE"(ENABLE STORAGE IN ROW CHUNK 8192 RETENTION  NOCACHE LOGGING )  LOB (`SUPERVISOR_RESULT`) STORE AS BASICFILE "SUPERVISOR_RESULT_STORAGE"(ENABLE STORAGE IN ROW CHUNK 8192 RETENTION  NOCACHE LOGGING ) 

   ALTER TABLE `ASSESSMENTS`  COMMENT 'Goals and results of appraisal records';
-- ------------------------------------------------------
--  DDL for Table ASSESSMENTS_CRITERIA
-- ------------------------------------------------------

  CREATE TABLE `ASSESSMENTS_CRITERIA` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `ASSESSMENT_ID` BIGINT NOT NULL,
    `CRITERIA_AREA_ID` BIGINT NOT NULL,
    `CHECKED` TINYINT);
-- ------------------------------------------------------
--  DDL for Table BUSINESS_CENTERS
-- ------------------------------------------------------

  CREATE TABLE `BUSINESS_CENTERS` (`NAME` VARCHAR(255) NOT NULL) ;

   ALTER TABLE `BUSINESS_CENTERS`  COMMENT 'OSU business centers';
-- ------------------------------------------------------
--  DDL for Table CLOSEOUT_REASONS
-- ------------------------------------------------------

  CREATE TABLE `CLOSEOUT_REASONS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `REASON` VARCHAR(255) NOT NULL,
    `CREATE_DATE` DATETIME NOT NULL,
    `CREATOR_PIDM` BIGINT NOT NULL,
    `DELETE_DATE` DATETIME) ;

   ALTER TABLE `CLOSEOUT_REASONS`  COMMENT 'Reason for closeout';
-- ------------------------------------------------------
--  DDL for Table CONFIGURATIONS
-- ------------------------------------------------------

  CREATE TABLE `CONFIGURATIONS` (`ID` BIGINT NOT NULL,
  `SECTION` VARCHAR(32),
  `NAME` VARCHAR(255) NOT NULL,
  `SEQUENCE` INT,
  `VALUE` VARCHAR(45) NOT NULL,
  `REFERENCE_POINT` VARCHAR(64),
  `ACTION` VARCHAR(32), `APPOINTMENT_TYPE` VARCHAR(45)) ;

   ALTER TABLE `CONFIGURATIONS`  COMMENT 'PASS configuration parameters';
-- ------------------------------------------------------
--  DDL for Table CONFIG_TIMES
-- ------------------------------------------------------

  CREATE TABLE `CONFIG_TIMES` (`CONTEXT_DATETIME` DATETIME NOT NULL) ;

   ALTER TABLE `CONFIG_TIMES`  COMMENT 'Stores datetime servers are refreshed';
-- ------------------------------------------------------
--  DDL for Table CRITERIA_AREAS
-- ------------------------------------------------------

  CREATE TABLE `CRITERIA_AREAS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `NAME` VARCHAR(255) NOT NULL,
    `APPOINTMENT_TYPE` VARCHAR(45) NOT NULL,
    `ANCESTOR_ID` BIGINT,
    `CREATE_DATE` DATETIME NOT NULL,
    `CREATOR_PIDM` BIGINT NOT NULL,
    `DELETE_DATE` DATETIME,
    `DELETER_PIDM` BIGINT,
    `DESCRIPTION` VARCHAR(4000) NOT NULL) ;

   ALTER TABLE `CRITERIA_AREAS`  COMMENT 'Names of evaluation criteria';
-- ------------------------------------------------------
--  DDL for Table CRITERIA_DETAILS
-- ------------------------------------------------------

  CREATE TABLE `CRITERIA_DETAILS` (
    `ID` BIGINT NOT NULL,
    `AREA_ID` BIGINT NOT NULL,
    `DESCRIPTION` VARCHAR(4000) NOT NULL,
    `CREATE_DATE` DATETIME NOT NULL,
    `CREATOR_PIDM` BIGINT NOT NULL) ;

   ALTER TABLE `CRITERIA_DETAILS`  COMMENT 'Description of evaluation criteria';
-- ------------------------------------------------------
--  DDL for Table EMAILS
-- ------------------------------------------------------

  CREATE TABLE `EMAILS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `APPRAISAL_ID` BIGINT NOT NULL,
    `EMAIL_TYPE` VARCHAR(64) NOT NULL,
    `SENT_TIME` DATETIME NOT NULL) ;

   ALTER TABLE `EMAILS`  COMMENT 'Records of emails sent by PASS system';
-- ------------------------------------------------------
--  DDL for Table EMAIL_TYPES
-- ------------------------------------------------------

  CREATE TABLE `EMAIL_TYPES` (
    `TYPE` VARCHAR(64) NOT NULL COMMENT 'goalsDue goalsOverDue...These are keys to an email subjects and bodies in a resource bundle file.',
    `MAILTO` VARCHAR(64) NOT NULL COMMENT 'emloyee supervisor upper supervisor reviewer employee, supervisor',
    `CC` VARCHAR(64), `BCC` VARCHAR(64)) ;

   ALTER TABLE `EMAIL_TYPES`  COMMENT 'Email types (goals due, results due, etc)';
-- ------------------------------------------------------
--  DDL for Table GOALS_LOGS
-- ------------------------------------------------------

  CREATE TABLE `GOALS_LOGS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `ASSESSMENT_ID` BIGINT NOT NULL,
    `CONTENT` LONGTEXT NOT NULL,
    `AUTHOR_PIDM` BIGINT NOT NULL,
    `CREATE_DATE` DATETIME NOT NULL,
    `TYPE` VARCHAR(45) COMMENT 'default to null, which will be for normal goals. the value is new for new goals, used for goalsReactivated.');
  
  -- RETENTION  NOCACHE LOGGING ) 

   ALTER TABLE `GOALS_LOGS`  COMMENT 'Goals change history';
-- ------------------------------------------------------
--  DDL for Table GOALS_VERSIONS
-- ------------------------------------------------------

  CREATE TABLE `GOALS_VERSIONS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `APPRAISAL_ID` BIGINT NOT NULL,
    `CREATE_DATE` DATETIME, `GOALS_APPROVED_DATE` DATETIME, `GOALS_APPROVER_PIDM` BIGINT, `REQUEST_DECISION` TINYINT, `REQUEST_DECISION_PIDM` BIGINT, `TIMED_OUT_AT` VARCHAR(255), `REQUEST_DECISION_DATE` DATETIME, `GOALS_SUBMIT_DATE` DATETIME, `GOALS_COMMENTS` VARCHAR(4000), `GOALS_REQUIRED_MOD_DATE` DATETIME);
-- ------------------------------------------------------
--  DDL for Table NOLIJ_COPIES
-- ------------------------------------------------------

  CREATE TABLE `NOLIJ_COPIES` (
    `ID` BIGINT NOT NULL,
    `APPRAISAL_ID` BIGINT NOT NULL,
    `SUBMIT_DATE` DATETIME NOT NULL,
    `FILE_NAME` VARCHAR(255) NOT NULL COMMENT 'Name of he file sent to Nolij.') ;

   ALTER TABLE `NOLIJ_COPIES`  COMMENT 'Records of appraisals to Nolij';
-- ------------------------------------------------------
--  DDL for Table NOTICES
-- ------------------------------------------------------

  CREATE TABLE `NOTICES` (`ID` DECIMAL(38,0), `ANCESTOR_ID` DECIMAL(38,0), `NAME` VARCHAR(255), `TEXT` VARCHAR(255), `CREATOR_PIDM` DECIMAL(38,0), `CREATE_DATE` DATETIME (6));
-- ------------------------------------------------------
--  DDL for Table PERMISSION_RULES
-- ------------------------------------------------------

  CREATE TABLE `PERMISSION_RULES` (
    `ID` BIGINT NOT NULL,
    `STATUS` VARCHAR(32) NOT NULL,
    `ROLE` VARCHAR(45) NOT NULL COMMENT 'employee supervisor upper supervisor reviewer',
    `APPROVED_GOALS` VARCHAR(1) COMMENT 'possible valeus: e: edit v: view null: no permission', `UNAPPROVED_GOALS` VARCHAR(1),
    `GOAL_COMMENTS` VARCHAR(1), `RESULTS` VARCHAR(1), `SUPERVISOR_RESULTS` VARCHAR(1) COMMENT 'e for edit v for view or empty for no access.',
    `EVALUATION` VARCHAR(1), `REVIEW` VARCHAR(1), `EMPLOYEE_RESPONSE` VARCHAR(1), `REBUTTAL_READ` VARCHAR(1),
    `SECONDARY_SUBMIT` VARCHAR(32) COMMENT 'if null, then then no require modification  button value for this column is the resource bundle key.  The value of the button is from resource bundle.',
    `SUBMIT` VARCHAR(32) COMMENT 'if null, then then no submit button value for this column is the resource bundle key.  The value of the button is from resource bundle.',
    `ACTION_REQUIRED` VARCHAR(45) DEFAULT 4 COMMENT 'if null, then then no action required. value for this column is the resource bundle key.  The value of the button is from resource bundle.',
    `DOWNLOAD_PDF` VARCHAR(1), `CLOSEOUT` VARCHAR(1), `SEND_TO_NOLIJ` VARCHAR(1), `SET_STATUS_TO_RESULTS_DUE` VARCHAR(1),
    `REACTIVATE_GOALS` VARCHAR(1), `APPOINTMENT_TYPE` VARCHAR(45)) ;

   ALTER TABLE `PERMISSION_RULES`  COMMENT 'Permissions to fields on the appraisal records based on';
-- ------------------------------------------------------
--  DDL for Table PYVPASE
-- ------------------------------------------------------

  CREATE TABLE `PYVPASE` (
    `PYVPASE_PIDM` INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Pidm',
    `PYVPASE_ID` VARCHAR(9) NOT NULL COMMENT 'OSU ID',
    `PYVPASE_LAST_NAME` VARCHAR(60) NOT NULL COMMENT 'Last Name',
    `PYVPASE_FIRST_NAME` VARCHAR(60) COMMENT 'First Name',
    `PYVPASE_MI` VARCHAR(60) COMMENT 'Middle Initial',
    `PYVPASE_ONID_LOGIN` VARCHAR(20) COMMENT 'ONID Login ID',
    `PYVPASE_EMAIL` VARCHAR(4000) COMMENT 'Preferred Email address',
    `PYVPASE_EMPL_STATUS` VARCHAR(1) NOT NULL COMMENT 'Employee Status from PEBEMPL') ;

   ALTER TABLE `PYVPASE`  COMMENT 'OSU PASS System -- Employee View';
-- ------------------------------------------------------
--  DDL for Table PYVPASJ
-- ------------------------------------------------------

  CREATE TABLE `PYVPASJ` (
    `PYVPASJ_PIDM` INT NOT NULL COMMENT 'Pidm',
    `PYVPASJ_POSN` VARCHAR(6) NOT NULL,
    `PYVPASJ_SUFF` VARCHAR(2) NOT NULL,
    `PYVPASJ_STATUS` VARCHAR(1) NOT NULL,
    `PYVPASJ_DESC` VARCHAR(30),
    `PYVPASJ_ECLS_CODE` VARCHAR(20) NOT NULL,
    `PYVPASJ_APPOINTMENT_TYPE` VARCHAR(4000),
    `PYVPASJ_BEGIN_DATE` DATETIME NOT NULL,
    `PYVPASJ_END_DATE` DATETIME,
    `PYVPASJ_PCLS_CODE` VARCHAR(5) NOT NULL,
    `PYVPASJ_SAL_GRADE` VARCHAR(5),
    `PYVPASJ_SAL_STEP` SMALLINT,
    `PYVPASJ_ORGN_CODE_TS` VARCHAR(6) NOT NULL,
    `PYVPASJ_ORGN_DESC` VARCHAR(30), `PYVPASJ_BCTR_TITLE` VARCHAR(4), `PYVPASJ_SUPERVISOR_PIDM` INT, `PYVPASJ_SUPERVISOR_POSN` VARCHAR(6), `PYVPASJ_SUPERVISOR_SUFF` VARCHAR(2), `PYVPASJ_TRIAL_IND` DOUBLE NOT NULL, `PYVPASJ_ANNUAL_IND` DOUBLE NOT NULL, `PYVPASJ_EVAL_DATE` DATETIME, `PYVPASJ_LOW` DECIMAL(13,4), `PYVPASJ_MIDPOINT` DECIMAL(13,4), `PYVPASJ_HIGH` DECIMAL(13,4), `PYVPASJ_SALARY` DECIMAL(11,2), `PYVPASJ_SGRP_CODE` VARCHAR(6), `PYVPASJ_INCLUDE_RANKED_FLAG` TINYINT) ;

   /* Moved to CREATE TABLE
COMMENT ON COLUMN `PYVPASJ`.`PYVPASJ_PIDM` IS 'Pidm' */
   ALTER TABLE `PYVPASJ`  COMMENT 'OSU PASS System -- Jobs View';
-- ------------------------------------------------------
--  DDL for Table PYVPDES
-- ------------------------------------------------------

  CREATE TABLE `PYVPDES` (
    `POSITIONDESCRIPTIONID` TEXT,
    `POSITIONNUMBER` TEXT,
    `UNIVERSITYID` TEXT,
    `POSITIONTITLE` TEXT,
    `JOBTITLE` TEXT,
    `DEPARTMENT` TEXT,
    `EMPLOYEEFIRSTNAME` TEXT,
    `EMPLOYEELASTNAME` TEXT,
    `EFFECTIVEDATE` TEXT,
    `POSITIONAPPOINTMENTPERCENT` TEXT,
    `APPOINTMENTBASIS` TEXT,
    `FLSASTATUS` TEXT,
    `JOBLOCATION` TEXT,
    `POSITIONCODEDESCRIPTION` TEXT,
    `POSITIONSUMMARY` TEXT,
    `DECISIONMAKINGGUIDELINES` TEXT,
    `PERCLEADWORKSUPERDUTIES` TEXT,
    `NBREMPLLEADORSUPVD` TEXT,
    `POSITIONDUTIES` TEXT,
    `POSITIONDUTIESCONTINUED` TEXT,
    `ADDTLREQQUALIFS` TEXT,
    `PREFERREDQUALIFICATIONS` TEXT,
    `CRIMBCKGRNDANDORDMVCHKRQRD` TEXT,
    `VALIDDRIVERLICENSEREQUIRED` TEXT,
    `COMMITMENTNCAAANDFSB` TEXT,
    `EMPLOYMENTCATEGORY` TEXT,
    `WORKSCHEDULE` TEXT,
    `WORKINGCONDITIONS` TEXT,
    `LASTUPDATEDATE` DATETIME,
    `LEADERSHIPPOSNCOMMTODIVERSITY` TEXT,
    `POSITIONTITLECODE` TEXT,
    `MINIMUMQUALIFICATIONS` TEXT);
-- ------------------------------------------------------
--  DDL for Table PYVPDLW
-- ------------------------------------------------------

  CREATE TABLE `PYVPDLW` (`POSITIONDESCRIPTIONID` BIGINT NOT NULL, `RESPONSE` VARCHAR(4000));
-- ------------------------------------------------------
--  DDL for Table RATINGS
-- ------------------------------------------------------

  CREATE TABLE `RATINGS` (
    `ID` BIGINT NOT NULL,
    `RATE` SMALLINT NOT NULL,
    `NAME` VARCHAR(64),
    `DESCRIPTION` VARCHAR(512), `APPOINTMENT_TYPE` VARCHAR(45));
-- ------------------------------------------------------
--  DDL for Table REVIEWERS
-- ------------------------------------------------------

  CREATE TABLE `REVIEWERS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `EMPLOYEE_PIDM` BIGINT NOT NULL,
    `BUSINESS_CENTER_NAME` VARCHAR(4) NOT NULL) ;

   ALTER TABLE `REVIEWERS`  COMMENT 'Business center HR reviewers';
-- ------------------------------------------------------
--  DDL for Table REVIEW_CYCLE_OPTIONS
-- ------------------------------------------------------

  CREATE TABLE `REVIEW_CYCLE_OPTIONS` (
    `ID` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `NAME` VARCHAR(75) NOT NULL,
    `VALUE` TINYINT NOT NULL,
    `SEQUENCE` TINYINT NOT NULL,
    `CREATOR_PIDM` BIGINT NOT NULL,
    `CREATE_DATE` DATETIME NOT NULL,
    `DELETER_PIDM` BIGINT, `DELETE_DATE` DATETIME);
-- ------------------------------------------------------
--  DDL for Table SALARIES
-- ------------------------------------------------------

  CREATE TABLE `SALARIES` (
    `ID` BIGINT NOT NULL,
    `APPRAISAL_ID` BIGINT NOT NULL,
    `SALARY_LOW` DECIMAL(13,4), `SALARY_MIDPOINT` DECIMAL(13,4), `SALARY_HIGH` DECIMAL(13,4), `SALARY_CURRENT` DECIMAL(11,4), `SALARY_SGRP_CODE` VARCHAR(6), `SALARY_INCREASE` DECIMAL(4,2), `TWO_INCREASE` DECIMAL(4,2), `ONE_MAX` DECIMAL(4,2), `ONE_MIN` DECIMAL(4,2));
-- ------------------------------------------------------
--  DDL for Table STATUS
-- ------------------------------------------------------

  CREATE TABLE `STATUS` (`STATUS` VARCHAR(32) NOT NULL) ;

   ALTER TABLE `STATUS`  COMMENT 'Possible status of appraisals';
-- ------------------------------------------------------
--  Constraints for Table ACTIONS
-- ------------------------------------------------------

  -- ALTER TABLE `ACTIONS` MODIFY `ACTION` NOT NULL;
  ALTER TABLE `ACTIONS` ADD CONSTRAINT `PK_ACTIONS` PRIMARY KEY (`ACTION`);
-- ------------------------------------------------------
--  Constraints for Table ADMINS
-- ------------------------------------------------------

  -- ALTER TABLE `ADMINS` ADD CONSTRAINT `PK_ADMINS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table APPOINTMENT_TYPES
-- ------------------------------------------------------

  -- ALTER TABLE `APPOINTMENT_TYPES` MODIFY (`NAME` NOT NULL ENABLE);
  ALTER TABLE `APPOINTMENT_TYPES` ADD CONSTRAINT `PK_APPOINTMENT_TYPES` PRIMARY KEY (`NAME`);
-- ------------------------------------------------------
--  Constraints for Table APPRAISALS
-- ------------------------------------------------------

  -- ALTER TABLE `APPRAISALS` ADD CONSTRAINT `PK_APPRAISALS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table APPRAISAL_STEPS
-- ------------------------------------------------------

  ALTER TABLE `APPRAISAL_STEPS` ADD CONSTRAINT `PK_APPRAISAL_STEPS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table ASSESSMENTS
-- ------------------------------------------------------

  -- ALTER TABLE `ASSESSMENTS` ADD CONSTRAINT `PK_ASSESSMENTS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table ASSESSMENTS_CRITERIA
-- ------------------------------------------------------

  -- ALTER TABLE `ASSESSMENTS_CRITERIA` ADD CONSTRAINT `PK_ASSESSMENTSCRITERIA` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table BUSINESS_CENTERS
-- ------------------------------------------------------

  ALTER TABLE `BUSINESS_CENTERS` ADD CONSTRAINT `PK_BUSINESS_CENTERS` PRIMARY KEY (`NAME`);
-- ------------------------------------------------------
--  Constraints for Table CLOSEOUT_REASONS
-- ------------------------------------------------------

  -- ALTER TABLE `CLOSEOUT_REASONS` ADD CONSTRAINT `PK_CLOSEOUT_REASONS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table CONFIGURATIONS
-- ------------------------------------------------------

  ALTER TABLE `CONFIGURATIONS` ADD CONSTRAINT `PK_CONFIGURATIONS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table CONFIG_TIMES
-- ------------------------------------------------------

  -- ALTER TABLE `CONFIG_TIMES` MODIFY (`CONTEXT_DATETIME` NOT NULL ENABLE);
-- ------------------------------------------------------
--  Constraints for Table CRITERIA_AREAS
-- ------------------------------------------------------

  -- ALTER TABLE `CRITERIA_AREAS` ADD CONSTRAINT `PK_CRITERIA_AREAS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table CRITERIA_DETAILS
-- ------------------------------------------------------

  ALTER TABLE `CRITERIA_DETAILS` ADD CONSTRAINT `PK_CRITERIA_DETAILS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table EMAILS
-- ------------------------------------------------------

  -- ALTER TABLE `EMAILS` ADD CONSTRAINT `PK_EMAILS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table EMAIL_TYPES
-- ------------------------------------------------------

  ALTER TABLE `EMAIL_TYPES` ADD CONSTRAINT `PK_EMAIL_TYPES` PRIMARY KEY (`TYPE`);
-- ------------------------------------------------------
--  Constraints for Table GOALS_LOGS
-- ------------------------------------------------------

  -- ALTER TABLE `GOALS_LOGS` ADD CONSTRAINT `PK_GOALS_LOGS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table GOALS_VERSIONS
-- ------------------------------------------------------

  -- ALTER TABLE `GOALS_VERSIONS` ADD CONSTRAINT `PK_GOALSVERSIONS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table NOLIJ_COPIES
-- ------------------------------------------------------

  ALTER TABLE `NOLIJ_COPIES` ADD CONSTRAINT `PK_NOLIJ_COPIES` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table PERMISSION_RULES
-- ------------------------------------------------------

  ALTER TABLE `PERMISSION_RULES` ADD CONSTRAINT `PK_PERMISSION_RULES` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table PYVPASE
-- ------------------------------------------------------

  -- ALTER TABLE `PYVPASE` ADD CONSTRAINT `PK_PYVPASE` PRIMARY KEY (`PYVPASE_PIDM`);
-- ------------------------------------------------------
--  Constraints for Table PYVPASJ
-- ------------------------------------------------------

  ALTER TABLE `PYVPASJ` ADD CONSTRAINT `PK_PYVPASJ` PRIMARY KEY (`PYVPASJ_PIDM`, `PYVPASJ_POSN`, `PYVPASJ_SUFF`);
-- ------------------------------------------------------
--  Constraints for Table PYVPDLW
-- ------------------------------------------------------

  -- ALTER TABLE `PYVPDLW` MODIFY (`POSITIONDESCRIPTIONID` NOT NULL ENABLE);
-- ------------------------------------------------------
--  Constraints for Table RATINGS
-- ------------------------------------------------------

-- ------------------------------------------------------
--  Constraints for Table REVIEWERS
-- ------------------------------------------------------

  -- ALTER TABLE `REVIEWERS` ADD CONSTRAINT `PK_REVIEWERS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table REVIEW_CYCLE_OPTIONS
-- ------------------------------------------------------

  -- ALTER TABLE `REVIEW_CYCLE_OPTIONS` ADD CONSTRAINT `PK_REVIEW_CYCLE_OPTIONS` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table SALARIES
-- ------------------------------------------------------

  ALTER TABLE `SALARIES` ADD CONSTRAINT `PK_SALARIES` PRIMARY KEY (`ID`);
-- ------------------------------------------------------
--  Constraints for Table STATUS
-- ------------------------------------------------------

  ALTER TABLE `STATUS` ADD CONSTRAINT `PK_STATUS` PRIMARY KEY (`STATUS`);

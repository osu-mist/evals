SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

CREATE TABLE `actions` (
  `ACTION` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ACTION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `admins` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `EMPLOYEE_PIDM` int(11) NOT NULL,
  `IS_MASTER` tinyint(1) NOT NULL,
  `CREATE_DATE` datetime NOT NULL,
  `CREATOR_PIDM` int(11) NOT NULL,
  `MODIFIED_DATE` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `employeeID` (`EMPLOYEE_PIDM`),
  KEY `createdBy` (`CREATOR_PIDM`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=35 ;

CREATE TABLE `appointment_types` (
  `NAME` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `appraisals` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `STATUS` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `START_DATE` date NOT NULL,
  `END_DATE` date NOT NULL,
  `JOB_PIDM` int(11) NOT NULL,
  `POSITION_NUMBER` varchar(8) COLLATE utf8_unicode_ci NOT NULL,
  `JOB_SUFFIX` varchar(2) COLLATE utf8_unicode_ci NOT NULL,
  `OVERDUE` int(11) DEFAULT NULL,
  `GOALS_APPROVER_PIDM` int(11) DEFAULT NULL,
  `GOALS_SUBMIT_DATE` datetime DEFAULT NULL,
  `GOALS_APPROVED_DATE` datetime DEFAULT NULL,
  `GOALS_REQUIRED_MOD_DATE` datetime DEFAULT NULL,
  `GOALS_COMMENTS` text COLLATE utf8_unicode_ci,
  `RESULT_SUBMIT_DATE` datetime DEFAULT NULL,
  `EVALUATION` text COLLATE utf8_unicode_ci,
  `EVALUATOR_PIDM` int(11) DEFAULT NULL,
  `EVALUATION_SUBMIT_DATE` datetime DEFAULT NULL,
  `RATING` int(1) DEFAULT NULL,
  `REVIEWER_PIDM` int(11) DEFAULT NULL,
  `REVIEW_SUBMIT_DATE` datetime DEFAULT NULL,
  `REVIEW` text COLLATE utf8_unicode_ci,
  `CREATE_DATE` datetime NOT NULL,
  `REBUTTAL` text COLLATE utf8_unicode_ci,
  `REBUTTAL_DATE` datetime DEFAULT NULL,
  `EMPLOYEE_SIGNED_DATE` datetime DEFAULT NULL,
  `CLOSEOUT_PIDM` int(11) DEFAULT NULL,
  `CLOSEOUT_DATE` date DEFAULT NULL,
  `CLOSEOUT_REASON_ID` int(11) DEFAULT NULL,
  `REOPENER_PIDM` int(11) DEFAULT NULL,
  `REOPENED_DATE` datetime DEFAULT NULL,
  `REOPEN_REASON` text COLLATE utf8_unicode_ci,
  `ORIGINAL_STATUS` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RELEASE_DATE` datetime DEFAULT NULL COMMENT 'The time when the supervisor requested employee''s signature.',
  `SUPERVISOR_REBUTTAL_READ` datetime DEFAULT NULL,
  `TYPE` varchar(16) COLLATE utf8_unicode_ci NOT NULL COMMENT 'possible values are: trial annual special',
  `GOALS_OVERDUE` int(3) DEFAULT NULL,
  `RESULTS_OVERDUE` int(3) DEFAULT NULL,
  `APPRAISAL_OVERDUE` int(3) DEFAULT NULL,
  `REVIEW_OVERDUE` int(3) DEFAULT NULL,
  `RELEASE_OVERDUE` int(3) DEFAULT NULL,
  `SIGNATURE_OVERDUE` int(3) DEFAULT NULL,
  `REBUTTAL_READ_OVERDUE` int(3) DEFAULT NULL,
  `GOALS_APPROVAL_OVERDUE` int(3) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `hrApproverID` (`REVIEWER_PIDM`),
  KEY `closeOutBy` (`CLOSEOUT_PIDM`),
  KEY `goalApproverID` (`GOALS_APPROVER_PIDM`),
  KEY `reopenedBy` (`REOPENER_PIDM`),
  KEY `originalStatus` (`ORIGINAL_STATUS`),
  KEY `status` (`STATUS`),
  KEY `CLOSEOUT_REASON_ID` (`CLOSEOUT_REASON_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=382 ;

CREATE TABLE `appraisal_steps` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ACTION` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `APPOINTMENT_TYPE` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `NEW_STATUS` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `EMAIL_TYPE` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `emailType` (`EMAIL_TYPE`),
  KEY `newStatus` (`NEW_STATUS`),
  KEY `appointmentType` (`APPOINTMENT_TYPE`),
  KEY `action` (`ACTION`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=3 ;

CREATE TABLE `assessments` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `GOAL_VERSION_ID` int(11) NOT NULL,
  `GOAL` text COLLATE utf8_unicode_ci,
  `EMPLOYEE_RESULT` text COLLATE utf8_unicode_ci,
  `SUPERVISOR_RESULT` text COLLATE utf8_unicode_ci,
  `SEQUENCE` int(11) NOT NULL,
  `CREATE_DATE` datetime NOT NULL,
  `MODIFIED_DATE` datetime DEFAULT NULL,
  `DELETER_PIDM` int(11) DEFAULT NULL,
  `DELETE_DATE` date DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `appraisalID` (`GOAL_VERSION_ID`),
  KEY `criterionDetailID` (`SEQUENCE`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1866 ;

CREATE TABLE `assessments_criteria` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CRITERIA_AREA_ID` int(11) NOT NULL,
  `ASSESSMENT_ID` int(11) NOT NULL,
  `CHECKED` int(1) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3729 ;

CREATE TABLE `business_centers` (
  `NAME` varchar(4) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `closeout_reasons` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `REASON` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `CREATE_DATE` datetime NOT NULL,
  `CREATOR_PIDM` int(11) NOT NULL,
  `DELETE_DATE` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `CREATOR_PIDM` (`CREATOR_PIDM`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=12 ;

CREATE TABLE `configurations` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `SECTION` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `SEQUENCE` int(3) NOT NULL,
  `VALUE` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `REFERENCE_POINT` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `ACTION` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=8 ;

CREATE TABLE `config_times` (
  `CONTEXT_DATETIME` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `criteria_areas` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `APPOINTMENT_TYPE` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `DESCRIPTION` text COLLATE utf8_unicode_ci NOT NULL,
  `ANCESTOR_ID` int(11) DEFAULT NULL,
  `CREATE_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CREATOR_PIDM` int(11) NOT NULL,
  `DELETE_DATE` timestamp NULL DEFAULT NULL,
  `DELETER_PIDM` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `originalID` (`ANCESTOR_ID`),
  KEY `createdBy` (`CREATOR_PIDM`),
  KEY `deletedBy` (`DELETER_PIDM`),
  KEY `appointmentType` (`APPOINTMENT_TYPE`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=78 ;

CREATE TABLE `emails` (
  `ID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `APPRAISAL_ID` int(11) unsigned NOT NULL,
  `EMAIL_TYPE` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `SENT_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=34 ;

CREATE TABLE `email_types` (
  `TYPE` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `MAILTO` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `CC` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BCC` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `type` (`TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `employees` (
  `PYVPASE_PIDM` int(11) NOT NULL,
  `PYVPASE_FIRST_NAME` varchar(150) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASE_MI` varchar(150) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PYVPASE_LAST_NAME` varchar(150) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASE_ID` varchar(9) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASE_ONID_LOGIN` varchar(15) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASE_EMAIL` varchar(75) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASE_EMPL_STATUS` varchar(1) COLLATE utf8_unicode_ci NOT NULL,
  KEY `pidm` (`PYVPASE_PIDM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `goals_logs` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CONTENT` text COLLATE utf8_unicode_ci NOT NULL,
  `ASSESSMENT_ID` int(11) NOT NULL,
  `AUTHOR_PIDM` int(11) NOT NULL,
  `CREATE_DATE` datetime NOT NULL,
  `TYPE` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'default to null, which will be for normal goals. the value is new for new goals, used for goalsReactivated.',
  PRIMARY KEY (`ID`),
  KEY `assessmentID` (`ASSESSMENT_ID`),
  KEY `authorPidm` (`AUTHOR_PIDM`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=71 ;

CREATE TABLE `goals_versions` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `APPROVER_PIDM` int(11) DEFAULT NULL,
  `CREATE_DATE` date NOT NULL,
  `APPROVED_DATE` date DEFAULT NULL,
  `APPRAISAL_ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=378 ;

CREATE TABLE `jobs` (
  `PYVPASJ_PIDM` int(11) NOT NULL DEFAULT '0',
  `PYVPASJ_STATUS` varchar(1) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_DESC` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_POSN` varchar(8) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_SUFF` varchar(2) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_ECLS_CODE` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_APPOINTMENT_TYPE` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_BEGIN_DATE` date NOT NULL,
  `PYVPASJ_END_DATE` date DEFAULT NULL,
  `PYVPASJ_BCTR_TITLE` varchar(4) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_ORGN_CODE_TS` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_ORGN_DESC` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_PCLS_CODE` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_SAL_GRADE` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_SAL_STEP` varchar(3) COLLATE utf8_unicode_ci NOT NULL,
  `PYVPASJ_SUPERVISOR_PIDM` int(11) DEFAULT NULL,
  `PYVPASJ_SUPERVISOR_POSN` varchar(8) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PYVPASJ_SUPERVISOR_SUFF` varchar(2) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PYVPASJ_TRIAL_IND` int(11) NOT NULL,
  `PYVPASJ_ANNUAL_IND` int(11) NOT NULL,
  `PYVPASJ_EVAL_DATE` datetime DEFAULT NULL,
  PRIMARY KEY (`PYVPASJ_PIDM`,`PYVPASJ_POSN`,`PYVPASJ_SUFF`),
  KEY `employeePidm` (`PYVPASJ_PIDM`),
  KEY `appointmentType` (`PYVPASJ_APPOINTMENT_TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `nolij_copies` (
  `ID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `APPRAISAL_ID` int(11) unsigned NOT NULL,
  `SUBMIT_DATE` datetime NOT NULL,
  `FILE_NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

CREATE TABLE `permission_rules` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `STATUS` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `ROLE` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `GOALS` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NEW_GOALS` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `GOAL_COMMENTS` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RESULTS` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SUPERVISOR_RESULTS` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `EVALUATION` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REVIEW` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `EMPLOYEE_RESPONSE` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REBUTTAL_READ` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SAVE_DRAFT` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REQUIRE_MODIFICATION` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SUBMIT` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ACTION_REQUIRED` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `status` (`STATUS`),
  KEY `submit` (`SUBMIT`),
  KEY `requireModification` (`REQUIRE_MODIFICATION`),
  KEY `saveDraft` (`SAVE_DRAFT`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=3 ;
CREATE TABLE `pyvpase` (
`PYVPASE_PIDM` int(11)
,`PYVPASE_FIRST_NAME` varchar(150)
,`PYVPASE_MI` varchar(150)
,`PYVPASE_LAST_NAME` varchar(150)
,`PYVPASE_ID` varchar(9)
,`PYVPASE_ONID_LOGIN` varchar(15)
,`PYVPASE_EMAIL` varchar(75)
,`PYVPASE_EMPL_STATUS` varchar(1)
);CREATE TABLE `pyvpasj` (
`PYVPASJ_PIDM` int(11)
,`PYVPASJ_STATUS` varchar(1)
,`PYVPASJ_DESC` varchar(255)
,`PYVPASJ_POSN` varchar(8)
,`PYVPASJ_SUFF` varchar(2)
,`PYVPASJ_ECLS_CODE` varchar(45)
,`PYVPASJ_APPOINTMENT_TYPE` varchar(45)
,`PYVPASJ_BEGIN_DATE` date
,`PYVPASJ_END_DATE` date
,`PYVPASJ_BCTR_TITLE` varchar(4)
,`PYVPASJ_ORGN_CODE_TS` varchar(45)
,`PYVPASJ_ORGN_DESC` varchar(255)
,`PYVPASJ_PCLS_CODE` varchar(45)
,`PYVPASJ_SAL_GRADE` varchar(45)
,`PYVPASJ_SAL_STEP` varchar(3)
,`PYVPASJ_SUPERVISOR_PIDM` int(11)
,`PYVPASJ_SUPERVISOR_POSN` varchar(8)
,`PYVPASJ_SUPERVISOR_SUFF` varchar(2)
,`PYVPASJ_TRIAL_IND` int(11)
,`PYVPASJ_ANNUAL_IND` int(11)
,`PYVPASJ_EVAL_DATE` datetime
);
CREATE TABLE `reviewers` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `EMPLOYEE_PIDM` int(11) NOT NULL,
  `BUSINESS_CENTER_NAME` varchar(4) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `employeeID` (`EMPLOYEE_PIDM`),
  KEY `businessCenterName` (`BUSINESS_CENTER_NAME`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=24 ;

CREATE TABLE `status` (
  `STATUS` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  KEY `status` (`STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
DROP TABLE IF EXISTS `pyvpase`;

CREATE  VIEW `pyvpase` AS select * from `employees`;
DROP TABLE IF EXISTS `pyvpasj`;

CREATE  VIEW `pyvpasj` AS select * from `jobs`;

-- re #23787
-- add autosave configuration value
insert into configurations values(pass_seq.nextval, 'configuration', 'autoSaveFrequency', 14, 60, null, null);
alter table goals_versions add (goals_comments varchar2(4000 char));
insert into configurations values(pass_seq.nextval, 'configuration', 'daysBeforeArchive', 15, 365, null, null);
insert into status values('archivedCompleted');
insert into status values('archivedClosed');
delete from status where status = 'archived';
alter table goals_versions add(goals_required_mod_date date);
UPDATE goals_versions 
SET (GOALS_COMMENTS, GOALS_REQUIRED_MOD_DATE) = (SELECT GOALS_COMMENTS, GOALS_REQUIRED_MOD_DATE
                                         FROM   appraisals 
                                         WHERE  appraisals.id = goals_versions.appraisal_id); 

-- Professional Faculty Changes
insert into appraisal_steps values(pass_seq.nextval, 'submit-appraisal', 'Professional Faculty', 'signatureDue', 'signatureDue');
alter table permission_rules add (
    appointment_type VARCHAR2(45 CHAR)
);
-- upload the permission_rules data via csv and then set the appointment_type column to not null
alter table configurations add (
    appointment_type VARCHAR2(45 CHAR)
);

update configurations set appointment_type = 'Default';

-- The -1 configuration value, allows us to disable emails for professional faculty
-- insert configuration items for professional faculty to prevent overdue emails from being sent
insert into configurations
select pass_seq.nextval, section, name, 21 + rownum, '-1', null, null, 'Professional Faculty'
from configurations where name like '%OverdueFrequency';

-- for any emails that we only want to be sent once, setting the frequency to a high # to prevent multiple emails
insert into configurations
select pass_seq.nextval, section, name, 28 + rownum, '200', null, null, 'Professional Faculty'
from configurations where name like '%DueFrequency';

-- insert different configuration values for professional faculty so they can be changed via the admin gui
insert into configurations
select pass_seq.nextval, section, name, 35 + rownum, value + 5, reference_point, action, 'Professional Faculty'
from configurations where name like '%Due';

insert into configurations
select pass_seq.nextval, section, name, 35 + rownum, value + 5, reference_point, action, 'Professional Faculty'
from configurations where name like '%Reminder';

-- ev-115 send goals approval due email via the web gui instead of backend
update appraisal_steps set email_type = 'goalsApprovalDue' where action = 'submit-goals';

-- EV-117
insert into status values('employeeReviewDue');
update appraisal_steps set new_status = 'employeeReviewDue', email_type = 'employeeReviewDue' 
where action = 'submit-appraisal' and appointment_type = 'Professional Faculty';
insert into appraisal_steps values(pass_seq.nextval, 'submit-review', 'Professional Faculty', 'releaseDue', 'releaseDue');
insert into email_types values('employeeReviewDue', 'employee', null, null);
insert into configurations values(pass_seq.nextval, 'configuration', 'employeeReviewDueExpiration', 16, '10', 'evaluationSubmitDate', 'add', 'Default');

-- EV-123: enable/disable prof faculty
insert into configurations values(pass_seq.nextval, 'configuration', 'enableProfessionalFaculty', 17, 0, 0, null, 'Default');

-- EV-124: add rating table
CREATE TABLE ratings (
    ID NUMBER(11) NOT NULL,
    RATE NUMBER(3) NOT NULL,
    NAME VARCHAR2(64 CHAR),
    DESCRIPTION VARCHAR2(512 CHAR),
    APPOINTMENT_TYPE VARCHAR2(45 CHAR)
);
insert into ratings values(1, 1, '1', 'Makes outstanding contribution in critical areas while meeting all major requirements of the position.', 'Classified');
insert into ratings values(2, 2, '2', 'Performs requirements of the position in a satisfactory manner.', 'Classified');
insert into ratings values(3, 3, '3', 'Does not meet performance requirements of the position in major or critical areas.', 'Classified');
insert into ratings values(4, 4, '4', 'Not rated', 'Classified');
insert into ratings values(5, 1, '1', 'Makes outstanding contribution in critical areas while meeting all major requirements of the position.', 'Classified IT');
insert into ratings values(6, 2, '2', 'Performs requirements of the position in a satisfactory manner.', 'Classified IT');
insert into ratings values(7, 3, '3', 'Does not meet performance requirements of the position in major or critical areas.', 'Classified IT');
insert into ratings values(8, 1, 'Exceptional Performance', ' - Consistently operates well above expectations', 'Professional Faculty');
insert into ratings values(9, 2, 'Strong Performance', ' - Fully meets and often exceeds expectations of the position', 'Professional Faculty');
insert into ratings values(10, 3, 'Satisfactory Performance', ' - Fully meets the expectations for the position', 'Professional Faculty');
insert into ratings values(11, 4, 'Needs Improvement', ' - Occasionally fails to meet expectations or needs significant improvement in critical areas', 'Professional Faculty');
insert into ratings values(12, 5, 'Does Not Meet Expectations', ' - Does not meet expectations in multiple key elements of the position', 'Professional Faculty');
insert into ratings values(13, 6, 'No basis for evaluation / Not rated', '', 'Professional Faculty');

-- EV-128: completion email reminder
insert into configurations values(pass_seq.nextval, 'email-notification', 'firstCompletionReminder', '38', '60', 'end', 'subtract', 'Professional Faculty');
insert into configurations values(pass_seq.nextval, 'email-notification', 'secondCompletionReminder', '39', '60', 'end', 'subtract', 'Professional Faculty');
insert into email_types values('firstCompletionReminder', 'supervisor', null, null);
insert into email_types values('secondCompletionReminder', 'supervisor', null, null);

-- EV-129: late report
insert into email_types values ('lateReportAABC', 'reviewer', null, null);
insert into email_types values ('lateReportAMBC', 'reviewer', null, null);
insert into email_types values ('lateReportASBC', 'reviewer', null, null);
insert into email_types values ('lateReportBEBC', 'reviewer', null, null);
insert into email_types values ('lateReportCCBC', 'reviewer', null, null);
insert into email_types values ('lateReportFOBC', 'reviewer', null, null);
insert into email_types values ('lateReportHSBC', 'reviewer', null, null);
insert into email_types values ('lateReportUABC', 'reviewer', null, null);
insert into email_types values ('lateReportadmins', 'admins', null, null);

-- EV-157: small rating update
update ratings set description = ' - Occasionally does not meet expectations or needs significant improvement in critical areas' where id = 11 and name = 'Needs Improvement';


-- EV-159: update rating display
update ratings set description = 'Consistently operates well above expectations' where id = 8  and name = 'Exceptional Performance';
update ratings set description = 'Fully meets and often exceeds expectations of the position' where id = 9  and name = 'Strong Performance';
update ratings set description = 'Fully meets the expectations for the position' where id = 10  and name = 'Satisfactory Performance';
update ratings set description = 'Occasionally does not meet expectations or needs significant improvement in critical areas' where id = 11 and name = 'Needs Improvement';
update ratings set description = 'Does not meet expectations in multiple key elements of the position' where id = 12  and name = 'Does Not Meet Expectations';

-- EV-161: late report update
delete from email_types where type = 'lateReportadmins';
insert into email_types values ('lateReportOHR', 'admins', null, null);

-- EV-163: employee review due expiration
insert into email_types values ('employeeReviewExpired', 'supervisor', null, null);

-- EV-167: cc employee on employee review expired email
update email_types set cc = 'employee' where type = 'employeeReviewExpired';

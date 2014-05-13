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
insert into email_types values ('initiatedProfessionalFaculty', 'employee', null, null);
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

-- EV-117
insert into status values('employeeReviewDue');
update appraisal_steps set new_status = 'employeeReviewDue', email_type = 'employeeReviewDue' 
where action = 'submit-appraisal' and appointment_type = 'Professional Faculty';
insert into appraisal_steps values(pass_seq.nextval, 'submit-review', 'Professional Faculty', 'releaseDue', 'releaseDue');
insert into email_types values('employeeReviewDue', 'employee', null, null);

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

-- EV-185
INSERT INTO email_types (type, mailto) VALUES ('signatureDueNotRated', 'upper supervisor');

-- EV-191: change configurations values/actions
update configurations set name = 'employeeReviewDue', value = 60, action = 'add' where section = 'due-date' and name = 'reviewDue' and appointment_type = 'Professional Faculty';
update configurations set value = 30, action = 'add' where name = 'resultsDue' and appointment_type = 'Professional Faculty';
update configurations set value = 45, action = 'add' where name = 'appraisalDue' and appointment_type = 'Professional Faculty';
update configurations set value = 75, action = 'add' where name = 'releaseDue' and appointment_type = 'Professional Faculty';
update configurations set value = 90, action = 'add' where name = 'signatureDue' and appointment_type = 'Professional Faculty';
update configurations set value = 0,  action = 'add' where name = 'firstResultDueReminder' and appointment_type = 'Professional Faculty';
update configurations set value = 15 where name = 'employeeReviewDueExpiration';

-- EV-526: configuration changes
delete from configurations where name = 'closeDue';

-- EV-5: remove multiple columns from appraisals
ALTER TABLE appraisals DROP (
    goals_comments,
    goals_approver_pidm,
    goals_approved_date,
    goals_reactivated_date,
    goals_required_mod_date,
    goals_submit_date
);
    
-- EV-529: remove reopen related columsn from appraisals table
ALTER TABLE appraisals DROP (
    reopener_pidm,
    reopen_reason,
    reopened_date
);

-- EV-191: change configurations values/actions
update configurations set name = 'reviewDue-prof-faculty' where section = 'due-date' and name = 'reviewDue' and appointment_type = 'Professional Faculty';
update configurations set value = 30, action = 'add' where name = 'resultsDue' and appointment_type = 'Professional Faculty';
update configurations set value = 45, action = 'add' where name = 'appraisalDue' and appointment_type = 'Professional Faculty';
update configurations set value = 60, action = 'add' where name = 'reviewDue' and appointment_type = 'Professional Faculty';
update configurations set value = 75, action = 'add' where name = 'releaseDue' and appointment_type = 'Professional Faculty';
update configurations set value = 90, action = 'add' where name = 'signatureDue' and appointment_type = 'Professional Faculty';

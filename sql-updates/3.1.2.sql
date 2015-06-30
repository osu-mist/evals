-- EV-602 add appraisal steps for Ranked Faculty
insert into appraisal_steps as
select pass_seq.nextval, action, 'Ranked Faculty', new_status, email_type
from appraisal_steps where appointment_type = 'Professional Faculty';

-- EV-604
insert into appointment_types values('Ranked Faculty');

insert into configurations
select pass_seq.nextval, section, name, (select max(sequence) from configurations where section = c.section) + rownum as seq, value, reference_point, action, 'Ranked Faculty'
from configurations c
where appointment_type != 'Default' and section = 'email-notification';

insert into configurations
select pass_seq.nextval, section, name, (select max(sequence) from configurations where section = c.section) + rownum as seq, value, reference_point, action, 'Ranked Faculty'
from configurations c
where appointment_type != 'Default' and section = 'configuration';

insert into configurations
select pass_seq.nextval, section, name, (select max(sequence) from configurations where section = c.section) + rownum as seq, value, reference_point, action, 'Ranked Faculty'
from configurations c
where appointment_type != 'Default' and section = 'due-date';

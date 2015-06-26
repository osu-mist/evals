-- EV-602 add appraisal steps for Ranked Faculty
insert into appraisal_steps as
select pass_seq.nextval, action, 'Ranked Faculty', new_status, email_type 
from appraisal_steps where appointment_type = 'Professional Faculty';

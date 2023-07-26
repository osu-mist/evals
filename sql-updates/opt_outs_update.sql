connect passmgr/&&passmgr_password

ALTER TABLE opt_outs
MODIFY DELETER_PIDM NUMBER(11) NULL;

ALTER TABLE opt_outs
MODIFY DELETE_DATE DATE NULL;

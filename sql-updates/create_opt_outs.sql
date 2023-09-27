connect passmgr/&&passmgr_password

create table opt_outs
  (ID		NUMBER(11) NOT NULL,
  TYPE		VARCHAR2(255) NOT NULL,
  EMPLOYEE_PIDM		NUMBER(11) NOT NULL,
  CREATOR_PIDM	NUMBER(11) NOT NULL,
  CREATE_DATE	DATE NOT NULL,
  DELETER_PIDM	NUMBER(11),
  DELETE_DATE	DATE)
tablespace data_sml;

alter table OPT_OUTS
  add constraint PK_OPT_OUTS primary key
  (
    ID
  )
using index
tablespace index_sml;

comment on table opt_outs is 'Evals system or email opt outs';

grant select,insert,update,delete on passmgr.opt_outs to passusr;
grant select on opt_outs to usr_passqery_s;

connect passusr/&&passusr_password
create synonym opt_outs for passmgr.opt_outs;

connect passdev/&&passdev_password

create table opt_outs
  (ID		NUMBER(11) NOT NULL,
  TYPE		VARCHAR2(255) NOT NULL,
  EMPLOYEE_PIDM		NUMBER(11) NOT NULL,
  CREATOR_PIDM	NUMBER(11) NOT NULL,
  CREATE_DATE	DATE NOT NULL,
  DELETER_PIDM	NUMBER(11) NOT NULL,
  DELETE_DATE	DATE NOT NULL);

alter table OPT_OUTS
  add constraint PK_OPT_OUTS primary key
  (
    ID
  )
using index;

comment on table opt_outs is 'Evals system or email opt outs';

grant select,insert,update,delete on passmgr.opt_outs to passdev;

create synonym opt_outs for passdev.opt_outs;

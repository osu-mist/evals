connect passmgr/&&passmgr_password

create table notices
  (ID		NUMBER(11) NOT NULL,
  ANCESTOR_ID	NUMBER(11) NOT NULL,
  NAME		VARCHAR2(255) NOT NULL,
  TEXT		VARCHAR2(4000),
  CREATOR_PIDM	NUMBER(11) NOT NULL,
  CREATE_DATE	DATE NOT NULL)
tablespace data_sml;

alter table NOTICES
  add constraint PK_NOTICES primary key
  (
    ID
  )
using index
tablespace index_sml;

comment on table notices is 'Evals system notices';

grant select,insert,update,delete on passmgr.notices to passusr;
grant select on notices to usr_passqery_s;

connect passusr/&&passusr_password
create synonym notices for passmgr.notices;

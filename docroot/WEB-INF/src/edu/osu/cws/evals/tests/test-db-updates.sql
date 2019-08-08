CREATE TABLE review_cycle_options (
    ID              Numeric(11) NOT NULL PRIMARY KEY,
    NAME            VARCHAR(75) NOT NULL,
    VALUE           Numeric(2) NOT NULL,
    SEQUENCE        Numeric(2) NOT NULL,
    CREATOR_PIDM    Numeric(11) NOT NULL,
    CREATE_DATE     DATE NOT NULL,
    DELETER_PIDM    Numeric(11),
    DELETE_DATE     DATE
);

CREATE TABLE ratings (
    ID NUMERIC(11) NOT NULL,
    RATE NUMERIC(3) NOT NULL,
    NAME VARCHAR(64),
    DESCRIPTION VARCHAR(512),
    APPOINTMENT_TYPE VARCHAR(45)
);
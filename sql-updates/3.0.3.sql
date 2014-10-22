-- EV-534 adding review cycle to admin configuration
CREATE TABLE review_cycle_options (
    ID              NUMBER(11) NOT NULL,
    NAME            VARCHAR2(75) NOT NULL,
    VALUE           NUMBER(2) NOT NULL,
    SEQUENCE        NUMBER(2) NOT NULL,
    CREATOR_PIDM    NUMBER(11) NOT NULL,
    CREATE_DATE     DATE NOT NULL,
    DELETER_PIDM    NUMBER(11),
    DELETE_DATE     DATE
);

ALTER TABLE review_cycle_options
ADD CONSTRAINT pk_review_cycle_options
    PRIMARY KEY(id);

-- EV-544
INSERT INTO configurations  (
    ID, 
    SECTION, 
    NAME, 
    VALUE, 
    SEQUENCE, 
    APPOINTMENT_TYPE
) VALUES (
    (SELECT MAX(ID) + 1 FROM configurations), 
    'configuration', 
    'allowProfFacultyPdf', 
    1, 
    (SELECT MAX(SEQUENCE) + 1 FROM configurations where section = 'configuration'), 
    'Professional Faculty'
);
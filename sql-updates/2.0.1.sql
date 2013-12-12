-- re #26029
ALTER TABLE salaries ADD (
    two_increase number(3,1), 
    one_max number(3,1), 
    one_min number(3,1)
);

-- re #26646
ALTER TABLE salaries MODIFY (
    salary_increase number(4,2),
    two_increase number(4,2),
    one_max number(4,2),
    one_min number(4,2)
);

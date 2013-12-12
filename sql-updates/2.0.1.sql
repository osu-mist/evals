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

-- re #26647
-- the two statements below update the salaries for records
-- were the Backend mgr didn't update the values.
UPDATE salaries SET two_increase = 4.75, one_max = 7.25, one_min = 6
WHERE salary_current < salary_midpoint and one_min is null;

UPDATE salaries SET two_increase = 2, one_max = 4, one_min = 3
WHERE salary_current >= salary_midpoint and one_min is null;

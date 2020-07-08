--These columns were originally all number(3).
--Evaluations were getting to need overdue sizes in the 4 digit range,
--so an error was thrown whenever evals tried to insert a 4 digit value.
ALTER TABLE appraisals
MODIFY (OVERDUE number(4),
        GOALS_OVERDUE number(4),
        GOALS_APPROVAL_OVERDUE number(4),
        RESULTS_OVERDUE number(4),
        APPRAISAL_OVERDUE number(4),
        REVIEW_OVERDUE number(4),
        RELEASE_OVERDUE number(4),
        SIGNATURE_OVERDUE number(4),
        REBUTTAL_READ_OVERDUE number(4));

-- re #23787
-- add autosave configuration value
insert into configurations values(pass_seq.nextval, 'configuration', 'autoSaveFrequency', 14, 60, null, null);
insert into configurations values(pass_seq.nextval, 'configuration', 'daysBeforeArchive', 15, 6, null, null);
insert into status values('archivedCompleted');
insert into status values('archivedClosed');
-- EV-665 fix bug related to bc name change
update email_types set type = 'lateReportCCBO' where type = 'lateReportCCBC';

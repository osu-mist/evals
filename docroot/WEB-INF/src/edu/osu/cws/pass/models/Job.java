/**
 * POJO to represent the employees table.
 */
package edu.osu.cws.pass.models;

import java.util.Date;

public class Job extends Pass {
    private int id;

    private Employee employeePidm;

    private Job supervisor;

    private String positionTitle;

    private String positionNumber;

    private String suffix;

    private String jobEcls;

    private AppointmentType appointmentType;

    private Date beginDate;

    private Date endDate;

    private String positionClass;

    private String tsOrgCode;

    private String orgCodeDescription;

    private String businessCenterName;

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public Employee getEmployeePidm() {
        return employeePidm;
    }

    public void setEmployeePidm(Employee employeePidm) {
        this.employeePidm = employeePidm;
    }

    public Job getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Job supervisor) {
        this.supervisor = supervisor;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public String getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getJobEcls() {
        return jobEcls;
    }

    public void setJobEcls(String jobEcls) {
        this.jobEcls = jobEcls;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPositionClass() {
        return positionClass;
    }

    public void setPositionClass(String positionClass) {
        this.positionClass = positionClass;
    }

    public String getTsOrgCode() {
        return tsOrgCode;
    }

    public void setTsOrgCode(String tsOrgCode) {
        this.tsOrgCode = tsOrgCode;
    }

    public String getOrgCodeDescription() {
        return orgCodeDescription;
    }

    public void setOrgCodeDescription(String orgCodeDescription) {
        this.orgCodeDescription = orgCodeDescription;
    }

    public String getBusinessCenterName() {
        return businessCenterName;
    }

    public void setBusinessCenterName(String businessCenterName) {
        this.businessCenterName = businessCenterName;
    }
}

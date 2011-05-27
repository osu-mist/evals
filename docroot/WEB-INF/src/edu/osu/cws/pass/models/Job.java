/**
 * POJO to represent the employees table.
 */
package edu.osu.cws.pass.models;

import java.util.Date;

public class Job extends Pass {
    private int id;

    private Employee employee;

    private Job supervisor;

    private String status;

    private String jobTitle;

    private String positionNumber;

    private String suffix;

    private String jobEcls;

    private String appointmentType;

    private Date beginDate;

    private Date endDate;

    private String positionClass;

    private String tsOrgCode;

    private String orgCodeDescription;

    private String businessCenterName;

    /**
     * This property holds the job of the current supervisor. If
     * this.supervisor.employee != null, then currentSupervisor holds
     * the same value as this.supervisor. Otherwise currentSupervisor
     * holds whatever is the next supervisor up the chain that has an
     * employee associated to it.
     */
    private Job currentSupervisor;

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Job getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Job supervisor) {
        this.supervisor = supervisor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
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

    public Job getCurrentSupervisor() {
        return currentSupervisor;
    }

    public void setCurrentSupervisor(Job currentSupervisor) {
        this.currentSupervisor = currentSupervisor;
    }
}

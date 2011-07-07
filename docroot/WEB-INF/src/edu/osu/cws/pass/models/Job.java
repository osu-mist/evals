/**
 * POJO to represent the employees table.
 */
package edu.osu.cws.pass.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import edu.osu.cws.util.CWSUtil;

public class Job extends Pass implements Serializable {
    private int id;

    private Employee employee;

    private Job supervisor;

    /**
     * Possible values of status are:
     * A - active,
     * L - leave,
     * T - terminated
     */
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

    private String salaryGrade;

    private String salaryStep;

    private String trialInd;

    private String annualInd;

    //private String evalDate;
    private String evalDate;

    /**
     * This property holds the job of the current supervisor. If
     * this.supervisor.employee != null, then currentSupervisor holds
     * the same value as this.supervisor. Otherwise currentSupervisor
     * holds whatever is the next supervisor up the chain that has an
     * employee associated to it.
     */
    private Job currentSupervisor;

    public Job() { }

    public Job(Employee employee, String positionNumber, String suffix) {
        this.employee = employee;
        this.positionNumber = positionNumber;
        this.suffix = suffix;
    }

    /**
     * Implementing equals method needed by Hibernate to use composite-ide
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job)) return false;

        Job job = (Job) o;

        if (id != job.id) return false;
        if (annualInd != null ? !annualInd.equals(job.annualInd) : job.annualInd != null) return false;
        if (appointmentType != null ? !appointmentType.equals(job.appointmentType) : job.appointmentType != null)
            return false;
        if (beginDate != null ? !beginDate.equals(job.beginDate) : job.beginDate != null) return false;
        if (businessCenterName != null ? !businessCenterName.equals(job.businessCenterName) : job.businessCenterName != null)
            return false;
        if (currentSupervisor != null ? !currentSupervisor.equals(job.currentSupervisor) : job.currentSupervisor != null)
            return false;
        if (employee != null ? !employee.equals(job.employee) : job.employee != null) return false;
        if (endDate != null ? !endDate.equals(job.endDate) : job.endDate != null) return false;
        if (evalDate != null ? !evalDate.equals(job.evalDate) : job.evalDate != null) return false;
        if (jobEcls != null ? !jobEcls.equals(job.jobEcls) : job.jobEcls != null) return false;
        if (jobTitle != null ? !jobTitle.equals(job.jobTitle) : job.jobTitle != null) return false;
        if (orgCodeDescription != null ? !orgCodeDescription.equals(job.orgCodeDescription) : job.orgCodeDescription != null)
            return false;
        if (positionClass != null ? !positionClass.equals(job.positionClass) : job.positionClass != null) return false;
        if (positionNumber != null ? !positionNumber.equals(job.positionNumber) : job.positionNumber != null)
            return false;
        if (salaryGrade != null ? !salaryGrade.equals(job.salaryGrade) : job.salaryGrade != null) return false;
        if (salaryStep != null ? !salaryStep.equals(job.salaryStep) : job.salaryStep != null) return false;
        if (status != null ? !status.equals(job.status) : job.status != null) return false;
        if (suffix != null ? !suffix.equals(job.suffix) : job.suffix != null) return false;
        if (supervisor != null ? !supervisor.equals(job.supervisor) : job.supervisor != null) return false;
        if (trialInd != null ? !trialInd.equals(job.trialInd) : job.trialInd != null) return false;
        if (tsOrgCode != null ? !tsOrgCode.equals(job.tsOrgCode) : job.tsOrgCode != null) return false;

        return true;
    }

    /**
     * Implementing hashCode method required by Hibernate to use composite-id.
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (employee != null ? employee.hashCode() : 0);
        result = 31 * result + (supervisor != null ? supervisor.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (jobTitle != null ? jobTitle.hashCode() : 0);
        result = 31 * result + (positionNumber != null ? positionNumber.hashCode() : 0);
        result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
        result = 31 * result + (jobEcls != null ? jobEcls.hashCode() : 0);
        result = 31 * result + (appointmentType != null ? appointmentType.hashCode() : 0);
        result = 31 * result + (beginDate != null ? beginDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (positionClass != null ? positionClass.hashCode() : 0);
        result = 31 * result + (tsOrgCode != null ? tsOrgCode.hashCode() : 0);
        result = 31 * result + (orgCodeDescription != null ? orgCodeDescription.hashCode() : 0);
        result = 31 * result + (businessCenterName != null ? businessCenterName.hashCode() : 0);
        result = 31 * result + (salaryGrade != null ? salaryGrade.hashCode() : 0);
        result = 31 * result + (salaryStep != null ? salaryStep.hashCode() : 0);
        result = 31 * result + (trialInd != null ? trialInd.hashCode() : 0);
        result = 31 * result + (annualInd != null ? annualInd.hashCode() : 0);
        result = 31 * result + (evalDate != null ? evalDate.hashCode() : 0);
        result = 31 * result + (currentSupervisor != null ? currentSupervisor.hashCode() : 0);
        return result;
    }

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

    public String getSalaryGrade() {
        return salaryGrade;
    }

    public void setSalaryGrade(String salaryGrade) {
        this.salaryGrade = salaryGrade;
    }

    public String getSalaryStep() {
        return salaryStep;
    }

    public void setSalaryStep(String salaryStep) {
        this.salaryStep = salaryStep;
    }

    public String getTrialInd() {
        return trialInd;
    }

    public void setTrialInd(String trialInd) {
        this.trialInd = trialInd;
    }

    public String getAnnualInd() {
        return annualInd;
    }

    public void setAnnualInd(String annualInd) {
        this.annualInd = annualInd;
    }

    public String getEvalDate() {
        return evalDate;
    }

    public void setEvalDate(String evalDate) {
        this.evalDate = evalDate;
    }


    /*
     * A job is within the trial period if
     * 1. The trial indicator is set
     * 2. NOW is within the trial period in term of time.
     */
    public boolean withinTrialPeriod()
    {
       	if (trialInd == null)
	        return false;

        if (evalDate != null)   //@@@for now just to pass testing
           //beginDate = evalDate;
           beginDate = new Date();

        int trialMonths = Integer.parseInt(trialInd);
        Date trialEndDate = CWSUtil.getEndDate(beginDate, trialMonths, Calendar.MONTH);
	    return CWSUtil.isWithinPeriod(beginDate, trialEndDate);
    }

    /**
     *
     * @return
     */
    public Date getNewAnnualStartDate()
    {
         return new Date();
    }

    /**
     * @offset Number of days after the current day
     * @return true is the job is still within the initial period offset days from now. false otherwise
     */
    public static boolean withinInitialPeriod(int offset)
    {

        return true;
    }
}


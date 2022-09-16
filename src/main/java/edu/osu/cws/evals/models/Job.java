/**
 * POJO to represent the employees table.
 */
package edu.osu.cws.evals.models;

import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Job extends Evals implements Serializable {
    private static final long serialVersionUID = 2531139600484032442L;

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

    private int trialInd;

    private int annualInd;

    private Date evalDate;

    private Double salaryLow;
    private Double salaryMidpoint;
    private Double salaryHigh;
    private Double salaryCurrent;
    private String salaryGrpCode;
    private Boolean rankedFlag;

    private Set appraisals = new HashSet();

    private PositionDescription positionDescription;

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
     * Constructor used by JobMgr.getJob to only fetch the pk fields of the
     * given job. The status and appointmentType fields are present for testing
     * purposes.
     *
     * @param id
     * @param positionNumber
     * @param suffix
     * @param status
     * @param appointmentType
     */
    public Job(int id, String positionNumber, String suffix, String status, String appointmentType) {
        this.employee = new Employee();
        this.employee.setId(id);
        this.status = status;
        this.positionNumber = positionNumber;
        this.suffix = suffix;
        this.appointmentType = appointmentType;
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

        return getIdKey().equals(job.getIdKey());
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
//        result = 31 * result + (supervisor != null ? supervisor.hashCode() : 0);
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
       // result = 31 * result + (trialInd != null ? trialInd.hashCode() : 0);
       // result = 31 * result + (annualInd != null ? annualInd.hashCode() : 0);
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

    public int getTrialInd() {
        return trialInd;
    }

    public void setTrialInd(int trialInd) {
        this.trialInd = trialInd;
    }

    public int getAnnualInd() {
        return annualInd;
    }

    public void setAnnualInd(int annualInd) {
        this.annualInd = annualInd;
    }

    public Date getEvalDate() {
        return evalDate;
    }

    public void setEvalDate(Date evalDate) {
        this.evalDate = evalDate;
    }

    public Set getAppraisals() {
        return appraisals;
    }

    public void setAppraisals(Set appraisals) {
        this.appraisals = appraisals;
    }

    public Double getSalaryLow() {
        return salaryLow;
    }

    public void setSalaryLow(Double salaryLow) {
        this.salaryLow = salaryLow;
    }

    public Double getSalaryMidpoint() {
        return salaryMidpoint;
    }

    public void setSalaryMidpoint(Double salaryMidpoint) {
        this.salaryMidpoint = salaryMidpoint;
    }

    public Double getSalaryHigh() {
        return salaryHigh;
    }

    public void setSalaryHigh(Double salaryHigh) {
        this.salaryHigh = salaryHigh;
    }

    public Double getSalaryCurrent() {
        return salaryCurrent;
    }

    public void setSalaryCurrent(Double salaryCurrent) {
        this.salaryCurrent = salaryCurrent;
    }

    public String getSalaryGrpCode() {
        return salaryGrpCode;
    }

    public void setSalaryGrpCode(String salaryGrpCode) {
        this.salaryGrpCode = salaryGrpCode;
    }

    public PositionDescription getPositionDescription() {
        return positionDescription;
    }

    public void setPositionDescription(PositionDescription positionDescription) {
        this.positionDescription = positionDescription;
    }

    public Boolean isRankedFlag() {
        return rankedFlag;
    }

    public void setRankedFlag(Boolean rankedFlag) {
        this.rankedFlag = rankedFlag;
    }

    /**
     * Trial start date doed not need to be the first date of the month.
     * @return  (DateTime) start date of the trial appraisal period.
     */
    public DateTime getTrialStartDate()
    {
        if (trialInd == 0) {
            return null; //No trial appraisal for this job
        }

       if (evalDate != null)
           return new DateTime(evalDate).withTimeAtStartOfDay();

        return new DateTime(beginDate).withTimeAtStartOfDay();
    }


    /*
     * A job is within the trial period if
     * 1. The trial indicator is set
     * 2. NOW is within the trial period in term of time.
     */
    public boolean withinTrialPeriod()
    {
        if (trialInd == 0)
            return false;

        DateTime startDate = getTrialStartDate();
        if (startDate == null) { // check if doesn't require a trial eval.
            return false;
        }

        DateTime trialEndDate = getEndEvalDate(startDate, "trial");
        return CWSUtil.isWithinPeriod(startDate, trialEndDate);
    }

    /**
     *
     * @return (DateTime) start date of appraisal period of the current year.
     * This day may be in the past of the future.
     * @throws Exception
     */
    public DateTime getNewAnnualStartDate() throws Exception {
        DateTime newStartDate = getInitialEvalStartDate();
        System.out.println("getNewAnnualStartDate() newStartDate = " + newStartDate);

        if (!isWithinInitialPeriod()) {
            System.out.println("is not within initial period");
            newStartDate = newStartDate.plusMonths(annualInd);
            System.out.println("EvalsUtil Current Year = " + EvalsUtil.getToday().getYear());
            newStartDate = newStartDate.withYear(EvalsUtil.getToday().getYear());
        }

        return newStartDate;
    }

    /**
     * Checks whether or not DateTime() - right now is within the first annual evaluation. This
     * is used to figure out if when getNewAnnualStartDate is called and annual_ind = 18,
     * whether we are in the first review period or the 2nd one.
     *
     * @return
     * @throws Exception
     */
    private boolean isWithinInitialPeriod() throws Exception {
        // Calculate the end date of the 1st annual evaluation
        DateTime initialStartDate = getInitialEvalStartDate();
        DateTime endFirstEval = initialStartDate.plusMonths(annualInd);
        System.out.println("isWithinInitialPeriod() " + initialStartDate + " - " + endFirstEval);

        return !initialStartDate.isAfterNow() && endFirstEval.isAfterNow() && noAnnualAppraisals();
    }

    private boolean noAnnualAppraisals() throws Exception {
        for (Appraisal appraisal : appraisals) {
            if (appraisal.getType().equals(Appraisal.TYPE_ANNUAL)) {
                return false;
            }
        }

        return true;
    }

    public DateTime getAnnualStartDateBasedOnJobBeginDate(int year)
    {
        DateTime dt = new DateTime(beginDate).withYear(year).withTimeAtStartOfDay();
        return CWSUtil.getFirstDayOfMonth(dt);
    }

    /**
     *
     * @return a DateTime object representing the start date of the initial annual appraisal period
     */
    public DateTime getInitialEvalStartDate()
    {
        DateTime dt;
        if (evalDate != null) {
            System.out.println("getInitialEvalStartDate() evalDate = " + evalDate);
            dt = new DateTime(evalDate).withTimeAtStartOfDay();
        }
        else {
            System.out.println("getInitialEvalStartDate() beginDate = " + beginDate);
            dt = new DateTime(beginDate).withTimeAtStartOfDay();
        }

        return CWSUtil.getFirstDayOfMonth(dt);
    }

    /**
     *
     * @param startDate     DateTime object
     * @param type: trial, or annual
     * @return
     */
    public DateTime getEndEvalDate(DateTime startDate, String type)
    {
      int interval = 0;
      if (type.equalsIgnoreCase("trial"))
        interval = trialInd;
      else if (startDate.equals(getInitialEvalStartDate())) //first annual appraisal
        interval = annualInd;
      else //annual, but not the first annual
        interval = 12;

      if (interval == 0) //No evaluation needed
        return null;

        return startDate.plusMonths(interval).minusDays(1);
    }

    public String getSignature()
    {
        return "pidm: " + getEmployee().getId() +  ", position: " + getPositionNumber() +
                ", suffix: " + getSuffix();
    }

    /**
     * Returns a job object with fields empty based on the string: pidm_posno_suff.
     *
     * @param jobString     Format is: pidm_posno_suff.
     * @return Job
     */
    public static Job getJobFromString(String jobString) {
        Job job = new Job();
        jobString = StringUtils.trim(jobString);

        if (StringUtils.isBlank(jobString)) {
            return null;
        }

        String[] jobId = StringUtils.split(jobString, "_");
        if (jobId.length != 3) {
            return null;
        }

        int pidm = Integer.parseInt(jobId[0]);
        job.setEmployee(new Employee(pidm));
        job.setPositionNumber(jobId[1]);
        job.setSuffix(jobId[2]);

        return job;
    }

    public String getIdKey() {
        return getEmployee().getId() + "_" + positionNumber + "_" + suffix;
    }

    /**
     * Returns a new Salary based on the current salary data in the Job. Only appointment
     * type allowed to return salary information is Classified IT.
     *
     * @return
     */
    public Salary getSalary() {
        if (!appointmentType.equals(AppointmentType.CLASSIFIED_IT)) {
            return null;
        }

        Salary salary = new Salary();
        salary.setCurrent(this.salaryCurrent);
        salary.setHigh(this.salaryHigh);
        salary.setLow(this.salaryLow);
        salary.setMidPoint(this.salaryMidpoint);
        salary.setSgrpCode(this.salaryGrpCode);

        return salary;
    }

    /**
     * Returns the review period of the job based on the first evaluation. This is used by the
     * professional faculty initiation page, where the employees usually have 1 or no evaluations.
     *
     * @return
     */
    public String getReviewPeriod() {
        return ((Appraisal) getAppraisals().iterator().next()).getReviewPeriod();
    }

    /**
     * Whether or not the job is an appointment type other than: Classified or Classified IT.
     *
     * @return  boolean true if the job is not classified.
     */
    public boolean isUnclassified() {
        return !appointmentType.equals(AppointmentType.CLASSIFIED) &&
                !appointmentType.equals(AppointmentType.CLASSIFIED_IT);
    }

    /**
     * Wrapper for isUnclassified method so that it can be called from jstl.
     */
    public boolean getIsUnclassified() {
        return isUnclassified();
    }
}


package edu.osu.cws.evals.models;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PositionDescription {
    private Integer id;

    private String positionNumber;

    private String universityId;

    private String positionTitle;

    private String jobTitle;

    private String department;

    private String firstName;

    private String lastName;

    private String effectiveDate;

    private String positionApptPercent;

    private String appointmentBasis;

    private String flsaStatus;

    private String jobLocation;

    private String positionDescription;

    private String positionSummary;

    private String decisionMakingGuidelines;

    private String percLeadWorkSuperDuties;

    private String nbrEmplLeadorSupVD;

    private String positionDuties;

    private String positionDutiesCont;

    private String addtlreqqualifs;

    private String preferredQualifications;

    private String crimbckgrndandordmvchkrqrd;

    private String validDriverLicenseRequired;

    private String employementCategory;

    private String workSchedule;

    private Date lastUpdate;

    private Set<PositionDescriptionWork> jobs = new HashSet<PositionDescriptionWork>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    public String getUniversityId() {
        return universityId;
    }

    public void setUniversityId(String universityId) {
        this.universityId = universityId;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getPositionApptPercent() {
        return positionApptPercent;
    }

    public void setPositionApptPercent(String positionApptPercent) {
        this.positionApptPercent = positionApptPercent;
    }

    public String getAppointmentBasis() {
        return appointmentBasis;
    }

    public void setAppointmentBasis(String appointmentBasis) {
        this.appointmentBasis = appointmentBasis;
    }

    public String getFlsaStatus() {
        return flsaStatus;
    }

    public void setFlsaStatus(String flsaStatus) {
        this.flsaStatus = flsaStatus;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public String getPositionDescription() {
        return positionDescription;
    }

    public void setPositionDescription(String positionDescription) {
        this.positionDescription = positionDescription;
    }

    public String getPositionSummary() {
        return positionSummary;
    }

    public void setPositionSummary(String positionSummary) {
        this.positionSummary = positionSummary;
    }

    public String getDecisionMakingGuidelines() {
        return decisionMakingGuidelines;
    }

    public void setDecisionMakingGuidelines(String decisionMakingGuidelines) {
        this.decisionMakingGuidelines = decisionMakingGuidelines;
    }

    public String getPercLeadWorkSuperDuties() {
        return percLeadWorkSuperDuties;
    }

    public void setPercLeadWorkSuperDuties(String percLeadWorkSuperDuties) {
        this.percLeadWorkSuperDuties = percLeadWorkSuperDuties;
    }

    public String getNbrEmplLeadorSupVD() {
        return nbrEmplLeadorSupVD;
    }

    public void setNbrEmplLeadorSupVD(String nbrEmplLeadorSupVD) {
        this.nbrEmplLeadorSupVD = nbrEmplLeadorSupVD;
    }

    public String getPositionDuties() {
        return positionDuties;
    }

    public void setPositionDuties(String positionDuties) {
        this.positionDuties = positionDuties;
    }

    public String getPositionDutiesCont() {
        return positionDutiesCont;
    }

    public void setPositionDutiesCont(String positionDutiesCont) {
        this.positionDutiesCont = positionDutiesCont;
    }

    public String getAddtlreqqualifs() {
        return addtlreqqualifs;
    }

    public void setAddtlreqqualifs(String addtlreqqualifs) {
        this.addtlreqqualifs = addtlreqqualifs;
    }

    public String getPreferredQualifications() {
        return preferredQualifications;
    }

    public void setPreferredQualifications(String preferredQualifications) {
        this.preferredQualifications = preferredQualifications;
    }

    public String getCrimbckgrndandordmvchkrqrd() {
        return crimbckgrndandordmvchkrqrd;
    }

    public void setCrimbckgrndandordmvchkrqrd(String crimbckgrndandordmvchkrqrd) {
        this.crimbckgrndandordmvchkrqrd = crimbckgrndandordmvchkrqrd;
    }

    public String getValidDriverLicenseRequired() {
        return validDriverLicenseRequired;
    }

    public void setValidDriverLicenseRequired(String validDriverLicenseRequired) {
        this.validDriverLicenseRequired = validDriverLicenseRequired;
    }

    public String getEmployementCategory() {
        return employementCategory;
    }

    public void setEmployementCategory(String employementCategory) {
        this.employementCategory = employementCategory;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}

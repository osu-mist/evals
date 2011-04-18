/**
 * POJO to interact with criteria_areas table. It also contains a method to
 * validate the name and sequence fields.
 */

package edu.osu.cws.pass.models;

import java.util.Date;
import java.util.Set;

public class CriterionArea {


    private int id;

    private String name;

    private AppointmentType appointmentTypeID;

    private CriterionArea originalID;

    private int sequence;

    private Date createDate;

    private Employee createdBy;

    private Date deleteDate;

    private Employee deletedBy;

    private Set details;

    public CriterionArea() { }

    /**
     * Method called by util Hibernate classes to validate the name.
     *
     * @return errors
     */
    public boolean validateName() {
        ArrayList<String> nameErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("name");
        if (this.name == null || this.name.equals("")) {
            nameErrors.add("criteria-name-required");
        }

        if (nameErrors.size() > 0) {
            this.errors.put("name", nameErrors);
            return false;
        }
        return true;
    }

    /**
     * Method called by util Hibernate classes to validate the sequence.
     *
     * @return errors
     */
    public boolean validateSequence() {
        ArrayList<String> sequenceErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("sequence");
        if (this.sequence == 0) {
            sequenceErrors.add("criteria-sequence-required");
        } else if (this.sequence < 1) {
            sequenceErrors.add("criteria-sequence-invalid");
        }

        if (sequenceErrors.size() > 0) {
            this.errors.put("sequence", sequenceErrors);
            return false;
        }
        return true;
    }

    /**
     * Method called by util Hibernate classes to make sure that there is a
     * valid appointment type set.
     *
     * @return
     */
    public boolean validateAppointmentTypeID() {
        ArrayList<String> appointmentErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("appointmentType");
        if (this.appointmentTypeID == null) {
            appointmentErrors.add("criteria-appointment-type-required");
        } else if (this.appointmentTypeID.getId() == 0) {
            appointmentErrors.add("criteria-appointment-type-required");
        }

        if (appointmentErrors.size() > 0) {
            this.errors.put("appointmentType", appointmentErrors);
            return false;
        }
        return true;
    }

    /**
     * Returns the most recent criterion_detail. The sorting is done by the
     * db using the createDate field.
     *
     * @return  The most recently created CriterionDetail for the CriterionArea
     */
    public CriterionDetail getCurrentDetail() {
        return (CriterionDetail) details.toArray()[0];
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AppointmentType getAppointmentTypeID() {
        return appointmentTypeID;
    }

    public void setAppointmentTypeID(AppointmentType appointmentTypeID) {
        this.appointmentTypeID = appointmentTypeID;
    }

    public CriterionArea getOriginalID() {
        return originalID;
    }

    public void setOriginalID(CriterionArea originalID) {
        this.originalID = originalID;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public Employee getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Employee deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Set getDetails() {
        return details;
    }

    public void setDetails(Set details) {
        this.details = details;
    }
}

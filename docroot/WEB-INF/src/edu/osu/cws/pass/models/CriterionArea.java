/**
 * POJO to interact with criteria_areas table. It also contains a method to
 * validate the name and sequence fields.
 */

package edu.osu.cws.pass.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CriterionArea extends Pass {


    private int id = 0;

    private String name = "";

    private String appointmentType;

    private CriterionArea originalID;

    private int sequence;

    private Date createDate;

    private Employee createdBy;

    private Date deleteDate;

    private Employee deletedBy;

    private Set details = new HashSet();

    /**
     * Validation error message for name is public because the add.jsp
     * needs to access this static variable in order to do js validation
     */
    public static final String nameRequired =
            "Please enter an area name for the evaluation criteria";

    /**
     * Validation error message for Sequence
     */
    private static final String sequenceRequired =
            "Please provide a sequence for the evaluation criteria";

    /**
     * Validation error message for Sequence
     */
    private static final String sequenceInvalid =
            "Evaluation criteria sequence should be greater than 1";

    /**
     * Validation error message for Sequence
     */
    private static final String appointmentTypeRequired =
            "Please select an appointment type";

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
            nameErrors.add(nameRequired);
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
            sequenceErrors.add(sequenceRequired);
        } else if (this.sequence < 1) {
            sequenceErrors.add(sequenceInvalid);
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
    public boolean validateAppointmentType() {
        ArrayList<String> appointmentErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("appointmentType");
        if (this.appointmentType == null) {
            appointmentErrors.add(appointmentTypeRequired);
        } else if (this.appointmentType.equals("")) {
            appointmentErrors.add(appointmentTypeRequired);
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

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
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

    /**
     * This method is not needed by hibernate, but it is a helper method used to add CriterionDetail
     * to the details HashSet.
     *
     * @param detail
     */
    public void addDetails(CriterionDetail detail) {
        detail.setAreaID(this);
        this.details.add(detail);
    }

}

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
    public String[] validateName() {
        return new String[2];
    }

    /**
     * Method called by util Hibernate classes to validate the sequence.
     *
     * @return errors
     */
    public String[] validateSequence() {
        return new String[2];
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

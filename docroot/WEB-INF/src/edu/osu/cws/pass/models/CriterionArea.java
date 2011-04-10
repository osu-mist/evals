/**
 * POJO to interact with criteria_areas table. It also contains a method to
 * validate the name and sequence fields.
 */

package edu.osu.cws.pass.models;

import java.util.Date;

public class CriterionArea {


    private long id;

    private String name;

    private long employeeTypeID;

    private int sequence;

    private Date created;

    private long createdBy;

    private Date deleted;

    private long deletedBy;

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

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEmployeeTypeID() {
        return employeeTypeID;
    }

    public void setEmployeeTypeID(long employeeTypeID) {
        this.employeeTypeID = employeeTypeID;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public long getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(long deletedBy) {
        this.deletedBy = deletedBy;
    }
}

/**
 * POJO to interact with criteria_area_details table. It also includes validation
 * method for the description field.
 */
package edu.osu.cws.pass.models;

import java.util.Date;

public class CriterionDetail {

    private long id;

    private long areaID;

    private String description;

    private Date created;

    private long createdBy;

    public CriterionDetail() {}

    /**
     * Method called by Hibernate util classes to validate the description.
     *
     * @return errors
     */
    public String[] validateDescription() {
        return new String[2];
    }

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public long getAreaID() {
        return areaID;
    }

    public void setAreaID(long areaID) {
        this.areaID = areaID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}

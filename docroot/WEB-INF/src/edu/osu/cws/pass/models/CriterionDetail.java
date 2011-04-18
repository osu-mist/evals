/**
 * POJO to interact with criteria_area_details table. It also includes validation
 * method for the description field.
 */
package edu.osu.cws.pass.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CriterionDetail extends Pass {

    private int id;

    private CriterionArea areaID;

    private String description;

    private Date createDate;

    private Employee createdBy;

    public CriterionDetail() {}

    /**
     * Method called by Hibernate util classes to validate the description.
     *
     * @return errors
     */
    public String[] validateDescription() {
        return new String[2];
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CriterionArea getAreaID() {
        return areaID;
    }

    public void setAreaID(CriterionArea areaID) {
        this.areaID = areaID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}

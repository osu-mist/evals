/**
 * POJO to interact with criteria_area_details table. It also includes validation
 * method for the description field.
 */
package edu.osu.cws.pass.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CriterionDetail extends Pass {

    private int id = 0;

    private CriterionArea areaID;

    private String description = "";

    private Date createDate;

    private Employee createdBy;

    /**
     * descriptionRequired static variable is public because add.jsp needs to
     * access it to provide js validation
     */
    public static final String descriptionRequired =
            "Please enter an evaluation criteria description";

    public CriterionDetail() {}

    /**
     * Method called by Hibernate util classes to validate the description.
     *
     * @return errors
     */
    public boolean validateDescription() {
        ArrayList<String> descriptionErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("description");
        if (this.description == null || this.description.equals("")) {
            descriptionErrors.add(descriptionRequired);
        }

        if (descriptionErrors.size() > 0) {
            this.errors.put("description", descriptionErrors);
            return false;
        }

        return true;
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

/**
 * POJO to interact with criteria_areas table. It also contains a method to
 * validate the name, appointmentType and description.
 */

package edu.osu.cws.evals.models;

import java.util.*;

public class CriterionArea extends Evals {

    private int id = 0;

    private String name = "";

    private String appointmentType;

    private CriterionArea ancestorID;

    private Date createDate;

    private Employee creator;

    private Date deleteDate;

    private Employee deleter;

    private String description;

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
            nameErrors.add(bundle.getString("criteria-nameRequired"));
        }

        if (nameErrors.size() > 0) {
            this.errors.put("name", nameErrors);
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
            appointmentErrors.add(bundle.getString("criteria-appointmentTypeRequired"));
        } else if (this.appointmentType.equals("")) {
            appointmentErrors.add(bundle.getString("criteria-appointmentTypeRequired"));
        }

        if (appointmentErrors.size() > 0) {
            this.errors.put("appointmentType", appointmentErrors);
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

    public CriterionArea getAncestorID() {
        return ancestorID;
    }

    public void setAncestorID(CriterionArea ancestorID) {
        this.ancestorID = ancestorID;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Employee getCreator() {
        return creator;
    }

    public void setCreator(Employee creator) {
        this.creator = creator;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public Employee getDeleter() {
        return deleter;
    }

    public void setDeleter(Employee deleter) {
        this.deleter = deleter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

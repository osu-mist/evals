package edu.osu.cws.evals.models;


import edu.osu.cws.evals.portlet.ActionHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

public class Admin extends Evals {
    private int id;

    private Employee employee;

    private Employee creator;

    private Date createDate;

    private Date modifiedDate;

    private boolean isMaster;

    /**
     * The value for scope will be: hr, uabac, etc.
     */
    private String scope;

    private static ActionHelper actionHelper;

    /**
     * Check that the employee we are adding as admin is active.
     *
     * @return
     */
    public boolean validateEmployee() {
        ArrayList<String> employeeErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("employee");
        if (this.employee == null || !this.employee.getStatus().equals("A")) {
            employeeErrors.add(getMessage("admin-validEmployeeRequired"));
        }

        if (employeeErrors.size() > 0) {
            this.errors.put("employee", employeeErrors);
            return false;
        }
        return true;
    }

    public Admin() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getCreator() {
        return creator;
    }

    public void setCreator(Employee creator) {
        this.creator = creator;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean getIsMaster() {
        return isMaster;
    }

    public void setIsMaster(boolean master) {
        isMaster = master;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public static String getMessage(String type){
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        return resource.getString(type);
    }
}

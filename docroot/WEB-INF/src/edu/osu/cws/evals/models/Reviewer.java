package edu.osu.cws.evals.models;

import edu.osu.cws.evals.portlet.ActionHelper;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class Reviewer extends Evals {
    private int id;

    private String businessCenterName;

    private Employee employee;

    private static ActionHelper actionHelper;

    public Reviewer() {
    }

    /**
     * Check that the employee we are adding as reviewer is active.
     *
     * @return
     */
    public boolean validateEmployee() {
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        ArrayList<String> employeeErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("employee");
        if (this.employee == null || !this.employee.getStatus().equals("A")) {
            employeeErrors.add(resource.getString("reviewer-validEmployeeRequired"));
        }

        if (employeeErrors.size() > 0) {
            this.errors.put("employee", employeeErrors);
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

    public String getBusinessCenterName() {
        return businessCenterName;
    }

    public void setBusinessCenterName(String businessCenterName) {
        this.businessCenterName = businessCenterName;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}

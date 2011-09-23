package edu.osu.cws.evals.models;

import java.util.ArrayList;

public class Reviewer extends Evals {
    private int id;

    private String businessCenterName;

    private Employee employee;


    public static final String validEmployeeRequired =
            "The username you entered does not exist or is inactive. Please provide a valid employee username.";

    public Reviewer() {
    }

    /**
     * Check that the employee we are adding as reviewer is active.
     *
     * @return
     */
    public boolean validateEmployee() {
        ArrayList<String> employeeErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("employee");
        if (this.employee == null || !this.employee.getStatus().equals("A")) {
            employeeErrors.add(validEmployeeRequired);
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

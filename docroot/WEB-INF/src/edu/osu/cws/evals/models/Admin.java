package edu.osu.cws.evals.models;


import java.util.ArrayList;
import java.util.Date;

public class Admin extends Evals {
    private int id;

    private Employee employee;

    private Employee creator;

    private Date createDate;

    private Date modifiedDate;

    private boolean isMaster;

    public static final String validEmployeeRequired =
            "The username you entered does not exist or is inactive. Please provide a valid employee username.";

    public static final String isMasterCannotBeEmpty =
            "Please select the type of administrator.";

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
            employeeErrors.add(validEmployeeRequired);
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
}

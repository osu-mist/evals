package edu.osu.cws.evals.models;

public class Reviewer extends Evals {
    private int id;

    private String businessCenterName;

    private Employee employee;

    public Reviewer() {
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

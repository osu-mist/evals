package edu.osu.cws.evals.models;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class OptOut {
    public static String TYPE_EVAL = "EVAL";
    public static String TYPE_EMAIL = "EMAIL";

    public static List<String> TYPES = Arrays.asList(TYPE_EVAL, TYPE_EMAIL);

    private int id;

    private Employee employee;

    private String type;

    private Employee creator;
    
    private Date createDate;

    private Employee deleter;

    private Date deleteDate;

    public OptOut() {}

    public OptOut(int id, Employee employee, String type) {
        this.id = id;
        this.employee = employee;
        this.type = type;
    }

    public OptOut(Employee employee, String type, Employee creator) {
        this.employee = employee;
        this.type = type;
        this.creator = creator;
        this.createDate = new Date();
    }

    public Boolean isActive() {
        return deleter == null;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Employee getDeleter() {
        return deleter;
    }

    public void setDeleter(Employee deleter) {
        this.deleter = deleter;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
}

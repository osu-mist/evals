package edu.osu.cws.pass.models;


import java.util.Date;

public class Admin extends Pass {
    private int id;

    private Employee employee;

    private Employee creator;

    private Date createDate;

    private Date modifiedDate;

    private boolean isMaster;

    private boolean isBackupMaster;

    /**
     * The value for scope will be: hr, uabac, etc.
     */
    private String scope;

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

    public boolean getIsBackupMaster() {
        return isBackupMaster;
    }

    public void setIsBackupMaster(boolean backupMaster) {
        isBackupMaster = backupMaster;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}

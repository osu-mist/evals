/**
 * POJO to interact with the employees table.
 */
package edu.osu.cws.pass.models;

public class Employee {
    private long pidm;

    private String firstName;

    private String middleName;

    private String lastName;

    private String osuid;

    private String onid;

    private String preferredEmail;

    private String copySupervisor;

    private boolean active;

    public long getPidm() {
        return pidm;
    }

    private void setPidm(long pidm) {
        this.pidm = pidm;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOsuid() {
        return osuid;
    }

    public void setOsuid(String osuid) {
        this.osuid = osuid;
    }

    public String getOnid() {
        return onid;
    }

    public void setOnid(String onid) {
        this.onid = onid;
    }

    public String getPreferredEmail() {
        return preferredEmail;
    }

    public void setPreferredEmail(String preferredEmail) {
        this.preferredEmail = preferredEmail;
    }

    public String getCopySupervisor() {
        return copySupervisor;
    }

    public void setCopySupervisor(String copySupervisor) {
        this.copySupervisor = copySupervisor;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

/**
 * POJO to interact with the employees table.
 */
package edu.osu.cws.pass.models;

public class Employee extends Pass {
    private int id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String osuid;

    private String onid;

    private String email;

    private Boolean copySupervisor;

    private Boolean active;

    public String getName() {
        return lastName + ", " + firstName;
    }

    public Employee() { }

    public int getId() {
        return id;
    }

    private void setId(int pidm) {
        this.id = pidm;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getCopySupervisor() {
        return copySupervisor;
    }

    public void setCopySupervisor(Boolean copySupervisor) {
        this.copySupervisor = copySupervisor;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

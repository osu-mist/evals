/**
 * POJO to interact with the employees table.
 */
package edu.osu.cws.pass.models;

import java.util.HashSet;
import java.util.Set;

public class Employee extends Pass {
    private int id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String osuid;

    private String onid;

    private String email;

    /**
     * Possible values of status are:
     * A - active,
     * L - leave,
     * T - terminated
     */
    private String status;

    private Set jobs = new HashSet();

    public String getName() {
        lastName = (lastName == null)? "" : lastName;
        firstName = (firstName == null)? "" : firstName;

        return lastName + ", " + firstName;
    }

    public Employee() { }

    public Employee(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int pidm) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set getJobs() {
        return jobs;
    }

    public void setJobs(Set jobs) {
        this.jobs = jobs;
    }

    public String getConventionName()
    {
        return (firstName + " " + lastName);
    }
}

/**
 * POJO to interact with appointment_types table. This table is
 * sometimes referenced as employment_type. Some sample
 * appointment types are: classified, classified it.
 */
package edu.osu.cws.pass.models;

public class AppointmentType {
    private long id;

    private String name;

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

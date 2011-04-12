/**
 * POJO to interact with the business_centers table.
 */
package edu.osu.cws.pass.models;

public class BusinessCenter {
    private int id;

    private String name;

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

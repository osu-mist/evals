/**
 * POJO to interact with the business_centers table.
 */
package edu.osu.cws.pass.models;

public class BusinessCenter {
    private long id;

    private String name;

    private long reviewerID;

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

/**
 * POJO to interact with business centers.
 */
package edu.osu.cws.pass.models;


public class BusinessCenter extends Pass {

    private String name;

    public BusinessCenter() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

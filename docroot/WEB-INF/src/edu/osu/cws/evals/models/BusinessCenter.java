/**
 * POJO to interact with business centers.
 */
package edu.osu.cws.evals.models;


public class BusinessCenter extends Evals {

    private String name;

    public BusinessCenter() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

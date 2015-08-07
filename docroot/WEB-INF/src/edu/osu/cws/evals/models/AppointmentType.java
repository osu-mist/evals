/**
 * POJO to interact with appointment types. This class is
 * sometimes referred as employment_type. Some sample
 * appointment types are: classified, classified it. This class
 * does not include logic, only static fields to reference the
 * names of the appointment types.
 */
package edu.osu.cws.evals.models;


public class AppointmentType extends Evals {
    public static final String CLASSIFIED = "Classified";

    public static final String CLASSIFIED_IT = "Classified IT";

    public static final String PROFESSIONAL_FACULTY = "Professional Faculty";

    public static final String RANKED_FACULTY = "Ranked Faculty";

    private String name;

    public AppointmentType() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

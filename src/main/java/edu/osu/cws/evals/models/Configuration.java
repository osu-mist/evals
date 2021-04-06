package edu.osu.cws.evals.models;

/**
 * POJO used to represent a single configuration row in the db.
 */
public class Configuration extends Evals {

    private int id;

    /**
     * Used to group the configurations together when displaying them
     * in a list for admin users. This holds a resource bundle key
     */
    private String section;

    /**
     * Name of the configuration. It holds a resource bundle key
     */
    private String name;


    /**
     * Sequence used to sort the configuration items within a section.
     */
    private int sequence;

    private String value;

    /**
     * Used by the backend processes to figure out how to use the configuration
     * values in operations.
     */
    private String referencePoint;

    private String action;

    private String appointmentType;

    public Configuration() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getReferencePoint() {
        return referencePoint;
    }

    public void setReferencePoint(String referencePoint) {
        this.referencePoint = referencePoint;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public int getIntValue() throws  NumberFormatException
    {
        return Integer.parseInt(value);
    }

}

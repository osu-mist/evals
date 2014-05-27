package edu.osu.cws.evals.models;

public class Rating extends Evals implements Comparable<Rating> {
    private Integer id;

    private Integer rate;

    private String description;

    private String appointmentType;

    public Rating() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    @Override
    public int compareTo(Rating otherRating) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this.rate > otherRating.rate) {
            return AFTER;
        }

        if (this.rate < otherRating.rate) {
            return BEFORE;
        }

        return EQUAL;
    }
}

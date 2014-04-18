package edu.osu.cws.evals.models;


import java.io.Serializable;

public class LeadResponsibility implements Serializable{
    private Integer positionDescriptionId;
    private String response;

    public Integer getPositionDescriptionId() {
        return positionDescriptionId;
    }

    public void setPositionDescriptionId(Integer positionDescriptionId) {
        this.positionDescriptionId = positionDescriptionId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}

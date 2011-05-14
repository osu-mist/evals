package edu.osu.cws.pass.models;

public class EmailType extends Pass {
    private String type;

    private String recipients;

    public EmailType() { }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }
}

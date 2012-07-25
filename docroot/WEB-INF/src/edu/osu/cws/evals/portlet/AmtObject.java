package edu.osu.cws.evals.portlet;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 7/19/12
 * Time: 9:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class AmtObject {
    public AmtObject(String appointment, String posn, String elcs) {

        Appointment = appointment;
        Posn = posn;
        Elcs = elcs;
    }

    private String Appointment;

    private String Posn;

    private String Elcs;







    public String getAppointment() {
        return Appointment;
    }

    public void setAppointment(String appointment) {
        Appointment = appointment;
    }

    public String getPosn() {
        return Posn;
    }

    public void setPosn(String posn) {
        Posn = posn;
    }

    public String getElcs() {
        return Elcs;
    }

    public void setElcs(String elcs) {
        Elcs = elcs;
    }
}

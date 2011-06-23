package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.AppointmentTypeMgr;
import edu.osu.cws.pass.models.AppointmentType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class AppointmentTypesTest {

    AppointmentTypeMgr appointmentTypeMgr = new AppointmentTypeMgr();

    @BeforeMethod
    public void initializeObjects() throws Exception {
        appointmentTypeMgr = new AppointmentTypeMgr();
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    @Test(groups = {"unittest"})
    public void testList() throws Exception {
        List types = appointmentTypeMgr.list();
        assert types.size() == 1 : "Invalid number of appointment types.";
        AppointmentType type = (AppointmentType) types.get(0);
        assert type.getName().equals("Classified") : "Invalid appointment type";
    }
}

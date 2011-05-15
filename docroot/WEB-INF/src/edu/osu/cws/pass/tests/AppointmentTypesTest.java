package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.models.AppointmentType;
import edu.osu.cws.pass.util.AppointmentTypes;
import edu.osu.cws.pass.util.HibernateUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class AppointmentTypesTest {

    AppointmentTypes appointmentTypes = new AppointmentTypes();

    @BeforeClass
    public void setUp() {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
    }

    @BeforeMethod
    public void initializeObjects() {
        appointmentTypes = new AppointmentTypes();
        setUp();
    }

    @Test(groups = {"unittest"})
    public void testList() {
        List types = appointmentTypes.list();
        assert types.size() == 1 : "Invalid number of appointment types.";
        AppointmentType type = (AppointmentType) types.get(0);
        assert type.getName().equals("Classified") : "Invalid appointment type";
    }
}

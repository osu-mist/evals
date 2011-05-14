package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.util.Appraisals;
import edu.osu.cws.pass.util.Employees;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class AppraisalsTest {

    Appraisal appraisal = new Appraisal();
    Appraisals appraisals = new Appraisals();
    Employees employees = new Employees();
    Employee employee = new Employee();
    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeClass
    public void setUp() {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
        DBUnit dbunit = new DBUnit();
        try {
            dbunit.seedDatabase();
        } catch (Exception e) {}
    }

    /**
     * This method tests that the Appraisals class can create an appraisal given a Job object.
     *
     * @throws Exception
     */
    @Test(groups = {"unittest"}, dataProvider = "job")
    public void shouldCreateAnAppraisal(Job job) throws Exception {
        assert appraisals.createAppraisal(job) != 0 :
                "Appraisals.createAppraisal should return id of appraisal";
    }

    @Test(groups = {"unittest"},  expectedExceptions = ModelException.class)
    public void appraisalShouldRequireValidJob() throws Exception {
        Job invalidJob = new Job();

        assert appraisals.createAppraisal(invalidJob) != 0 :
                "Appraisals.createAppraisal should require valid Job";
    }

    /**
     * TestNG Dataprovider, returns an array of Jobs to be used in this test class.
     * @return
     */
    @DataProvider(name = "job")
    public Object[][] loadJob() {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, 1);
        tx.commit();

        return new Object[][] {
                {job}
        };
    }

    /**
     * TestNG Dataprovider, returns an array of fields to update and appraisals.
     * @return
     */
//    @DataProvider(name = "job")
//    public Object[][] loadAppraisalSaveList() throws ModelException {
//        Session hsession = HibernateUtil.getCurrentSession();
//        Transaction tx = hsession.beginTransaction();
//        Job job = (Job) hsession.load(Job.class, 1);
//        Appraisal appraisal = (Appraisal) hsession.load(Appraisal.class,
//                appraisals.createAppraisal(job));
//        tx.commit();
//        employee = employees.findByOnid("luf");
//
//        String[] list1 = {"goalsComments", "goalApproverID", "goalApprovedDate"};
//
//        return new Object[][] {
//                {appraisal, list1}
//        };
//    }
    @Test(groups = {"unitttest"})
    public void shouldUpdateFieldsInList(){

    }
}

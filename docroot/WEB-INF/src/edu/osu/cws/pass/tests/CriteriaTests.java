/**
 * Tests the Criteria model methods.
 */
package edu.osu.cws.pass.tests;

import com.liferay.portal.kernel.dao.orm.Criterion;
import edu.osu.cws.pass.models.AppointmentType;
import edu.osu.cws.pass.models.CriterionArea;
import edu.osu.cws.pass.models.CriterionDetail;


import edu.osu.cws.pass.models.Employee;
import edu.osu.cws.pass.util.Criteria;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.*;

@Test
public class CriteriaTests {

    CriterionArea criterionObject;
    CriterionDetail criteriaDetailObject;
    Criteria criteriaObject;

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeClass
    public void setUp() {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
    }

    /**
     * This method is run before each one of the test methods gets executed. It
     * just resets the various criteria objects used in the tests to new values.
     */
    @BeforeMethod
    public void initializeObjects() {
        criterionObject = new CriterionArea();
        criteriaDetailObject = new CriterionDetail();
        criteriaObject = new Criteria();
    }

    /**
     * Tests that fetching a list of active criteria for classified employees
     * works correctly. It checks that the correct associated CriterionDetail
     * are fetched as well.
     */
    @Test(groups = {"unittest"})
    public void returnActiveCriteriaForClassified() {
        List activeCriteriaList = criteriaObject.list(Criteria.DEFAULT_APPOINTMENT_TYPE);
        CriterionArea expectedCriteria = new CriterionArea();
        CriterionArea expectedCriteria2 = new CriterionArea();
        CriterionDetail expectedDetails = new CriterionDetail();
        CriterionDetail expectedDetails2 = new CriterionDetail();

        CriterionArea fakeCriteria;
        CriterionArea dbCriteria;
        CriterionDetail fakeDetails;
        CriterionDetail dbDetails;

        AppointmentType type = new AppointmentType();
        type.setName("Classified");

        expectedCriteria.setId(1);
        expectedCriteria.setName("COMMUNICATION SKILLS");
        expectedCriteria.setAppointmentTypeID(type);
        expectedCriteria.setSequence(1);
        expectedCriteria.setCreatedBy(new Employee());
        expectedCriteria.setDetails(new HashSet<CriterionDetail>());
        expectedDetails.setId(1);
        expectedDetails.setDescription("How will you improve your communication?");
        expectedCriteria.getDetails().add(expectedDetails);

        expectedCriteria2.setId(2);
        expectedCriteria2.setName("TECHNICAL SKILLS");
        expectedCriteria2.setAppointmentTypeID(type);
        expectedCriteria2.setSequence(2);
        expectedCriteria2.setCreatedBy(new Employee());
        expectedCriteria2.setDetails(new HashSet<CriterionDetail>());
        expectedDetails2.setId(2);
        expectedDetails2.setDescription("What training will you obtain this year?");
        expectedCriteria2.getDetails().add(expectedDetails2);

        ArrayList expected = new ArrayList<CriterionArea>();
        expected.add(expectedCriteria);
        expected.add(expectedCriteria2);

        Assert.assertEquals(expected.size(), activeCriteriaList.size());
        for (int i = 0; i < expected.size(); i++) {
            fakeCriteria = (CriterionArea) expected.get(i);
            dbCriteria = (CriterionArea) activeCriteriaList.get(i);
            Assert.assertEquals(fakeCriteria.getId(), dbCriteria.getId());

            fakeDetails = (CriterionDetail) fakeCriteria.getDetails().toArray()[0];
            dbDetails = (CriterionDetail) fakeCriteria.getDetails().toArray()[0];
            Assert.assertEquals(fakeDetails.getId(), dbDetails.getId());
        }
    }

    /**
     * Tests to make sure that when loading a CriterionArea from the db it loads the right number
     * of CriteironDetail and that the getCurrentDetail method returns the most recent detail
     * added.
     */
    @Test(groups = {"unittest"})
    public void returnCurrentDetailsForCriterion() {
        Session hsession = null;

        hsession = HibernateUtil.getSessionFactory().getCurrentSession();
        hsession.beginTransaction();
        CriterionArea result = (CriterionArea) hsession.load(CriterionArea.class, 1);

        Assert.assertEquals(2, result.getDetails().size());
        Assert.assertEquals(result.getCurrentDetail().getDescription(),
                "How will you increase your communication skills?");
    }

    /**
     * Tests that saving a new Criteria object works correctly. The save method returns boolean
     * based on success of the operation.
     */
/*    @Test(groups = {"unittest", "pending"})
    public void addNewCriteria() {
        criteriaObject.setName("Communication");
        criteriaDetailObject.setDescription("How do you plan to improve your communication skills?");
        criteriaObject.setAppointmentTypeID(new AppointmentType());
        criteriaObject.setCreatedBy(new Employee());

        //Assert.assertTrue(criteriaObject.save());
        //Assert.assertTrue(criteriaDetailObject.save());
    }*/

    /**
     * Tests that after editing a field in the
     */
/*
    @Test(groups = {"unittest", "pending"})
    public void editCriteriaShouldCreateRevision() {
        long newRevisionId = 0;
        long oldRevisionId = 1;
        //@todo: get criteria object with id = 3
        //criteriaObject.read((long) 3);

//        oldRevisionId = criteriaObject.getCriteriaDetailsID();

        criteriaDetailObject.setDescription("What is your plan to improve communication skills?");
        //criteriaDetailObject.save();


        //newRevisionId = criteriaObject.getCriteriaDetailsID();

        assertFalse(oldRevisionId == newRevisionId);
    }
*/

    /**
     * Tests that we can edit a criteria's property and the criteria ids remain the same
     * thus propagating the changes to the existing assessments.
     */
/*    @Test(groups = {"unittest", "pending"})
    public void editTypoInCriteria() {
//        criteriaObject.read((long) 3);
        long newRevisionId = 0;
        long oldRevisionId = 0;

//        long oldRevisionId = criteriaObject.getCriteriaDetailsID();

        criteriaDetailObject.setDescription("What is your plan to improve communication skills?");
//        criteriaDetailObject.setPropagateChanges(true);
//        criteriaObject.save();

//        long newRevisionId = criteriaObject.getCriteriaDetailsID();

        Assert.assertEquals(oldRevisionId,newRevisionId);
    }*/


    /**
     * Tests that we can enable and disable a given criteria.
     */
/*    @Test(groups = {"unittest", "pending"})
    public void disableCriteria() {
//        criteriaObject.read((long) 1);
//        Assert.assertEquals(criteriaObject.isDisabled(), false);
        criteriaObject.setDeleteDate(new Date());
//        criteriaObject.save();

//        criteriaObject.read(1);
//        Assert.assertEquals(criteriaObject.isDisabled(), true);
    }*/
}
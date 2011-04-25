/**
 * Tests the Criteria model methods.
 */
package edu.osu.cws.pass.tests;

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

        Assert.assertEquals(activeCriteriaList.size(), expected.size());
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
        Session hsession = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        CriterionArea result = (CriterionArea) hsession.load(CriterionArea.class, 1);
        tx.commit();

        Assert.assertEquals(result.getDetails().size(), 2);
        Assert.assertEquals(result.getCurrentDetail().getDescription(),
                "How will you increase your communication skills?");
    }


    /**
     * Tests that criteria area names are required.
     */
    @Test(groups = {"unittest"})
    public void shouldRequireAName() {
        criterionObject.setName(null);
        assert !criterionObject.validateName() : "Name should be required";
        assert criterionObject.getErrors().containsKey("name") : "Missing name error msg";

        criterionObject.setName("");
        assert !criterionObject.validateName() : "Name should be required";
        assert criterionObject.getErrors().containsKey("name") : "Missing name error msg";

        criterionObject.setName("technical skills");
        assert criterionObject.validateName() : "Name is provided, validation should pass";
        assert !criterionObject.getErrors().containsKey("name") : "No name error msg should be present";
    }

    /**
     * Tests that criteria sequence is required.
     */
    @Test(groups = {"unittest"})
    public void shouldRequireASequence() {
        assert !criterionObject.validateSequence() : "Sequence should be required";
        assert criterionObject.getErrors().containsKey("sequence") : "Missing sequence error msg";

        criterionObject.setSequence(-1);
        assert !criterionObject.validateSequence(): "Sequence should be greater than 1";
        assert criterionObject.getErrors().containsKey("sequence") : "Missing sequence error msg";


        criterionObject.setSequence(1);
        assert criterionObject.validateSequence(): "Sequence should validate";
        assert !criterionObject.getErrors().containsKey("sequence") : "No sequence error msg should be present";
    }

    /**
     * Tests that a valid appointment type is associated to the CriterionArea Object.
     */
    @Test(groups = {"unittest"})
    public void shouldRequireAppointmentType() {
        assert !criterionObject.validateAppointmentTypeID() :
                "A valid appointment type should be required";
        assert criterionObject.getErrors().containsKey("appointmentType") :
                "Missing sequence error msg";

        AppointmentType type = new AppointmentType();
        criterionObject.setAppointmentTypeID(type);
        assert !criterionObject.validateAppointmentTypeID():
                "A valid appointment type should be required";
        assert criterionObject.getErrors().containsKey("appointmentType") :
                "Missing sequence error msg";

        type.setId(1);
        type.setName("Classified");
        criterionObject.setAppointmentTypeID(type);
        assert criterionObject.validateAppointmentTypeID():
                "Appointment type should validate";
        assert !criterionObject.getErrors().containsKey("appointmentType") :
                "No appointmentType error msg should be present";

    }

    /**
     * Tests that a valid description is given to the CriterionDetail object.
     */
    @Test(groups = {"unitttest"})
    public void shouldRequireDescription() {
        assert !criteriaDetailObject.validateDescription() :
                "A valid description should be required";
        assert criteriaDetailObject.getErrors().containsKey("description") :
                "Missing description error msg";

//        criteriaDetailObject.setDescription("");
//        criteriaDetailObject.validateDescription() :
//                "A valid description should be required";
//        criteriaDetailObject.getErrors().containsKey("")


    }

    @Test(groups={"unittest"})
    public void shouldValidateAllFields() {
        Session hsession = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Employee createdBy = (Employee) hsession.load(Employee.class, 12345);
        AppointmentType type = (AppointmentType) hsession.load(AppointmentType.class, 1);
        tx.commit();

        criterionObject.setName("");
        criteriaDetailObject.setDescription("");
        criteriaDetailObject.setCreatedBy(createdBy);
        criterionObject.setAppointmentTypeID(type);
        criterionObject.setSequence(1);
        criterionObject.setCreatedBy(createdBy);
        assert !criterionObject.validate() : "All fields in CriterionArea should check validation";
        assert !criteriaDetailObject.validate() : "All fields in CriterionDetail should check validation";

    }

    /**
     * Tests that saving a new Criteria object works correctly. The save method returns boolean
     * based on success of the operation.
     */
    @Test(groups = {"unittest"})
    public void addNewCriteria() {
        Session hsession = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Employee createdBy = (Employee) hsession.load(Employee.class, 12345);
        AppointmentType type = (AppointmentType) hsession.load(AppointmentType.class, 1);
        tx.commit();

        criterionObject.setName("Communication");
        criteriaDetailObject.setDescription("How do you plan to improve your communication skills?");
        criteriaDetailObject.setCreatedBy(createdBy);
        criterionObject.setAppointmentTypeID(type);
        criterionObject.setSequence(1);
        criterionObject.setCreatedBy(createdBy);

        assert criteriaObject.add(criterionObject, criteriaDetailObject) : "Valid data should save";
    }

    @Test (groups = {"unittest"})
    public void shouldReturnNextAvailableSequence() {

        assert criteriaObject.getNextSequence(Criteria.DEFAULT_APPOINTMENT_TYPE) == 5 :
                "Incorrect calculation of next sequence";

    }

    /**
     * Tests that after editing a field in the
     */
/*
    @Test(groups = {"unittest", "pending"})
    public void editCriteriaShouldCreateRevision() {
        long newRevisionId = 0;
        long oldRevisionId = 1;
        //@todo: get criteria object with id = 3
        //criterionObject.read((long) 3);

//        oldRevisionId = criterionObject.getCriteriaDetailsID();

        criteriaDetailObject.setDescription("What is your plan to improve communication skills?");
        //criteriaDetailObject.save();


        //newRevisionId = criterionObject.getCriteriaDetailsID();

        assertFalse(oldRevisionId == newRevisionId);
    }
*/

    /**
     * Tests that we can edit a criteria's property and the criteria ids remain the same
     * thus propagating the changes to the existing assessments.
     */
/*    @Test(groups = {"unittest", "pending"})
    public void editTypoInCriteria() {
//        criterionObject.read((long) 3);
        long newRevisionId = 0;
        long oldRevisionId = 0;

//        long oldRevisionId = criterionObject.getCriteriaDetailsID();

        criteriaDetailObject.setDescription("What is your plan to improve communication skills?");
//        criteriaDetailObject.setPropagateChanges(true);
//        criterionObject.save();

//        long newRevisionId = criterionObject.getCriteriaDetailsID();

        Assert.assertEquals(oldRevisionId,newRevisionId);
    }*/


    /**
     * Tests that we can enable and disable a given criteria.
     */
/*    @Test(groups = {"unittest", "pending"})
    public void disableCriteria() {
//        criterionObject.read((long) 1);
//        Assert.assertEquals(criterionObject.isDisabled(), false);
        criterionObject.setDeleteDate(new Date());
//        criterionObject.save();

//        criterionObject.read(1);
//        Assert.assertEquals(criterionObject.isDisabled(), true);
    }*/
}
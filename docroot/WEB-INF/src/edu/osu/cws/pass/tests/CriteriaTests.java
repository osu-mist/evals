/**
 * Tests the Criteria model methods.
 */
package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.CriteriaMgr;
import edu.osu.cws.pass.hibernate.EmployeeMgr;
import edu.osu.cws.pass.models.*;


import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.*;
import java.util.*;

@Test
public class CriteriaTests {

    CriterionArea criterionObject = new CriterionArea();
    CriterionDetail criteriaDetailObject = new CriterionDetail();
    CriteriaMgr criteriaMgrObject = new CriteriaMgr();

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
//    @BeforeClass
    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    /**
     * This method is run before each one of the test methods gets executed. It
     * just resets the various criteria objects used in the tests to new values.
     */
    @BeforeMethod
    public void initializeObjects() throws Exception {
        criterionObject = new CriterionArea();
        criteriaDetailObject = new CriterionDetail();
        criteriaMgrObject = new CriteriaMgr();
        setUp();
    }

    /**
     * TestNG Dataprovider, returns an array of CriterionArea and CriteriondDetailObject
     *
     * @return
     */
    @DataProvider(name = "criteria")
    public Object[][] createData1() throws Exception {
        initializeObjects();
        String type = AppointmentType.CLASSIFIED;
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Employee creator = (Employee) hsession.load(Employee.class, 12345);
        tx.commit();

        criterionObject.setName("Some valid name");
        criteriaDetailObject.setDescription("Some valid description");
        criteriaDetailObject.setCreator(creator);
        criterionObject.setAppointmentType(type);
        criterionObject.setSequence(1);
        criterionObject.setCreator(creator);

        return new Object[][] {
                { criterionObject, criteriaDetailObject }
        };
    }

    /**
     * Tests that fetching a list of active criteria for classified employees
     * works correctly. It checks that the correct associated CriterionDetail
     * are fetched as well.
     */
    @Test(groups = {"unittest"})
    public void returnActiveCriteriaForClassified() throws ModelException, Exception {
        setUp();
        List activeCriteriaList = criteriaMgrObject.list(CriteriaMgr.DEFAULT_APPOINTMENT_TYPE);
        CriterionArea expectedCriteria = new CriterionArea();
        CriterionArea expectedCriteria2 = new CriterionArea();
        CriterionDetail expectedDetails = new CriterionDetail();
        CriterionDetail expectedDetails2 = new CriterionDetail();

        CriterionArea fakeCriteria;
        CriterionArea dbCriteria;
        CriterionDetail fakeDetails;
        CriterionDetail dbDetails;

        String type = AppointmentType.CLASSIFIED;

        expectedCriteria.setId(1);
        expectedCriteria.setName("COMMUNICATION SKILLS");
        expectedCriteria.setAppointmentType(type);
        expectedCriteria.setSequence(1);
        expectedCriteria.setCreator(new Employee());
        expectedCriteria.setDetails(new HashSet<CriterionDetail>());
        expectedDetails.setId(1);
        expectedDetails.setDescription("How will you improve your communication?");
        expectedCriteria.getDetails().add(expectedDetails);

        expectedCriteria2.setId(2);
        expectedCriteria2.setName("TECHNICAL SKILLS");
        expectedCriteria2.setAppointmentType(type);
        expectedCriteria2.setSequence(2);
        expectedCriteria2.setCreator(new Employee());
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
        Session hsession = HibernateUtil.getCurrentSession();
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
        criterionObject.setSequence(0);
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
        criterionObject.setAppointmentType(null);
        assert !criterionObject.validateAppointmentType() :
                "A valid appointment type should be required";
        assert criterionObject.getErrors().containsKey("appointmentType") :
                "Missing sequence error msg";

        String type = AppointmentType.CLASSIFIED;
        criterionObject.setAppointmentType("");
        assert !criterionObject.validateAppointmentType():
                "A valid appointment type should be required";
        assert criterionObject.getErrors().containsKey("appointmentType") :
                "Missing sequence error msg";

        criterionObject.setAppointmentType(type);
        assert criterionObject.validateAppointmentType():
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
    }

    @Test(groups={"unittest"}, expectedExceptions = {ModelException.class}, dataProvider = "criteria")
    public void shouldValidateAllAreaFields(CriterionArea area, CriterionDetail details)
            throws ModelException {

        area.setName("");
        area.setSequence(0);
        area.setAppointmentType(AppointmentType.CLASSIFIED);
        assert !area.validate() : "All fields in CriterionArea should check validation";
    }

    /**
     * @param area
     * @param details
     * @throws ModelException
     */
    @Test(groups={"unittest"}, expectedExceptions = {ModelException.class}, dataProvider = "criteria")
    public void shouldValidateAllCriteriaDetails(CriterionArea area, CriterionDetail details)
            throws ModelException {

        details.setDescription("");
        assert !details.validate() : "All fields in CriterionDetail should check validation";

    }

    /**
     * Tests that saving a new Criteria object works correctly. The save method returns boolean
     * based on success of the operation.
     */
    @Test(groups = {"unittest"})
    public void addNewCriteria() throws Exception {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Employee createdBy = (Employee) hsession.load(Employee.class, 12345);
        String type = AppointmentType.CLASSIFIED;
        tx.commit();

        criterionObject.setName("Communication");
        criteriaDetailObject.setDescription("How do you plan to improve your communication skills?");
        criterionObject.setAppointmentType(type);
        criterionObject.setSequence(1);

        assert criteriaMgrObject.add(criterionObject, criteriaDetailObject, createdBy) :
                    "Valid data should save";
    }

    @Test (groups = {"unittest"})
    public void shouldReturnNextAvailableSequence() throws Exception {
        setUp();
        assert criteriaMgrObject.getNextSequence(CriteriaMgr.DEFAULT_APPOINTMENT_TYPE) == 3 :
                "Incorrect calculation of next sequence";

    }

    /**
     * Tests that when only the criteria name is modified in the editCriteria action,
     * it creates two pojos, and the old criteria is set to deleted.
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void editOnlyCriteriaNameShouldCreatesTwoNewPOJOs() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        String newCriterionName = "New Name For Criteria";
        request.put("name", new String[] {newCriterionName});
        request.put("criterionAreaId", new String[] {"1"});
        request.put("description", new String[] {"How will you increase your communication skills?"});

        int id = 1;
        EmployeeMgr employeeMgr = new EmployeeMgr();
        Employee employee = employeeMgr.findByOnid("cedenoj", null);
        CriterionArea criterionArea =  criteriaMgrObject.get(id);

        // grab old ids and properties to compare
        int oldCriterionAreaID = criterionArea.getId();
        int oldCriterionDetailID = criterionArea.getCurrentDetail().getId();
        int oldSequence = criterionArea.getSequence();

        criteriaMgrObject.edit(request, id, employee);

        // Double check that the deleted properties were set on pojo
        criterionArea =  criteriaMgrObject.get(id);
        assert criterionArea.getDeleteDate() != null : "Should have set deletedDate in old pojo";
        assert criterionArea.getDeleter() != null : "Should have set deleter in old pojo";

        // Sequence should not change
        assert criterionArea.getSequence() == oldSequence : "Sequence should not change";

        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            criterionArea = (CriterionArea) session.
                    createQuery("from edu.osu.cws.pass.models.CriterionArea WHERE name = :name")
                    .setString("name", newCriterionName).list().get(0);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        // Checks that two new pojos were created
        assert oldCriterionAreaID != criterionArea.getId() :
                "should have created a new criteria pojo";
        assert oldCriterionDetailID != criterionArea.getCurrentDetail().getId() :
                "should have created a new criteria detail pojo";
        assert oldSequence == criterionArea.getSequence() : "Sequence should not change";
    }

    /**
     * Tests that when only the criteria description is modified in the editCriteria action,
     * it creates one pojo for the description
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void editOnlyCriteriaDescriptionShouldOnlyCreateNewDescriptionPOJO() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        String newCriterionName = "COMMUNICATION SKILLS";
        request.put("name", new String[] {newCriterionName});
        request.put("criterionAreaId", new String[] {"1"});
        String newCriteriaDetail = "New Value for Criteria Description";
        request.put("description", new String[] {newCriteriaDetail});

        int id = 1;
        EmployeeMgr employeeMgr = new EmployeeMgr();
        Employee employee = employeeMgr.findByOnid("cedenoj", null);
        CriterionArea criterionArea =  criteriaMgrObject.get(id);

        // grab old ids and properties to compare
        int oldCriterionAreaID = criterionArea.getId();
        int oldCriterionDetailID = criterionArea.getCurrentDetail().getId();
        int oldSequence = criterionArea.getSequence();

        criteriaMgrObject.edit(request, id, employee);

        // Double check that the deleted properties were set on pojo
        criterionArea =  criteriaMgrObject.get(id);
        assert criterionArea.getDeleteDate() == null : "Should not have set deletedDate in old pojo";
        assert criterionArea.getDeleter() == null : "Should not have set deleter in old pojo";

        // Sequence should not change
        assert criterionArea.getSequence() == oldSequence : "Sequence should not change";

        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            criterionArea = (CriterionArea) session.
                    createQuery("from edu.osu.cws.pass.models.CriterionArea WHERE name = :name")
                    .setString("name", newCriterionName).list().get(0);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        // Checks that two new pojos were created
        assert oldCriterionAreaID == criterionArea.getId() :
                "should not have created a new criteria pojo";
        assert oldCriterionDetailID != criterionArea.getCurrentDetail().getId() :
                "should have created a new criteria detail pojo";
        assert oldSequence == criterionArea.getSequence() : "Sequence should not change";
        assert criterionArea.getCurrentDetail().getDescription().equals(newCriteriaDetail);
    }

    /**
     * Tests that when both criteria name and description are modified in the editCriteria action,
     * it creates two pojos, and the old criteria is set to deleted.
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void editBothCriteriaNameAndDescriptionShouldCreatesTwoNewPOJOs() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        String newCriterionName = "New Name For Criteria";
        request.put("name", new String[] {newCriterionName});
        request.put("criterionAreaId", new String[] {"1"});
        String newDetailDescription = "New Value for Criteria Description";
        request.put("description", new String[] {newDetailDescription});

        int id = 1;
        EmployeeMgr employeeMgr = new EmployeeMgr();
        Employee employee = employeeMgr.findByOnid("cedenoj", null);
        CriterionArea criterionArea =  criteriaMgrObject.get(id);

        // grab old ids and properties to compare
        int oldCriterionAreaID = criterionArea.getId();
        int oldCriterionDetailID = criterionArea.getCurrentDetail().getId();
        int oldSequence = criterionArea.getSequence();

        criteriaMgrObject.edit(request, id, employee);

        // Double check that the deleted properties were set on pojo
        criterionArea =  criteriaMgrObject.get(id);
        assert criterionArea.getDeleteDate() != null : "Should have set deletedDate in old pojo";
        assert criterionArea.getDeleter() != null : "Should have set deleter in old pojo";

        // Sequence should not change
        assert criterionArea.getSequence() == oldSequence : "Sequence should not change";

        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            criterionArea = (CriterionArea) session.
                    createQuery("from edu.osu.cws.pass.models.CriterionArea WHERE name = :name")
                    .setString("name", newCriterionName).list().get(0);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        // Checks that two new pojos were created
        assert oldCriterionAreaID != criterionArea.getId() :
                "should have created a new criteria pojo";
        assert oldCriterionDetailID != criterionArea.getCurrentDetail().getId() :
                "should have created a new criteria detail pojo";
        assert oldSequence == criterionArea.getSequence() : "Sequence should not change";
        assert criterionArea.getName().equals(newCriterionName) : "Should have updated name";
        assert criterionArea.getCurrentDetail().
                getDescription().equals(newDetailDescription);
    }

    /**
     * Tests that when only the criteria description is modified in the editCriteria action,
     * it creates one pojo for the description
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void editCriteriaPropagateShouldOnlyPropagateOnOpenAppraisals() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        String newCriterionName = "COMMUNICATION SKILLS";
        request.put("name", new String[] {newCriterionName});
        request.put("criterionAreaId", new String[] {"1"});
        String newCriteriaDetail = "New Value for Criteria Description";
        request.put("description", new String[] {newCriteriaDetail});
        request.put("propagateEdit", new String[] {"1"});

        int id = 1;
        EmployeeMgr employeeMgr = new EmployeeMgr();
        Employee employee = employeeMgr.findByOnid("cedenoj", null);
        CriterionArea criterionArea =  criteriaMgrObject.get(id);

        // grab old ids and properties to compare
        int oldCriterionAreaID = criterionArea.getId();
        int oldCriterionDetailID = criterionArea.getCurrentDetail().getId();
        int oldSequence = criterionArea.getSequence();

        criteriaMgrObject.edit(request, id, employee);

        // Double check that the deleted properties were set on pojo
        criterionArea =  criteriaMgrObject.get(id);
        assert criterionArea.getDeleteDate() == null : "Should not have set deletedDate in old pojo";
        assert criterionArea.getDeleter() == null : "Should not have set deleter in old pojo";

        // Sequence should not change
        assert criterionArea.getSequence() == oldSequence : "Sequence should not change";

        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            criterionArea = (CriterionArea) session.
                    createQuery("from edu.osu.cws.pass.models.CriterionArea WHERE name = :name")
                    .setString("name", newCriterionName).list().get(0);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        // Checks that two new pojos were created
        assert oldCriterionAreaID == criterionArea.getId() :
                "should not have created a new criteria pojo";
        assert oldCriterionDetailID != criterionArea.getCurrentDetail().getId() :
                "should have created a new criteria detail pojo";
        assert oldSequence == criterionArea.getSequence() : "Sequence should not change";
        assert criterionArea.getCurrentDetail().getDescription().equals(newCriteriaDetail);

        session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            List<Assessment> results = (List<Assessment>) session.
                    createQuery("from edu.osu.cws.pass.models.Assessment ORDER BY ID")
                    .list();
            tx.commit();

            Assessment assessment = results.get(0);
            assert assessment.getCriterionDetail().getId() != 3 :
                    "Open Appraisals should have assessments' Criterion Detail ID modified";

            assessment = results.get(1);
            assert assessment.getCriterionDetail().getId() != 3 :
                    "Open Appraisals should have assessments' Criterion Detail ID modified";

            assessment = results.get(2);
            assert assessment.getCriterionDetail().getId() == 3 :
                    "Closed Appraisals should not have assessments' Criterion Detail ID modified";

            assessment = results.get(3);
            assert assessment.getCriterionDetail().getId() == 3 :
                    "Completed Appraisals should not have assessments' Criterion Detail ID modified";
        } catch (Exception e) {
            session.close();
            throw e;
        }
    }

    /**
     * Tests to make sure that we get a ModelException when we try to delete a non-existent criteria.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotDeleteCriteriaThatDoesntExist() throws Exception {
        criteriaMgrObject.delete(9999, new Employee(12345));
    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotDeleteAlreadyDeletedcriteria() throws Exception {
        criteriaMgrObject.delete(3, new Employee(12345));
    }

    public void shouldDeleteCriteriaAndUpdateSequence() throws Exception {
        criteriaMgrObject.delete(1, new Employee(12345));
        assert criteriaMgrObject.list(AppointmentType.CLASSIFIED).size() == 1;
        assert criteriaMgrObject.getNextSequence(AppointmentType.CLASSIFIED) == 2 :
                "Invalid criteria sequence";
    }

    public void shouldUpdateSequenceWhenMovingDown() throws Exception {
        criteriaMgrObject.updateSequence(1, 2);
        assert criteriaMgrObject.get(1).getSequence() == 2;
        assert criteriaMgrObject.get(2).getSequence() == 1;
    }

    public void shouldUpdateSequenceWhenMovingUp() throws Exception {
        criteriaMgrObject.updateSequence(2, 1);
        assert criteriaMgrObject.get(1).getSequence() == 2;
        assert criteriaMgrObject.get(2).getSequence() == 1;
    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotUpdateSequenceOfDeletedCriteria() throws Exception {
        criteriaMgrObject.updateSequence(3, 1);
    }
}
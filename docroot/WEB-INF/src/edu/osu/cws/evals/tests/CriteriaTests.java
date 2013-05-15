/**
 * Tests the Criteria model methods.
 */
package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.CriteriaMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.models.*;


import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.*;
import java.util.*;

@Test
public class CriteriaTests {

    CriterionArea criterionObject = new CriterionArea();

    Transaction tx;

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
        initializeObjects();
        Session session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        tx.commit();
    }

    /**
     * This method is run before each one of the test methods gets executed. It
     * just resets the various criteria objects used in the tests to new values.
     */
    public void initializeObjects() throws Exception {
        criterionObject = new CriterionArea();
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
        Session session = HibernateUtil.getCurrentSession();
        Employee creator = (Employee) session.load(Employee.class, 12345);

        criterionObject.setName("Some valid name");
        criterionObject.setDescription("Some valid description");
        //was criteriaDetailObject.setDescription("Some valid description");
        criterionObject.setAppointmentType(type);
        criterionObject.setCreator(creator);

        return new Object[][] {
                { criterionObject }
                //was { criterionObject, criteriaDetailObject }
        };
    }

    /**
     * Tests that fetching a list of active criteria for classified employees
     * works correctly. It checks that the correct associated CriterionDetail
     * are fetched as well.
     */
    @Test(groups = {"unittest"})
    public void returnActiveCriteriaForClassified() throws ModelException, Exception {
        List activeCriteriaList = CriteriaMgr.list(CriteriaMgr.DEFAULT_APPOINTMENT_TYPE);
        CriterionArea expectedCriteria = new CriterionArea();
        CriterionArea expectedCriteria2 = new CriterionArea();

        CriterionArea fakeCriteria;
        CriterionArea dbCriteria;

        String type = AppointmentType.CLASSIFIED;

        expectedCriteria.setId(1);
        expectedCriteria.setName("COMMUNICATION SKILLS");
        expectedCriteria.setAppointmentType(type);
        expectedCriteria.setCreator(new Employee());
        expectedCriteria.setDescription("How will you improve your communication?");

        expectedCriteria2.setId(2);
        expectedCriteria2.setName("TECHNICAL SKILLS");
        expectedCriteria2.setAppointmentType(type);
        expectedCriteria2.setCreator(new Employee());
        expectedCriteria2.setDescription("What training will you obtain this year?");

        ArrayList expected = new ArrayList<CriterionArea>();
        expected.add(expectedCriteria);
        expected.add(expectedCriteria2);

        Assert.assertEquals(activeCriteriaList.size(), expected.size());
        for (int i = 0; i < expected.size(); i++) {
            fakeCriteria = (CriterionArea) expected.get(i);
            dbCriteria = (CriterionArea) activeCriteriaList.get(i);
            Assert.assertEquals(fakeCriteria.getId(), dbCriteria.getId());
        }
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
     * Tests that saving a new Criteria object works correctly. The save method returns boolean
     * based on success of the operation.
     */
    @Test(groups = {"unittest"})
    public void addNewCriteria() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Employee createdBy = (Employee) session.load(Employee.class, 12345);
        String type = AppointmentType.CLASSIFIED;

        String name = "Communication";
        criterionObject.setName(name);
        criterionObject.setDescription("How do you plan to improve your communication skills?");
        criterionObject.setAppointmentType(type);

        CriteriaMgr.add(criterionObject, createdBy);

        boolean savedInDB = session.createQuery("from edu.osu.cws.evals.models.CriterionArea WHERE name = :name")
                        .setString("name", name).list().size() == 1;
        assert savedInDB : "Valid data should save";
    }


    /**
     * Tests that when only the criteria name is modified in the edit action,
     * it creates two pojos, and the old criteria is set to deleted.
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void editOnlyCriteriaNameShouldWork() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        String newCriterionName = "New Name For Criteria";
        request.put("name", new String[] {newCriterionName});
        request.put("criterionAreaId", new String[] {"1"});
        request.put("description", new String[] {"How will you increase your communication skills?"});

        int id = 1;
        EmployeeMgr employeeMgr = new EmployeeMgr();
        Employee employee = employeeMgr.findByOnid("cedenoj", null);
        CriterionArea criterionArea =  CriteriaMgr.get(id);

        // grab old ids and properties to compare
        int oldCriterionAreaID = criterionArea.getId();

        CriteriaMgr.edit(request, id, employee);

        // Double check that the deleted properties were set on pojo
        criterionArea =  CriteriaMgr.get(id);
        assert criterionArea.getDeleteDate() != null : "Should have set deletedDate in old pojo";
        assert criterionArea.getDeleter() != null : "Should have set deleter in old pojo";

        Session session = HibernateUtil.getCurrentSession();
        criterionArea = (CriterionArea) session.
                createQuery("from edu.osu.cws.evals.models.CriterionArea WHERE name = :name")
                .setString("name", newCriterionName).list().get(0);

        // Checks that two new pojos were created
        assert oldCriterionAreaID != criterionArea.getId() :
                "should have created a new criteria pojo";
    }

    /**
     * Tests that when only the criteria description is modified in the edit action,
     * it creates one pojo for the description
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void editOnlyCriteriaDescriptionShouldWork() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        String newCriterionName = "COMMUNICATION SKILLS";
        request.put("name", new String[] {newCriterionName});
        request.put("criterionAreaId", new String[] {"1"});
        String newDescription = "New Value for Criteria Description";
        request.put("description", new String[] {newDescription});

        int id = 1;
        Employee employee = EmployeeMgr.findByOnid("cedenoj", null);
        CriterionArea criterionArea =  CriteriaMgr.get(id);

        // grab old ids and properties to compare
        int oldCriterionAreaID = criterionArea.getId();

        CriteriaMgr.edit(request, id, employee);

        // Double check that the deleted properties were set on pojo
        criterionArea =  CriteriaMgr.get(id);
        assert criterionArea.getDeleteDate() != null : "Should have set deletedDate in old pojo";
        assert criterionArea.getDeleter() != null : "Should have set deleter in old pojo";

        Session session = HibernateUtil.getCurrentSession();
        criterionArea = (CriterionArea) session.
                createQuery("from edu.osu.cws.evals.models.CriterionArea WHERE description = :desc")
                .setString("desc", newDescription).list().get(0);

        // Checks that two new pojos were created
        assert oldCriterionAreaID != criterionArea.getId() :
                "should have created a new criteria pojo";
        assert criterionArea.getDescription().equals(newDescription);
    }

    /**
     * Tests that when both criteria name and description are modified in the edit action,
     * it creates two pojos, and the old criteria is set to deleted.
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void editBothCriteriaNameAndDescriptionShouldWork() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        String newCriterionName = "New Name For Criteria";
        request.put("name", new String[] {newCriterionName});
        request.put("criterionAreaId", new String[] {"1"});
        String newDetailDescription = "New Value for Criteria Description";
        request.put("description", new String[] {newDetailDescription});

        int id = 1;
        EmployeeMgr employeeMgr = new EmployeeMgr();
        Employee employee = employeeMgr.findByOnid("cedenoj", null);
        CriterionArea criterionArea =  CriteriaMgr.get(id);

        // grab old ids and properties to compare
        int oldCriterionAreaID = criterionArea.getId();

        CriteriaMgr.edit(request, id, employee);

        // Double check that the deleted properties were set on pojo
        criterionArea =  CriteriaMgr.get(id);
        assert criterionArea.getDeleteDate() != null : "Should have set deletedDate in old pojo";
        assert criterionArea.getDeleter() != null : "Should have set deleter in old pojo";

        Session session = HibernateUtil.getCurrentSession();
        criterionArea = (CriterionArea) session.
                createQuery("from edu.osu.cws.evals.models.CriterionArea WHERE name = :name")
                .setString("name", newCriterionName).list().get(0);

        // Checks that two new pojos were created
        assert oldCriterionAreaID != criterionArea.getId() :
                "should have created a new criteria pojo";
        assert criterionArea.getName().equals(newCriterionName) : "Should have updated name";
        assert criterionArea.getDescription().equals(newDetailDescription);
    }

    /**
     * Tests that when only the criteria description is modified in the edit action,
     * it creates one pojo for the description
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void editPropagateShouldOnlyPropagateOnOpenAppraisals() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        String newCriterionName = "COMMUNICATION SKILLS";
        request.put("name", new String[] {newCriterionName});
        request.put("criterionAreaId", new String[] {"1"});
        String newDescription = "New Value for Criteria Description";
        request.put("description", new String[] {newDescription});
        request.put("propagateEdit", new String[] {"1"});

        int id = 1;
        Employee employee = EmployeeMgr.findByOnid("cedenoj", null);
        CriterionArea criterionArea =  CriteriaMgr.get(id);

        // grab old ids and properties to compare
        int oldCriterionAreaID = criterionArea.getId();

        CriteriaMgr.edit(request, id, employee);

        // Double check that the deleted properties were set on pojo
        criterionArea =  CriteriaMgr.get(id);
        assert criterionArea.getDeleteDate() != null : "Should have set deletedDate in old pojo";
        assert criterionArea.getDeleter() != null : "Should have set deleter in old pojo";

        Session session = HibernateUtil.getCurrentSession();
        criterionArea = (CriterionArea) session.
                createQuery("from edu.osu.cws.evals.models.CriterionArea WHERE description = :desc")
                .setString("desc", newDescription).list().get(0);

        // Checks that two new pojos were created
        assert oldCriterionAreaID != criterionArea.getId() :
                "should have created a new criteria pojo";
        assert criterionArea.getDescription().equals(newDescription);

        session = HibernateUtil.getCurrentSession();
        Integer newCount = session.createQuery("from edu.osu.cws.evals.models.AssessmentCriteria " +
                        "where criteriaArea.id = :id")
                .setInteger("id", criterionArea.getId()).list().size();

        Integer oldCount = session.createQuery("from edu.osu.cws.evals.models.AssessmentCriteria " +
                        "where criteriaArea.id = :id")
                .setInteger("id", oldCriterionAreaID).list().size();

        assert newCount > 0 : "Should have created updated assessment criteria fk";
        assert oldCount == 0 : "Should have created updated assessment criteria fk";
    }

    /**
     * Tests to make sure that we get a ModelException when we try to delete a non-existent criteria.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotDeleteCriteriaThatDoesntExist() throws Exception {
        CriteriaMgr.delete(9999, new Employee(12345));
    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotDeleteAlreadyDeletedcriteria() throws Exception {
        CriteriaMgr.delete(3, new Employee(12345));
    }

}
package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.EvalsPDF;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

@Test
public class EvalsPDFTest {
    Appraisal appraisal = new Appraisal();

    @BeforeMethod
    public void setUp() throws Exception {
        Job job = new Job();
        Employee employee = new Employee();
        employee.setId(1234);
        job.setEmployee(employee);
        job.setPositionNumber("C555");
        appraisal.setJob(job);
        appraisal.setStartDate(new Date());
    }

    @Test(expectedExceptions = {Exception.class})
    public void shouldFailWithInvalidEnvironemnt() throws Exception {
        EvalsPDF.getFileName(appraisal, null, "invalid");
    }

    public void shouldUsePassAndEnvironmentInFilePrefix() throws Exception {
        String filename = EvalsPDF.getFileName(appraisal, "", "prod");
        assert filename.contains("prod_pass-") : "Invalid file prefix: " + filename;

        filename = EvalsPDF.getFileName(appraisal, "", "dev2");
        assert filename.contains("dev2_pass-") : "Invalid file prefix: " + filename;
    }

    public void shouldUsePidmFiscalYearAndPositionNoInFilename() throws Exception {
        String filename;
        String expectedFilename;

        int fiscalYear = Calendar.getInstance().get(Calendar.YEAR);
        filename = EvalsPDF.getFileName(appraisal, "/tmp/testo/", "prod");
        expectedFilename = "/tmp/testo/prod_pass-1234_" + fiscalYear + "_C555-.pdf";
        assert filename.equals(expectedFilename);

    }

    /**
     * Test method to create a pdf without having to go through the web ui.
     */
//    public void createPDFTesto() throws Exception {
//        HibernateUtil.setConfig("hibernate-luminis-dev.cfg.xml");
//        String filename = EvalsPDF.getNolijFileName(appraisal, "/tmp/", "dev2");
//        AppraisalMgr appraisalMgr = new AppraisalMgr();
//        PermissionRuleMgr permissionRuleMgr = new PermissionRuleMgr();
//        AdminMgr adminMgr = new AdminMgr();
//        AppraisalStepMgr appraisalStepMgr = new AppraisalStepMgr();
//        EmployeeMgr employeeMgr = new EmployeeMgr();
//        Employee currentlyLoggedOnUser = employeeMgr.findByOnid("pass2", "employee-with-jobs");
//
//        appraisalMgr.setPermissionRules(permissionRuleMgr.list());
//        appraisalMgr.setLoggedInUser(currentlyLoggedOnUser);
//        appraisalMgr.setAdmins(adminMgr.mapByEmployeeId());
//        appraisalMgr.setAppraisalSteps(appraisalStepMgr.list());
//
//        appraisal = appraisalMgr.getAppraisal(69);
//        PermissionRule permRule = appraisalMgr.getAppraisalPermissionRule(appraisal, true);
//
//        FileInputStream fis = new FileInputStream("/Users/jose/Documents/liferay-plugins-sdk-5.2.3/portlets/evals-portlet/docroot/WEB-INF/src/edu/osu/cws/evals/portlet/Language.properties");
//        ResourceBundle resource = new PropertyResourceBundle(fis);
//
//        EvalsPDF.createPDF(appraisal, permRule, filename, resource, "docroot/");
//    }
}

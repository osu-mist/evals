package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.EvalsPDF;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

import java.util.*;

@Test
public class EvalsPDFTest {
    Appraisal appraisal = mock(Appraisal.class);

    @BeforeMethod
    public void setUp() throws Exception {
        Job job = new Job();
        Employee employee = new Employee();
        employee.setId(1234);
        job.setEmployee(employee);
        job.setPositionNumber("C555");
        when(appraisal.getAppointmentType()).thenReturn("");
        when(appraisal.getJob()).thenReturn(job);
        when(appraisal.getStartDate()).thenReturn(new Date());
    }

    @Test(expectedExceptions = {Exception.class})
    public void shouldFailWithInvalidEnvironment() throws Exception {
        EvalsPDF PdfGenerator = new EvalsPDF("", appraisal, null, "", "invalid", "-prof", null);
        PdfGenerator.getFileName();
    }

    public void shouldUsePassAndEnvironmentInFilePrefix() throws Exception {
        EvalsPDF PdfGenerator1 = new EvalsPDF("", appraisal, null, "", "prod", "-prof", null);
        String filename = PdfGenerator1.getFileName();
        assert filename.contains("prod_evals-") : "Invalid file prefix: " + filename;

        EvalsPDF PdfGenerator2 = new EvalsPDF("", appraisal, null, "", "dev2", "-prof", null);
        filename = PdfGenerator2.getFileName();
        assert filename.contains("dev2_evals-") : "Invalid file prefix: " + filename;
    }

    public void shouldUsePidmFiscalYearAndPositionNoInFilename() throws Exception {
        String filename;
        String expectedFilename;

        int fiscalYear = Calendar.getInstance().get(Calendar.YEAR);
        EvalsPDF PdfGenerator = new EvalsPDF("", appraisal, null, "/tmp/testo/", "prod", "-prof", null);
        filename = PdfGenerator.getFileName();
        expectedFilename = "/tmp/testo/prod_evals-1234_" + fiscalYear + "_C555-.pdf";
        assert filename.equals(expectedFilename);

    }

    public void shouldReturnCorrectNonEmptySuffixWhenProfessionalFaculty() {
        String correctSuffix = "-prof";
        Appraisal appraisal1 = mock(Appraisal.class);
        when(appraisal1.getAppointmentType()).thenReturn(AppointmentType.PROFESSIONAL_FACULTY);
        EvalsPDF PdfGenerator1 = new EvalsPDF("", appraisal1, null, "", "dev2", correctSuffix, null);
        String suffix = PdfGenerator1.getAppointmentTypeSuffix();
        // Assertions
        assert suffix.equals(correctSuffix);
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

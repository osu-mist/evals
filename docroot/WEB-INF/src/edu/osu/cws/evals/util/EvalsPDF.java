package edu.osu.cws.evals.util;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

public class EvalsPDF {

    /** Fonts **/
    public static final Font INFO_FONT = new Font(Font.FontFamily.HELVETICA, 10);
    public static final Font FONT_BOLD_12 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    public static final Font FONT_BOLD_11 = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    public static final Font FONT_BOLDITALIC_10 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLDITALIC);
    public static final Font FONT_BOLD_10 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    public static final Font FONT_10 = new Font(Font.FontFamily.HELVETICA, 10);
    public static final Font FONT_ITALIC_10 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLDITALIC);
    public static final Font FONT_BOLD_14 = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);

    /** Images **/
    public static final String IMAGE_OSU_LOGO = "images/pdf-osu-logo.png";
    public static final String IMAGE_CHECKBOX_CHECKED = "images/pdf-checkbox-checked.jpg";
    public static final String IMAGE_CHECKBOX_UNCHECKED = "images/pdf-checkbox-unchecked.jpg";

    public static final float LEFT_INDENTATION = 8f;
    public static final float BEFORE_SPACING = 12f;

    private Appraisal appraisal;
    private ResourceBundle resource;
    private String dirName;
    private String environment;
    private String rootDir;
    private PermissionRule permRule;
    private Document document;
    private List<Rating> ratings;

    /**
     * @param rootDir       Root directory where the images and other resources can be found
     * @param appraisal     Appraisal object
     * @param resource      ResourceBundle object
     * @param dirName       the directory PDF files resides.
     * @param env   either "prod" or "dev2"
     * @param ratings       Sorted list of ratings
     */
    public EvalsPDF(String rootDir, Appraisal appraisal, ResourceBundle resource, String dirName, String env,
                    List<Rating> ratings) {
        this.rootDir = rootDir;
        this.appraisal = appraisal;
        this.resource = resource;
        this.dirName = dirName;
        this.environment = env;
        this.permRule = appraisal.getPermissionRule();
        this.ratings = ratings;
        Rectangle pageSize = new Rectangle(612f, 792f);
        this.document = new Document(pageSize, 50f, 40f, 24f, 24f);
    }

    /**
     * Returns the name of the appraisal nolij pdf file.
     *
     * @return filename     composed of: dirname+prod_pass-[PIDM]_[FISCAL YEAR]_[POSITION NUMBER]-.pdf
     * @throws Exception    If the environment is not valid
     *
     * remains public, so the test methods don't break.
     */
    public String getFileName() throws Exception {
        String filename = "";

        if (environment == null || (!environment.equals("prod") && !environment.equals("dev2"))) {
            throw new Exception("Invalid environment: '" + environment + "' in filename");
        }

        if (appraisal.getStartDate() == null) {
            throw new Exception("Start date is null");
        }

        if (appraisal.getJob().getEmployee() == null) {
            throw new Exception("Employee is null");
        }

        if (appraisal.getJob().getPositionNumber() == null) {
            throw new Exception("Job position no is null");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(appraisal.getStartDate());
        int fiscalYear = Calendar.getInstance().get(Calendar.YEAR);
        int pidm = appraisal.getJob().getEmployee().getId();

        String positionNo = appraisal.getJob().getPositionNumber();

        filename = dirName;
        filename += environment + "_evals-" + pidm + "_" + fiscalYear + "_" + positionNo + "-.pdf";

        return filename;
    }

    /**
     * Generates a PDF file for the given appraisal using the permission rule. It writes the
     * PDF file to disk.
     *
     * @throws Exception
     */
    public String createPDF() throws Exception {

        //@todo: escape any text before writing it to the PDF doc??
        String filename = getFileName();

        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();

        document.add(getLetterHead());
        document.add(getInfoTable());

        addAssessments();
        if (StringUtils.containsAny(permRule.getEvaluation(), "ev")) {
            addEvaluation();
        }
        if (StringUtils.containsAny(permRule.getEmployeeResponse(), "ev") && appraisal.getEmployeeSignedDate() != null) {
            addEmployeeResponse();
        }
        addCriteriaLegend();
        document.close();
        return filename;
    }

    /**
     * Specifies whether or not the employee rebuttal should be displayed
     * and add rebuttal
     *
     * @throws DocumentException
     */
    private void addEmployeeResponse() throws DocumentException {
        document.add(getSignatureTable());
        if (appraisal.getRebuttal() != null) {
            addRebuttal();
        }
    }


    /**
     * Returns a PdfPTable object that contains the signature table displayed at the bottom of the
     * appraisal form.
     *
     * @return
     * @throws DocumentException
     */
    private PdfPTable getSignatureTable() throws DocumentException {

        int signatureTableMaxCols = 29;
        int signatureTableMaxRows = 7;
        int nameColSpan = 8;
        int dateColSpan = 5;

        PdfPTable signatureTable = new PdfPTable(signatureTableMaxCols);
        signatureTable.setWidthPercentage(100f);
        signatureTable.setSpacingBefore(24f);
        signatureTable.setKeepTogether(true);

        PdfPCell employeeCell = new PdfPCell();
        PdfPCell employeeDateCell = new PdfPCell();
        PdfPCell supervisorCell = new PdfPCell();
        PdfPCell supervisorDateCell = new PdfPCell();
        employeeCell.setColspan(nameColSpan);
        employeeCell.setBorder(Rectangle.TOP + Rectangle.BOTTOM);
        employeeDateCell.setColspan(dateColSpan);
        employeeDateCell.setBorder(Rectangle.TOP + Rectangle.BOTTOM);
        supervisorCell.setColspan(nameColSpan);
        supervisorCell.setBorder(Rectangle.TOP + Rectangle.BOTTOM);
        supervisorDateCell.setColspan(dateColSpan);
        supervisorDateCell.setBorder(Rectangle.TOP + Rectangle.BOTTOM);

        // Begin ROW 1
        Job job = appraisal.getJob();

        String employeeName = job.getEmployee().getName();
        DateTime employeeSignedDate = new DateTime(appraisal.getEmployeeSignedDate()).withTimeAtStartOfDay();
        String employeeSignDate = employeeSignedDate.toString(Constants.DATE_FORMAT);
        employeeCell.addElement(new Phrase(employeeName, INFO_FONT));
        employeeDateCell.addElement(new Phrase(employeeSignDate, INFO_FONT));

        if (appraisal.getReleaseDate() != null) {
            DateTime releaseDate = new DateTime(appraisal.getReleaseDate()).withTimeAtStartOfDay();
            String supervisorName = job.getSupervisor().getEmployee().getName();
            String supervisorSignDate = releaseDate.toString(Constants.DATE_FORMAT);
            supervisorCell.addElement(new Phrase(supervisorName, INFO_FONT));
            supervisorDateCell.addElement(new Phrase(supervisorSignDate, INFO_FONT));
        }

        // Add Left Padding column that spans to all the rows of the table
        PdfPCell leftPaddingCell = new PdfPCell();
        leftPaddingCell.setRowspan(signatureTableMaxRows);
        leftPaddingCell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM + Rectangle.TOP);
        signatureTable.addCell(leftPaddingCell);

        signatureTable.addCell(employeeCell);
        signatureTable.addCell(employeeDateCell);

        PdfPCell middlePaddingCell = new PdfPCell();
        middlePaddingCell.setRowspan(signatureTableMaxRows - 2);
        middlePaddingCell.setBorder(Rectangle.TOP);
        signatureTable.addCell(middlePaddingCell);

        signatureTable.addCell(supervisorCell);
        signatureTable.addCell(supervisorDateCell);
        /** End ROW 1 **/

        // Add Right Padding column that spans to all the rows of the table
        PdfPCell rightPaddingCell = new PdfPCell();
        rightPaddingCell.setRowspan(signatureTableMaxRows);
        rightPaddingCell.setBorder(Rectangle.BOTTOM + Rectangle.TOP + Rectangle.RIGHT);
        signatureTable.addCell(rightPaddingCell);

        /** Begin ROW 2 **/
        PdfPCell employeeSignLabel = new PdfPCell();
        employeeSignLabel.setColspan(nameColSpan);
        String employeeSignText = resource.getString("appraisal-employee-signature-pdf");
        employeeSignLabel.setPhrase(new Phrase(employeeSignText, INFO_FONT));
        employeeSignLabel.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(employeeSignLabel);

        String dateText = resource.getString("appraisal-date");
        PdfPCell dateCell = new PdfPCell();
        dateCell.setColspan(dateColSpan);
        dateCell.setPhrase(new Phrase(dateText, INFO_FONT));
        dateCell.setVerticalAlignment(Element.ALIGN_TOP);
        dateCell.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(dateCell);

        PdfPCell supervisorSignLabel = new PdfPCell();
        supervisorSignLabel.setColspan(nameColSpan);
        String supervisorSignText = resource.getString("appraisal-supervisor-signature");
        supervisorSignLabel.setPhrase(new Phrase(supervisorSignText, INFO_FONT));
        supervisorSignLabel.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(supervisorSignLabel);
        signatureTable.addCell(dateCell);
        /** End ROW 2 **/

        // Empty ROW 3
        PdfPCell nameAndDateCell = new PdfPCell();
        nameAndDateCell.setColspan(dateColSpan + nameColSpan);
        nameAndDateCell.setFixedHeight(12f);
        nameAndDateCell.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(nameAndDateCell);
        signatureTable.addCell(nameAndDateCell);

        /** Begin ROW 4 **/
        String employeeSignDesc = resource.getString("appraisal-acknowledge-read");
        PdfPCell employeeDescCell = new PdfPCell();
        employeeDescCell.setPhrase(new Paragraph(employeeSignDesc, FONT_10));
        employeeDescCell.setColspan(nameColSpan + dateColSpan);
        employeeDescCell.setRowspan(2);
        employeeDescCell.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(employeeDescCell);

        PdfPCell reviewerCell = new PdfPCell();
        reviewerCell.setMinimumHeight(12f);
        reviewerCell.setColspan(nameColSpan);
        reviewerCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell reviewerDateCell = new PdfPCell();
        reviewerDateCell.setColspan(dateColSpan);
        reviewerDateCell.setBorder(Rectangle.NO_BORDER);
        boolean displayReviewer = appraisal.getReleaseDate() != null && appraisal.getReviewSubmitDate() != null;
        if (displayReviewer) {
            String reviewerName = appraisal.getReviewer().getName();
            DateTime reviewSubmitDate = new DateTime(appraisal.getReviewSubmitDate()).withTimeAtStartOfDay();
            String reviewDate = reviewSubmitDate.toString(Constants.DATE_FORMAT);
            reviewerCell.setPhrase(new Phrase(reviewerName, INFO_FONT));
            reviewerDateCell.setPhrase(new Phrase(reviewDate, INFO_FONT));
        }
        signatureTable.addCell(reviewerCell);
        signatureTable.addCell(reviewerDateCell);
        /** End ROW 4 **/

        /** Begin ROW 5 **/
        String reviewerLabel = resource.getString("role-reviewer");
        PdfPCell reviewerCellDesc = new PdfPCell(new Phrase(reviewerLabel, INFO_FONT));
        reviewerCellDesc.setColspan(nameColSpan);
        reviewerCellDesc.setBorder(Rectangle.TOP);
        signatureTable.addCell(reviewerCellDesc);
        dateCell.setBorder(Rectangle.TOP);
        signatureTable.addCell(dateCell);
        /** End ROW 5 **/

        /** Begin ROW 6 **/
        String electronicSignature = resource.getString("appraisal-electronic-signature-desc");
        PdfPCell electronicSignatureCell = new PdfPCell();
        electronicSignatureCell.addElement(new Phrase(electronicSignature, FONT_ITALIC_10));
        electronicSignatureCell.setColspan(signatureTableMaxCols-2);
        electronicSignatureCell.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(electronicSignatureCell);
        /** End ROW 6 **/

        // Empty ROW 7
        nameAndDateCell.setBorder(Rectangle.BOTTOM);
        nameAndDateCell.setColspan(signatureTableMaxCols-2);
        signatureTable.addCell(nameAndDateCell);

        return signatureTable;
    }

    /**
     * Takes care of adding the employee rebuttal and supervisor rebuttal read to the pdf document.
     *
     * @throws DocumentException
     */
    private void addRebuttal() throws DocumentException {
        String rebuttalType =  "rebuttal";
        if (appraisal.getAppointmentType().equals(AppointmentType.PROFESSIONAL_FACULTY)) {
            rebuttalType = "feedback";
        }

        String rebuttalLblText= resource.getString("appraisal-employee-response-" + rebuttalType).toUpperCase();
        Paragraph rebuttalLbl = new Paragraph(rebuttalLblText, FONT_BOLD_11);
        rebuttalLbl.setSpacingBefore(BEFORE_SPACING);
        document.add(rebuttalLbl);
        LineSeparator line = new LineSeparator(1, 100, null, Element.ALIGN_CENTER, -2);
        document.add(line);

        Paragraph rebuttalText = new Paragraph(appraisal.getRebuttal(), FONT_10);
        rebuttalText.setIndentationLeft(LEFT_INDENTATION);
        document.add(rebuttalText);
    }


    /**
     * Adds the evaluation section (appraisal + rating) to the PDF document.
     *
     * @throws DocumentException
     * @throws IOException
     */
    private void addEvaluation() throws DocumentException, IOException {
        String appraisalSectionText = resource.getString("appraisal-summary");
        appraisalSectionText = appraisalSectionText.toUpperCase();
        Paragraph appraisalSectionLbl = new Paragraph(appraisalSectionText, FONT_BOLD_11);
        appraisalSectionLbl.setSpacingBefore(BEFORE_SPACING);
        document.add(appraisalSectionLbl);
        LineSeparator line = new LineSeparator(1, 100, null, Element.ALIGN_CENTER, -2);
        document.add(line);

        Paragraph evaluationLbl = new Paragraph(resource.getString("appraisal-evaluation"), FONT_BOLDITALIC_10);
        Paragraph evaluationText = new Paragraph(appraisal.getEvaluation(), FONT_10);
        evaluationLbl.setIndentationLeft(LEFT_INDENTATION);
        evaluationText.setIndentationLeft(LEFT_INDENTATION);
        document.add(evaluationLbl);
        document.add(evaluationText);

        Paragraph ratingLbl = new Paragraph(resource.getString("appraisal-select-rating"), FONT_BOLDITALIC_10);
        ratingLbl.setIndentationLeft(LEFT_INDENTATION);
        ratingLbl.setSpacingBefore(BEFORE_SPACING);
        ratingLbl.setSpacingAfter(6f);
        document.add(ratingLbl);

        int ratingMaxCols = 30;
        PdfPTable ratingTable = new PdfPTable(ratingMaxCols);
        PdfPCell emptyLeftCol = new PdfPCell();
        emptyLeftCol.setBorder(Rectangle.NO_BORDER);
        emptyLeftCol.setRowspan(4);
        ratingTable.addCell(emptyLeftCol);
        ratingTable.setWidthPercentage(100f);
        PdfPCell cell;

        // Use an image for the unchecked box
        Image checkedImg = Image.getInstance(rootDir + IMAGE_CHECKBOX_CHECKED);
        checkedImg.scaleToFit(10f, 10f);
        PdfPCell checkedBox = new PdfPCell(checkedImg, false);
        checkedBox.setColspan(1);
        checkedBox.setVerticalAlignment(Element.ALIGN_MIDDLE);
        checkedBox.setBorder(Rectangle.NO_BORDER);

        // Use an image for the checked box.
        Image uncheckedImg = Image.getInstance(rootDir + IMAGE_CHECKBOX_UNCHECKED);
        uncheckedImg.scaleToFit(10f, 10f);
        PdfPCell uncheckedBox = new PdfPCell(uncheckedImg, false);
        uncheckedBox.setColspan(1);
        uncheckedBox.setVerticalAlignment(Element.ALIGN_MIDDLE);
        uncheckedBox.setBorder(Rectangle.NO_BORDER);

        for (Rating rating : ratings) {
            if (appraisal.getRating() != null && appraisal.getRating().equals(rating.getRate())) {
                ratingTable.addCell(checkedBox);
            } else {
                ratingTable.addCell(uncheckedBox);
            }
            cell = new PdfPCell(new Paragraph(rating.getDescription(), FONT_10));
            cell.setColspan(ratingMaxCols - 2);
            cell.setBorder(Rectangle.NO_BORDER);
            ratingTable.addCell(cell);
        }

        document.add(ratingTable);

        // only Classified IT evals get the salary table
        if (appraisal.getJob().getAppointmentType().equals(AppointmentType.CLASSIFIED_IT)) {
            addSalaryTable();
        }
    }

    /**
     * Adds the salary table for IT evaluations.
     *
     * @throws DocumentException
     */
    private void addSalaryTable() throws DocumentException {

        Paragraph sectionTitle = new Paragraph(resource.getString("appraisal-salary-section-title"),
                FONT_BOLDITALIC_10);
        sectionTitle.setIndentationLeft(LEFT_INDENTATION);
        sectionTitle.setSpacingBefore(BEFORE_SPACING);
        sectionTitle.setSpacingAfter(6f);
        document.add(sectionTitle);

        float[] columnWidths = new float[] {20f, 20f, 20f, 20f, 26f, 20f, 26f, 20f};
        PdfPTable salaryTable = new PdfPTable(columnWidths.length);
        salaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        salaryTable.setWidthPercentage(98.3f);
        salaryTable.setKeepTogether(true);
        // specify column widths dynamically since we need varying widths
        salaryTable.setWidths(columnWidths);

        PdfPCell cell;
        Phrase phrase;

        // Heading Row
        phrase = new Phrase(resource.getString("appraisal-salary-current")+": ", FONT_BOLD_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        phrase = new Phrase(resource.getString("appraisal-salary-control-point-value")+": ", FONT_BOLD_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        phrase = new Phrase(resource.getString("appraisal-salary-control-low")+": ", FONT_BOLD_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        phrase = new Phrase(resource.getString("appraisal-salary-control-high")+": ", FONT_BOLD_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        phrase = new Phrase(resource.getString("appraisal-salary-control-point")+": ", FONT_BOLD_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        phrase = new Phrase(resource.getString("appraisal-salary-recommended-increase") +": ", FONT_BOLD_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        phrase = new Phrase(resource.getString("appraisal-salary-after-increase")+": ", FONT_BOLD_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        phrase = new Phrase(resource.getString("appraisal-salary-eligibility-date")+": ", FONT_BOLD_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        // Data Rows
        Salary salary = appraisal.getSalary();
        // Current Salary
        phrase = new Phrase(CWSUtil.formatCurrency(salary.getCurrent()), FONT_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        // Control Point Value
        phrase = new Phrase(CWSUtil.formatCurrency(salary.getMidPoint()), FONT_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        // Low Control Point
        phrase = new Phrase(CWSUtil.formatCurrency(salary.getLow()), FONT_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        // High Control Point
        phrase = new Phrase(CWSUtil.formatCurrency(salary.getHigh()), FONT_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        // Above/Below Control Point
        String belowOrAboveOrAt = resource.getString("appraisal-salary-at");
        if (salary.getCurrent() > salary.getMidPoint()) {
            belowOrAboveOrAt = resource.getString("appraisal-salary-above");
        } else if (salary.getCurrent() < salary.getMidPoint()) {
            belowOrAboveOrAt = resource.getString("appraisal-salary-below");
        }
        phrase = new Phrase(belowOrAboveOrAt, FONT_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        // Recommended Increase
        Double increase = salary.getIncrease();
        if (increase == null) {
            increase = 0d;
        }
        phrase = new Phrase(increase.toString(), FONT_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        // Salary After Increase
        Double salaryAfterIncrease = salary.getCurrent() * (1 + increase / 100);
        phrase = new Phrase(CWSUtil.formatCurrency(salaryAfterIncrease), FONT_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);


        // Salary Eligibility Date
        DateTime salaryEligibilityDate = new DateTime(appraisal.getSalaryEligibilityDate()).withTimeAtStartOfDay();
        String sed = salaryEligibilityDate.toString("MM/dd");
        phrase = new Phrase(sed, FONT_10);
        cell = new PdfPCell(phrase);
        salaryTable.addCell(cell);

        document.add(salaryTable);
    }

    /**
     * Returns a PdfPTable object that contains the appraisal and job summary information.
     *
     * @return PdfPTable
     */
    public PdfPTable getInfoTable() {
        PdfPTable info = new PdfPTable(4);
        info.setWidthPercentage(100f);
        info.setKeepTogether(true);

        PdfPCell cell;
        Paragraph p;
        Chunk c;

        c = new Chunk(resource.getString("employee")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getEmployee().getName(), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        cell.setColspan(2);
        info.addCell(cell);

        c = new Chunk(resource.getString("ts-org-code-desc")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getOrgCodeDescription(), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("jobTitle")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getJobTitle(), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("supervisor")+": ", INFO_FONT);
        p = new Paragraph(c);
        String name = "";
        if (appraisal.getJob().getSupervisor() != null) {
            name = appraisal.getJob().getSupervisor().getEmployee().getName();
        }
        c = new Chunk(name, FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("appraisal-employee-id")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getEmployee().getOsuid(), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("position-class")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getPositionClass(), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("position-no")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getPositionNumber(), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        String appraisalTypeKey = "appraisal-type-" + appraisal.getType();
        c = new Chunk(resource.getString("appraisal-type-pdf")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(resource.getString(appraisalTypeKey), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("reviewPeriod")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getReviewPeriod(), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("appraisal-status")+": ", INFO_FONT);
        p = new Paragraph(c);
        c = new Chunk(resource.getString(appraisal.getViewStatus()), FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("appraisal-rating")+": ", INFO_FONT);
        p = new Paragraph(c);
        String rating = "";
        boolean displayRating = StringUtils.containsAny(permRule.getEvaluation(), "ev");
        if (appraisal.getRating() != null && displayRating) {
            rating = appraisal.getRating().toString();
        }
        c = new Chunk(rating, FONT_BOLD_11);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        return info;
    }

    /**
     * Returns a table that contains the top header used in the PDF form.
     *
     * @return PdfPTable
     * @throws Exception
     */
    public PdfPTable getLetterHead() throws Exception {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100f);
        Paragraph paragraph;
        PdfPCell cell;

        Image image = Image.getInstance(rootDir + IMAGE_OSU_LOGO);
        image.scaleToFit(116.625f, 35.875f);
        cell = new PdfPCell(image, false);
        cell.setBorder(Rectangle.NO_BORDER);
        header.addCell(cell);


        paragraph = new Paragraph(resource.getString("office-hr"), INFO_FONT);
        paragraph.setAlignment(Element.ALIGN_MIDDLE);
        cell = new PdfPCell(paragraph);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setBorder(Rectangle.NO_BORDER);
        header.addCell(cell);

        String title = appraisal.getJob().getAppointmentType() + " " + resource.getString("appraisal-title");
        paragraph = new Paragraph(title, FONT_BOLD_14);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        cell = new PdfPCell(paragraph);
        cell.setBorder(Rectangle.NO_BORDER);
        header.addCell(cell);

        header.setSpacingAfter(12f);

        return header;
    }


    /**
     * Adds headers and assessments based on goals versions
     *
     * @throws
     */
    private void addAssessments() throws Exception {
        String goalHeader = "";
        int displayedGoalCount = 0;

        if(StringUtils.containsAny(permRule.getApprovedGoals(), "ev")) {
            List<GoalVersion> approvedGoalsVersions = appraisal.getApprovedGoalsVersions();
            for (GoalVersion goalVersion : approvedGoalsVersions){
                goalHeader = resource.getString("appraisal-goals-approved-on") + " " +
                        new DateTime(goalVersion.getGoalsApprovedDate()).toString(Constants.DATE_FORMAT) + ":";
                setGoalsHeader(goalHeader);
                List<Assessment> sortedAssessments = goalVersion.getSortedAssessments();
                displayedGoalCount = displayAssessments(sortedAssessments, displayedGoalCount);
            }
        }

        if(StringUtils.containsAny(permRule.getUnapprovedGoals(), "ev")) {
            GoalVersion unapprovedGoalsVersion = appraisal.getUnapprovedGoalsVersion();
            if (unapprovedGoalsVersion != null) {
                goalHeader = resource.getString("appraisal-goals-need-approved");
                setGoalsHeader(goalHeader);
                List<Assessment> sortedAssessments = unapprovedGoalsVersion.getSortedAssessments();
                displayAssessments(sortedAssessments, displayedGoalCount);
            }
        }
    }

    /**
     * Adds header for one goal version
     *
     * @throws Exception
     */
    private void setGoalsHeader(String goalHeader) throws Exception {
        Chunk goalHeaderChunk = new Chunk(goalHeader, FONT_BOLD_12);
        goalHeaderChunk.setUnderline(1f, -2f);
        Paragraph goalsHeader = new Paragraph(goalHeaderChunk);
        goalsHeader.setSpacingBefore(BEFORE_SPACING);
        document.add(goalsHeader);
    }

    /**
     * Adds assessments for one goal version
     *
     * @throws Exception
     */
    private int displayAssessments(List<Assessment> sortedAssessments, int displayedGoalCount) throws Exception {
        boolean displayEmployeeResults = StringUtils.containsAny(permRule.getResults(), "ev");
        boolean displaySupervisorResults = StringUtils.containsAny(permRule.getSupervisorResults(), "ev");
        Paragraph sectionText;

        for (Assessment assessment : sortedAssessments) {
            sectionText = new Paragraph();
            sectionText.setSpacingBefore(BEFORE_SPACING);
            document.add(sectionText);

            displayedGoalCount ++;
            String goalLabel = resource.getString("appraisal-goals") + displayedGoalCount;

            Chunk goalChunk = new Chunk(goalLabel, FONT_BOLDITALIC_10);
            goalChunk.setUnderline(1f, -2f);

            Paragraph goalsLabel = new Paragraph(goalChunk);
            Paragraph goals = new Paragraph(assessment.getGoal(), FONT_10);
            goals.setIndentationLeft(LEFT_INDENTATION);
            document.add(goalsLabel);
            document.add(goals);

            addAssessmentsCriteria(assessment);

            if (!assessment.isNewGoal()) {
                if (displayEmployeeResults) {
                    Paragraph resultsLabel = new Paragraph(resource.getString("appraisal-employee-results"),
                            FONT_BOLDITALIC_10);
                    Paragraph employeeResult = new Paragraph(assessment.getEmployeeResult(), FONT_10);
                    resultsLabel.setSpacingBefore(BEFORE_SPACING);
                    resultsLabel.setIndentationLeft(LEFT_INDENTATION);
                    employeeResult.setIndentationLeft(LEFT_INDENTATION);
                    document.add(resultsLabel);
                    document.add(employeeResult);
                }

                if (displaySupervisorResults) {
                    Paragraph supervisorResultLbl = new Paragraph(resource.getString("appraisal-result-comments"),
                            FONT_BOLDITALIC_10);
                    Paragraph supervisorResult = new Paragraph(assessment.getSupervisorResult(), FONT_10);
                    supervisorResultLbl.setSpacingBefore(BEFORE_SPACING);
                    supervisorResultLbl.setIndentationLeft(LEFT_INDENTATION);
                    supervisorResult.setIndentationLeft(LEFT_INDENTATION);
                    document.add(supervisorResultLbl);
                    document.add(supervisorResult);
                }
            }
        }
        return displayedGoalCount;
    }

    /**
     * Adds the assessments criteria checkboxes for a single assessment.
     *
     * @param assessment
     * @throws DocumentException
     * @throws IOException
     */
    private void addAssessmentsCriteria(Assessment assessment) throws DocumentException, IOException {
        int ratingMaxCols = 41; // 40 is a round # and 1 extra for padding column
        PdfPTable criteriaTable = new PdfPTable(ratingMaxCols);
        PdfPCell emptyLeftCol = new PdfPCell();
        emptyLeftCol.setBorder(Rectangle.NO_BORDER);
        emptyLeftCol.setRowspan(4);
        criteriaTable.addCell(emptyLeftCol);
        criteriaTable.setWidthPercentage(100f);
        PdfPCell cell;

        // Use an image for the unchecked box
        Image checkedImg = Image.getInstance(rootDir + IMAGE_CHECKBOX_CHECKED);
        checkedImg.scaleToFit(10f, 10f);
        PdfPCell checkedBox = new PdfPCell(checkedImg, false);
        checkedBox.setColspan(1);
        checkedBox.setVerticalAlignment(Element.ALIGN_MIDDLE);
        checkedBox.setBorder(Rectangle.NO_BORDER);
        checkedBox.setPaddingLeft(2f);

        // Use an image for the checked box.
        Image uncheckedImg = Image.getInstance(rootDir + IMAGE_CHECKBOX_UNCHECKED);
        uncheckedImg.scaleToFit(10f, 10f);
        PdfPCell uncheckedBox = new PdfPCell(uncheckedImg, false);
        uncheckedBox.setColspan(1);
        uncheckedBox.setVerticalAlignment(Element.ALIGN_MIDDLE);
        uncheckedBox.setBorder(Rectangle.NO_BORDER);
        uncheckedBox.setPaddingLeft(2f);

        // empty space
        Paragraph sectionText = new Paragraph();
        sectionText.setSpacingBefore(BEFORE_SPACING);
        document.add(sectionText);

        Paragraph criteriaLabel = new Paragraph(resource.getString("appraisal-selected-criteria"), FONT_BOLDITALIC_10);
        criteriaLabel.setIndentationLeft(LEFT_INDENTATION);
        document.add(criteriaLabel);

        // set default column span to 1/4 of # of columns
        int criteriaSize = assessment.getAssessmentCriteria().size();
        int colspan = (ratingMaxCols - 1) / criteriaSize;
        if (criteriaSize > 4) {
            colspan = 10;
        }


        for (AssessmentCriteria assessmentCriteria : assessment.getSortedAssessmentCriteria()) {
            if (assessmentCriteria.getChecked() != null && assessmentCriteria.getChecked()) {
                criteriaTable.addCell(checkedBox);
            } else {
                criteriaTable.addCell(uncheckedBox);
            }
            cell = new PdfPCell(new Paragraph(assessmentCriteria.getCriteriaArea().getName(), FONT_10));

            cell.setColspan(colspan);
            cell.setBorder(Rectangle.NO_BORDER);
            criteriaTable.addCell(cell);
        }

        document.add(criteriaTable);
    }

    /**
     * Adds the criteria area legend.
     *
     * @throws DocumentException
     */
    private void addCriteriaLegend() throws DocumentException {
        LineSeparator line = new LineSeparator(1, 100, null, Element.ALIGN_CENTER, -12);
        document.add(line);

        List<CriterionArea> sortedCriteriaArea = getSortedCriteria();
        PdfPTable criteriaTable = new PdfPTable(41);
        PdfPCell emptyLeftCol = new PdfPCell();
        emptyLeftCol.setBorder(Rectangle.NO_BORDER);
        emptyLeftCol.setRowspan(sortedCriteriaArea.size());
        criteriaTable.addCell(emptyLeftCol);
        criteriaTable.setWidthPercentage(100f);
        PdfPCell cell;

        // empty space
        Paragraph sectionText = new Paragraph();
        sectionText.setSpacingBefore(BEFORE_SPACING);
        document.add(sectionText);

        Chunk criteriaChunk = new Chunk(resource.getString("appraisal-criteria-legend"), FONT_BOLDITALIC_10);
        criteriaChunk.setUnderline(1f, -2f);
        Paragraph criteriaLabel = new Paragraph(criteriaChunk);
        document.add(criteriaLabel);


        for (CriterionArea criterionArea : sortedCriteriaArea) {
            cell = new PdfPCell(new Paragraph(criterionArea.getName(), FONT_10));
            cell.setColspan(10);
            cell.setBorder(Rectangle.NO_BORDER);
            criteriaTable.addCell(cell);

            cell = new PdfPCell(new Paragraph(criterionArea.getDescription(), FONT_10));
            cell.setColspan(30);
            cell.setBorder(Rectangle.NO_BORDER);
            criteriaTable.addCell(cell);
        }

        document.add(criteriaTable);
    }

    /**
     * Returns a list of sorted criteria areas to be used when displaying the legend. This method
     * iterates over the goal versions to find one goal version that has assessments. It then returns
     * the criteria areas for the first assessment within this goal version.
     *
     * @return
     */
    private List<CriterionArea> getSortedCriteria() {
        List<CriterionArea> sortedCriteriaArea = new ArrayList<CriterionArea>();
        for (GoalVersion goalVersion : appraisal.getGoalVersions()) {
            if (!goalVersion.getAssessments().isEmpty()) {
                Assessment assessment = (Assessment) goalVersion.getAssessments().toArray()[0];
                for (AssessmentCriteria assessmentCriteria : assessment.getSortedAssessmentCriteria()) {
                    sortedCriteriaArea.add(assessmentCriteria.getCriteriaArea());
                }
                break;
            }
        }
        return sortedCriteriaArea;
    }
}

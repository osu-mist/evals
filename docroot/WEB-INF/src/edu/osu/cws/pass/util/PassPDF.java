package edu.osu.cws.pass.util;


import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import edu.osu.cws.pass.models.Appraisal;
import edu.osu.cws.pass.models.Assessment;
import edu.osu.cws.pass.models.Job;
import edu.osu.cws.pass.models.PermissionRule;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class PassPDF {

    /** Fonts **/
    public static final Font FONT_10 = new Font(Font.FontFamily.TIMES_ROMAN, 10);
    public static final Font FONT_BOLD_10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    public static final Font FONT_BOLDITALIC_10 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLDITALIC);
    public static final Font FONT_9 = new Font(Font.FontFamily.TIMES_ROMAN, 9);
    public static final Font FONT_ITALIC_9 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLDITALIC);
    public static final Font FONT_BOLD_13 = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.BOLD);

    /** Images **/
    public static final String IMAGE_OSU_LOGO = "images/pdf-osu-logo.png";
    public static final String IMAGE_CHECKBOX_CHECKED = "images/pdf-checkbox-checked.jpg";
    public static final String IMAGE_CHECKBOX_UNCHECKED = "images/pdf-checkbox-unchecked.jpg";

    public static final float LEFT_INDENTATION = 8f;
    public static final float BEFORE_SPACING = 12f;

    /**
     * Returns the name of the appraisal nolij pdf file.
     *
     * @param appraisal
     * @param dirName       the directory Nolij PDF files resides.
     * @param environment   either "prod" or "dev2"
     * @return filename     composed of: dirname+prod_pass-[PIDM]_[FISCAL YEAR]_[POSITION NUMBER]-.pdf
     * @throws Exception    If the environment is not valid
     */
    public static String getNolijFileName(Appraisal appraisal, String dirName, String environment)
            throws Exception {
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
        filename += environment + "_pass-" + pidm + "_" + fiscalYear + "_" + positionNo + "-.pdf";

        return filename;
    }

    /**
     * Generates a PDF file for the given appraisal using the permission rule. It writes the
     * PDF file to disk.
     *
     * @param appraisal     Appraisal object
     * @param rule          PermissionRule object to decide what sections to display
     * @param filename      Full path and filename used to store the PDF
     * @param resource      ResourceBundle object
     * @param rootDir       Root directory where the images and other resources can be found
     * @throws Exception
     */
    public static void createPDF(Appraisal appraisal, PermissionRule rule, String filename,
                                 ResourceBundle resource, String rootDir) throws Exception {

        //@todo: escape any text before writing it to the PDF doc??

        Rectangle pageSize = new Rectangle(612f, 792f);
        Document document = new Document(pageSize, 50f, 40f, 24f, 24f);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();

        document.add(getLetterHead(resource, rootDir));
        document.add(getInfoTable(appraisal, resource, rule));
        addAssessments(appraisal, rule, resource, document);
        addEvaluation(appraisal, rule, resource, document, rootDir);
        addRebuttal(appraisal, rule, resource, document);
        if (appraisal.getEmployeeSignedDate() != null) {
            document.add(getSignatureTable(appraisal, rule, resource, document));
        }
        document.close();
    }

    /**
     * Handles deleting the temporary PDF file that the user downloads
     *
     * @param filename      Full path and name of temporary PDF file
     * @return
     */
    public static boolean deletePDF(String filename) throws Exception {
        File pdfFile = new File(filename);

        return pdfFile.delete();
    }

    /**
     * Returns a PdfPTable object that contains the signature table displayed at the bottom of the
     * appraisal form.
     *
     * @param appraisal
     * @param rule
     * @param resource
     * @param document
     * @return
     * @throws DocumentException
     */
    private static PdfPTable getSignatureTable(Appraisal appraisal, PermissionRule rule,
                                               ResourceBundle resource, Document document) throws DocumentException {
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
        if (dispalyEmployeeRebuttal(rule)) {
            String employeeName = job.getEmployee().getName();
            String employeeSignDate = PassUtil.formatDate(appraisal.getEmployeeSignedDate());
            employeeCell.addElement(new Phrase(employeeName, FONT_10));
            employeeDateCell.addElement(new Phrase(employeeSignDate, FONT_10));
        }
        if (appraisal.getReleaseDate() != null) {
            String supervisorName = job.getCurrentSupervisor().getEmployee().getName();
            String supervisorSignDate = PassUtil.formatDate(appraisal.getReleaseDate());
            supervisorCell.addElement(new Phrase(supervisorName, FONT_10));
            supervisorDateCell.addElement(new Phrase(supervisorSignDate, FONT_10));
        }

        // Add Left Padding column that spans to all the rows of the table
        PdfPCell leftPaddingCell = new PdfPCell();
        leftPaddingCell.setRowspan(signatureTableMaxRows);
        leftPaddingCell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM + Rectangle.TOP);
        signatureTable.addCell(leftPaddingCell);

        signatureTable.addCell(employeeCell);
        signatureTable.addCell(employeeDateCell);

        PdfPCell middlePaddingCell = new PdfPCell();
        middlePaddingCell.setRowspan(signatureTableMaxRows-2);
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
        employeeSignLabel.setPhrase(new Phrase(employeeSignText, FONT_10));
        employeeSignLabel.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(employeeSignLabel);

        String dateText = resource.getString("appraisal-date");
        PdfPCell dateCell = new PdfPCell();
        dateCell.setColspan(dateColSpan);
        dateCell.setPhrase(new Phrase(dateText, FONT_10));
        dateCell.setVerticalAlignment(Element.ALIGN_TOP);
        dateCell.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(dateCell);

        PdfPCell supervisorSignLabel = new PdfPCell();
        supervisorSignLabel.setColspan(nameColSpan);
        String supervisorSignText = resource.getString("appraisal-supervisor-signature");
        supervisorSignLabel.setPhrase(new Phrase(supervisorSignText, FONT_10));
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
        employeeDescCell.setPhrase(new Paragraph(employeeSignDesc, FONT_9));
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
            String reviewDate = PassUtil.formatDate(appraisal.getReviewSubmitDate());
            reviewerCell.setPhrase(new Phrase(reviewerName, FONT_10));
            reviewerDateCell.setPhrase(new Phrase(reviewDate, FONT_10));
        }
        signatureTable.addCell(reviewerCell);
        signatureTable.addCell(reviewerDateCell);
        /** End ROW 4 **/

        /** Begin ROW 5 **/
        String reviewerLabel = resource.getString("role-reviewer");
        PdfPCell reviewerCellDesc = new PdfPCell(new Phrase(reviewerLabel, FONT_10));
        reviewerCellDesc.setColspan(nameColSpan);
        reviewerCellDesc.setBorder(Rectangle.TOP);
        signatureTable.addCell(reviewerCellDesc);
        dateCell.setBorder(Rectangle.TOP);
        signatureTable.addCell(dateCell);
        /** End ROW 5 **/

        /** Begin ROW 6 **/
        String electronicSignature = resource.getString("appraisal-electronic-signature-desc");
        PdfPCell electronicSignatureCell = new PdfPCell();
        electronicSignatureCell.addElement(new Phrase(electronicSignature,  FONT_ITALIC_9));
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
     * @param appraisal
     * @param rule
     * @param resource
     * @param document
     * @throws DocumentException
     */
    private static void addRebuttal(Appraisal appraisal, PermissionRule rule, ResourceBundle resource,
                                       Document document) throws DocumentException {
        boolean displayEmployeeRebuttal = dispalyEmployeeRebuttal(rule);
        boolean displayRebuttalRead = StringUtils.containsAny(rule.getRebuttalRead(), "ev");

        if (displayEmployeeRebuttal && appraisal.getRebuttal() != null) {
            String rebuttalLblText= resource.getString("appraisal-employee-response").toUpperCase();
            Paragraph rebuttalLbl = new Paragraph(rebuttalLblText, FONT_BOLD_10);
            rebuttalLbl.setSpacingBefore(BEFORE_SPACING);
            document.add(rebuttalLbl);
            LineSeparator line = new LineSeparator(1, 100, null, Element.ALIGN_CENTER, -2);
            document.add(line);

            Paragraph rebuttalText = new Paragraph(appraisal.getRebuttal(), FONT_9);
            rebuttalText.setIndentationLeft(LEFT_INDENTATION);
            document.add(rebuttalText);
        }
    }

    /**
     * Specifies whether or not the employee rebuttal should be displayed.
     *
     * @param rule
     * @return
     */
    private static boolean dispalyEmployeeRebuttal(PermissionRule rule) {
        return StringUtils.containsAny(rule.getEmployeeResponse(), "ev");
    }


    /**
     * Adds the evaluation section (appraisal + rating) to the PDF document.
     *
     * @param appraisal
     * @param rule
     * @param resource
     * @param document
     * @param rootDir   Prefix for img file path
     * @throws DocumentException
     * @throws IOException
     */
    private static void addEvaluation(Appraisal appraisal, PermissionRule rule, ResourceBundle resource,
                                      Document document, String rootDir) throws DocumentException, IOException {
        boolean displayAppraisal = StringUtils.containsAny(rule.getEvaluation(), "ev");
        if (displayAppraisal) {
            String appraisalSectionText = resource.getString("appraisal-summary");
            appraisalSectionText = appraisalSectionText.toUpperCase();
            Paragraph appraisalSectionLbl = new Paragraph(appraisalSectionText, FONT_BOLD_10);
            appraisalSectionLbl.setSpacingBefore(BEFORE_SPACING);
            document.add(appraisalSectionLbl);
            LineSeparator line = new LineSeparator(1, 100, null, Element.ALIGN_CENTER, -2);
            document.add(line);

            Paragraph evaluationLbl = new Paragraph(resource.getString("appraisal-evaluation"), FONT_BOLDITALIC_10);
            Paragraph evaluationText = new Paragraph(appraisal.getEvaluation(), FONT_9);
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
            PdfPTable rating = new PdfPTable(ratingMaxCols);
            PdfPCell emptyLeftCol = new PdfPCell();
            emptyLeftCol.setBorder(Rectangle.NO_BORDER);
            emptyLeftCol.setRowspan(4);
            rating.addCell(emptyLeftCol);
            rating.setWidthPercentage(100f);
            PdfPCell cell;

            // Use an image for the unchecked box
            Image checkedImg = Image.getInstance(rootDir + IMAGE_CHECKBOX_CHECKED);
            checkedImg.scaleToFit(10f, 10f);
            PdfPCell checkedBox = new PdfPCell(checkedImg, false);
            checkedBox.setColspan(1);
            checkedBox.setVerticalAlignment(Element.ALIGN_BOTTOM);
            checkedBox.setBorder(Rectangle.NO_BORDER);

            // Use an image for the checked box.
            Image uncheckedImg = Image.getInstance(rootDir + IMAGE_CHECKBOX_UNCHECKED);
            uncheckedImg.scaleToFit(10f, 10f);
            PdfPCell uncheckedBox = new PdfPCell(uncheckedImg, false);
            uncheckedBox.setColspan(1);
            uncheckedBox.setVerticalAlignment(Element.ALIGN_BOTTOM);
            uncheckedBox.setBorder(Rectangle.NO_BORDER);

            if (appraisal.getRating() != null && appraisal.getRating() == 1) {
                rating.addCell(checkedBox);
            } else {
                rating.addCell(uncheckedBox);
            }
            cell = new PdfPCell(new Paragraph("1. " + resource.getString("appraisal-rating-1"), FONT_9));
            cell.setColspan(ratingMaxCols - 2);
            cell.setBorder(Rectangle.NO_BORDER);
            rating.addCell(cell);

            if (appraisal.getRating() != null && appraisal.getRating() == 2) {
                rating.addCell(checkedBox);
            } else {
                rating.addCell(uncheckedBox);
            }
            cell = new PdfPCell(new Paragraph("2. " + resource.getString("appraisal-rating-2"), FONT_9));
            cell.setColspan(ratingMaxCols - 2);
            cell.setBorder(Rectangle.NO_BORDER);
            rating.addCell(cell);

            if (appraisal.getRating() != null && appraisal.getRating() == 3) {
                rating.addCell(checkedBox);
            } else {
                rating.addCell(uncheckedBox);
            }
            cell = new PdfPCell(new Paragraph("3. " + resource.getString("appraisal-rating-3"), FONT_9));
            cell.setColspan(ratingMaxCols - 2);
            cell.setBorder(Rectangle.NO_BORDER);
            rating.addCell(cell);

            if (appraisal.getRating() != null && appraisal.getRating() == 4) {
                rating.addCell(checkedBox);
            } else {
                rating.addCell(uncheckedBox);
            }
            cell = new PdfPCell(new Paragraph("4. " + resource.getString("appraisal-rating-4"), FONT_9));
            cell.setColspan(ratingMaxCols - 2);
            cell.setBorder(Rectangle.NO_BORDER);
            rating.addCell(cell);

            document.add(rating);
        }
    }

    /**
     * Returns a PdfPTable object that contains the appraisal and job summary information.
     *
     * @param appraisal Appraisal object
     * @param resource  ResourceBundle object
     * @param rule      PermissionRule object
     * @return PdfPTable
     */
    public static PdfPTable getInfoTable(Appraisal appraisal, ResourceBundle resource,
                                         PermissionRule rule) {
        PdfPTable info = new PdfPTable(4);
        info.setWidthPercentage(100f);
        info.setKeepTogether(true);

        PdfPCell cell;
        Paragraph p;
        Chunk c;

        c = new Chunk(resource.getString("employee")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getEmployee().getName(), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        cell.setColspan(2);
        info.addCell(cell);

        c = new Chunk(resource.getString("ts-org-code-desc")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getOrgCodeDescription(), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("jobTitle")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getJobTitle(), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("supervisor")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getSupervisor().getEmployee().getName(), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("appraisal-employee-id")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getEmployee().getOsuid(), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("position-class")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getPositionClass(), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("position-no")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getJob().getPositionNumber(), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("appraisal-status")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(resource.getString(appraisal.getStatus()), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        cell.setColspan(2);
        info.addCell(cell);

        c = new Chunk(resource.getString("reviewPeriod")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(appraisal.getReviewPeriod(), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        String appraisalTypeKey = "appraisal-type-" + appraisal.getType();
        c = new Chunk(resource.getString("appraisal-type")+": ", FONT_10);
        p = new Paragraph(c);
        c = new Chunk(resource.getString(appraisalTypeKey), FONT_BOLD_10);
        p.add(c);
        cell = new PdfPCell(p);
        cell.setPaddingLeft(4);
        cell.setPaddingBottom(4);
        info.addCell(cell);

        c = new Chunk(resource.getString("appraisal-rating")+": ", FONT_10);
        p = new Paragraph(c);
        String rating = "";
        boolean displayRating = StringUtils.containsAny(rule.getEvaluation(), "ev");
        if (appraisal.getRating() != null && displayRating) {
            rating = resource.getString("appraisal-rating-pdf-" + appraisal.getRating());
        }
        c = new Chunk(rating, FONT_BOLD_10);
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
     * @param resource  ResourceBundle object
     * @param rootDir   Prefix for img file path
     * @return PdfPTable
     * @throws Exception
     */
    public static PdfPTable getLetterHead(ResourceBundle resource, String rootDir) throws Exception {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100f);
        Paragraph paragraph;
        PdfPCell cell;

        Image image = Image.getInstance(rootDir + IMAGE_OSU_LOGO);
        image.scaleToFit(116.625f, 35.875f);
        cell = new PdfPCell(image, false);
        cell.setBorder(Rectangle.NO_BORDER);
        header.addCell(cell);


        paragraph = new Paragraph(resource.getString("office-hr"), FONT_10);
        paragraph.setAlignment(Element.ALIGN_MIDDLE);
        cell = new PdfPCell(paragraph);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setBorder(Rectangle.NO_BORDER);
        header.addCell(cell);

        paragraph = new Paragraph(resource.getString("classified-employee-appraisal-record"), FONT_BOLD_13);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        cell = new PdfPCell(paragraph);
        cell.setBorder(Rectangle.NO_BORDER);
        header.addCell(cell);

        header.setSpacingAfter(12f);

        return header;
    }


    /**
     * Adds each evaluation criteria, goals, employee results and supervisor results based on
     * the permission rule.
     *
     * @param appraisal
     * @param rule
     * @param resource
     * @param document
     * @throws DocumentException
     */
    private static void addAssessments(Appraisal appraisal, PermissionRule rule, ResourceBundle resource,
                                       Document document) throws DocumentException {
        Paragraph sectionText;
        int i = 0;

        for (Assessment assessment : (java.util.List<Assessment>) appraisal.getSortedAssessments()) {
            i++;
            String areaText = i + ". " + assessment.getCriterionDetail().getAreaID().getName().toUpperCase() + ":";
            String descriptionText = " (" + assessment.getCriterionDetail().getDescription() + ")";

            Chunk area = new Chunk(areaText, FONT_BOLD_10);
            area.setUnderline(1f, -2f);

            Phrase description = new Phrase(descriptionText, FONT_9);
            sectionText = new Paragraph();
            sectionText.add(area);
            sectionText.add(description);
            sectionText.setSpacingBefore(BEFORE_SPACING);
            document.add(sectionText);

            boolean displayGoals = StringUtils.containsAny(rule.getGoals(), "ev");
            boolean displayEmployeeResults = StringUtils.containsAny(rule.getResults(), "ev");
            boolean displaySupervisorResults = StringUtils.containsAny(rule.getSupervisorResults(), "ev");
            if (displayGoals) {
                Paragraph goalsLabel = new Paragraph(resource.getString("appraisal-goals"), FONT_BOLDITALIC_10);
                Paragraph goals = new Paragraph(assessment.getGoal(), FONT_9);
                goalsLabel.setIndentationLeft(LEFT_INDENTATION);
                goals.setIndentationLeft(LEFT_INDENTATION);
                document.add(goalsLabel);
                document.add(goals);
            }

            if (displayEmployeeResults) {
                Paragraph resultsLabel = new Paragraph(resource.getString("appraisal-employee-results"),
                        FONT_BOLDITALIC_10);
                Paragraph employeeResult = new Paragraph(assessment.getEmployeeResult(), FONT_9);
                resultsLabel.setSpacingBefore(BEFORE_SPACING);
                resultsLabel.setIndentationLeft(LEFT_INDENTATION);
                employeeResult.setIndentationLeft(LEFT_INDENTATION);
                document.add(resultsLabel);
                document.add(employeeResult);
            }

            if (displaySupervisorResults) {
                Paragraph supervisorResultLbl = new Paragraph(resource.getString("appraisal-result-comments"),
                        FONT_BOLDITALIC_10);
                Paragraph supervisorResult = new Paragraph(assessment.getSupervisorResult(), FONT_9);
                supervisorResultLbl.setSpacingBefore(BEFORE_SPACING);
                supervisorResultLbl.setIndentationLeft(LEFT_INDENTATION);
                supervisorResult.setIndentationLeft(LEFT_INDENTATION);
                document.add(supervisorResultLbl);
                document.add(supervisorResult);
            }

        }
    }
}
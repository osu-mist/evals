package edu.osu.cws.evals.util;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.hibernate.PermissionRuleMgr;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.commons.lang.WordUtils;


public class EvalsPDFBox {

    private static PDFont font = PDType1Font.HELVETICA;
    private static PDFont fontBold = PDType1Font.HELVETICA_BOLD;
    private static PDFont fontBoldItalic = PDType1Font.HELVETICA_BOLD_OBLIQUE;
    private static float fontSize = 10.0f;
    private static float fontSizeBold = 11.0f;
    private static float fontSizeHeaderBold = 14.0f;

    private static float sideMargin = 50f;
    private static float topMargin = 22f;
    private static float lineHeight = 13f;
    private static float tabSize = 12.5f;

    private static final String IMAGE_OSU_LOGO = "images/pdf-osu-logo.png";
    private static final float OSU_LOGO_HEIGHT = 35.875f;
    private static final float OSU_LOGO_WIDTH = 116.625f;
    private static final String IMAGE_CHECKBOX_CHECKED = "images/pdf-checkbox-checked.jpg";
    private static final String IMAGE_CHECKBOX_UNCHECKED = "images/pdf-checkbox-unchecked.jpg";
    private static final float CHECKBOX_HEIGHT = 10f;
    private static final float CHECKBOX_WIDTH = 10f;

    private static float baseRowHeight = 17f;
    private static float cellMargin = 5f;

    private ResourceBundle resource;
    private String rootPath;

    private Appraisal appraisal;
    private List<Rating> ratings;
    private PermissionRule permRule;
    private String dirName;
    private String environment;
    private String suffix;

    private PDDocument doc;
    private List<PDPage> pages;
    private PDPageContentStream contStream;
    private float curLine;

    /**
     * @param rootPath      Root directory where the images and other resources can be found
     * @param appraisal     Appraisal object
     * @param resource      ResourceBundle object
     * @param dirName       the directory PDF files resides.
     * @param suffix        the suffix for the pdf file name.
     * @param env           either "prod" or "dev2"
     * @param ratings       Sorted list of ratings
     */
    public EvalsPDFBox(String rootPath, Appraisal appraisal, ResourceBundle resource, String dirName, String env, String suffix, List<Rating> ratings) throws Exception {
        this.resource = resource;
        this.rootPath = rootPath + Constants.PDF_DIR;
        this.appraisal = appraisal;
        this.dirName = dirName;
        this.environment = env;
        this.suffix = suffix;
        this.ratings = ratings;
        this.permRule = appraisal.getPermissionRule();
    }

    public String createPDF() throws Exception {
        String fileName = getFileName();

        doc = new PDDocument();
        try {
            pages = new ArrayList<PDPage>();
            addPage();

            addHeader();

            addInfoTable();

            addGoalVersions();

            if (StringUtils.containsAny(permRule.getEvaluation(), "ev")) {
                addEvaluation();
            }

            if (StringUtils.containsAny(permRule.getEmployeeResponse(), "ev") && appraisal.getEmployeeSignedDate() != null) {
                addEmployeeResponse();
            }

            addCriteriaLegend();


            if (contStream != null) {
                contStream.close();
            }

            File file = new File(fileName);
            file.createNewFile();
            doc.save(file);
        } finally {
            doc.close();
        }

        return fileName;
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
        String aptTypeSuffix = getAppointmentTypeSuffix();
        filename = dirName;
        filename += environment + "_evals-" + pidm + "_" + fiscalYear + "_" + positionNo + aptTypeSuffix + "-.pdf";

        return filename;
    }

    /**
     * Returns the appropriate suffix based on appointmentType.
     * @return
     */
    public String getAppointmentTypeSuffix() throws IOException {
        String aptTypeSuffix;
        String aptType = appraisal.getAppointmentType();
        if(aptType.equals(AppointmentType.PROFESSIONAL_FACULTY)) {
            aptTypeSuffix = suffix;
        }
        else {
            aptTypeSuffix = "";
        }
        return aptTypeSuffix;
    }

    private void addHeader() throws IOException {
        writeImage(rootPath + IMAGE_OSU_LOGO, sideMargin, curLine, OSU_LOGO_WIDTH, OSU_LOGO_HEIGHT);
        addToCurLine(OSU_LOGO_HEIGHT);

        String officeHr = resource.getString("office-hr");
        float headerTextWidth = getTextWidth(officeHr, font, fontSize);
        float headertextX = getPageWidth() / 2f - (headerTextWidth / 2);
        float headertextY = curLine;
        writeText(font, fontSize, headertextX, headertextY, officeHr);

        String jobType = appraisal.getJob().getAppointmentType();
        String appraisalTitle = resource.getString("appraisal-title");
        float textWidth = getTextWidth(appraisalTitle, fontBold, fontSizeHeaderBold);
        headertextX = getPageWidth() - sideMargin - textWidth;
        writeText(fontBold, fontSizeHeaderBold, headertextX, headertextY, appraisalTitle);
        float perfEvalHeight = getTextHeight(fontBold, fontSizeHeaderBold);
        writeText(fontBold, fontSizeHeaderBold, headertextX, headertextY + perfEvalHeight, jobType);

        addToCurLine(lineHeight);
    }

    private void addInfoTable() throws IOException {
        Job job = appraisal.getJob();
        Employee emp = job.getEmployee();

        String empName = emp.getLastName() + ", " + emp.getFirstName();
        String supName = "";
        if (job.getSupervisor() != null) {
            supName = job.getSupervisor().getEmployee().getName();
        }
        String appraisalTypeKey = "appraisal-type-annual";
        if (appraisal.getType() != null) {
            appraisalTypeKey = "appraisal-type-" + appraisal.getType();
        }
        String ratingText = "";
        boolean displayRating = StringUtils.containsAny(permRule.getEvaluation(), "ev");
        // boolean displayRating = true;
        if (appraisal.getRating() != null && displayRating) {
            for (Rating rating : ratings) {
                if (appraisal.getRating().equals(rating.getRate())) {
                    ratingText = rating.getName();
                }
            }
        }
        String[][][] content = new String[][][]{
            { { resource.getString("employee") + ": ", empName }, { resource.getString("ts-org-code-desc") + ": ", job.getOrgCodeDescription() } },
            { { resource.getString("jobTitle") + ": ", job.getJobTitle() }, { resource.getString("supervisor") + ": ", supName } },
            { { resource.getString("appraisal-employee-id") + ": ", emp.getOsuid() }, { resource.getString("position-class") + ": ", job.getPositionClass() }, { resource.getString("position-no") + ": ", job.getPositionNumber() }, { resource.getString("appraisal-type-pdf") + ": ", resource.getString(appraisalTypeKey) } },
            { { resource.getString("reviewPeriod") + ": ", appraisal.getReviewPeriod() }, { "", "" }, { resource.getString("appraisal-status") + ": ", resource.getString(appraisal.getViewStatus()) }, { resource.getString("appraisal-rating") + ": ", ratingText } }
        };
        writeTable(curLine, content);
    }

    private void addGoalVersions() throws IOException {
        int displayedGoals = 0;

        boolean displayGoals = StringUtils.containsAny(permRule.getApprovedGoals(), "ev");
        if(displayGoals) {
            List<GoalVersion> approvedGoalsVersions = appraisal.getApprovedGoalsVersions();
            for (GoalVersion goalVersion : approvedGoalsVersions) {
                String goalsApproved = resource.getString("appraisal-goals-approved-on") + " " +
                        new DateTime(goalVersion.getGoalsApprovedDate()).toString(Constants.DATE_FORMAT) + ":";
                writeText(fontBold, 12f, sideMargin, curLine, goalsApproved, true);
                addToCurLine(lineHeight * 2);

                List<Assessment> sortedAssessments = goalVersion.getSortedAssessments();
                displayedGoals = addGoals(displayedGoals, sortedAssessments);
            }
        }
    }

    private int addGoals(int displayedGoals, List<Assessment> sortedAssessments) throws IOException {
        boolean displayEmployeeResults = StringUtils.containsAny(permRule.getResults(), "ev");
        boolean displaySupervisorResults = StringUtils.containsAny(permRule.getSupervisorResults(), "ev");

        for(Assessment assessment : sortedAssessments) {
            displayedGoals ++;

            // add employee goal
            String goalLabel = resource.getString("appraisal-goals") + displayedGoals;
            writeText(fontBoldItalic, fontSize, sideMargin, curLine, goalLabel, true);
            addToCurLine(lineHeight);
            writeText(font, fontSize, sideMargin + tabSize, curLine, assessment.getGoal(), false, false, true);
            addToCurLine(lineHeight * 2);

            addAssessmentCriteria(assessment);

            if (!assessment.isNewGoal()) {
                if (displayEmployeeResults) {
                    // add employee result
                    String empResultText = resource.getString("appraisal-employee-results");
                    writeText(fontBoldItalic, fontSize, sideMargin + tabSize, curLine, empResultText);
                    addToCurLine(lineHeight);
                    writeText(font, fontSize, sideMargin + tabSize, curLine, assessment.getEmployeeResult(), false, false, true);
                    addToCurLine(lineHeight * 2);
                }

                if (displaySupervisorResults) {
                    // add supervisor result
                    String supResultText = resource.getString("appraisal-result-comments");
                    writeText(fontBoldItalic, fontSize, sideMargin + tabSize, curLine, supResultText);
                    addToCurLine(lineHeight);
                    writeText(font, fontSize, sideMargin + tabSize, curLine, assessment.getSupervisorResult(), false, false, true);
                    addToCurLine(lineHeight * 2);
                }
            }
        }
        addToCurLine(lineHeight);

        return displayedGoals;
    }

    private void addAssessmentCriteria(Assessment assessment) throws IOException {
        writeText(fontBoldItalic, fontSize, sideMargin + tabSize, curLine, resource.getString("appraisal-selected-criteria"));
        addToCurLine(lineHeight);
        float checkboxPadding = 3f;
        float criteriaPadding = 20f;
        float initialx = sideMargin + tabSize;
        float x = initialx;

        for (AssessmentCriteria assessmentCriteria : assessment.getSortedAssessmentCriteria()) {
            String checkboxPath;
            if (assessmentCriteria.getChecked() != null && assessmentCriteria.getChecked()) {
                checkboxPath = rootPath + IMAGE_CHECKBOX_CHECKED;
            } else {
                checkboxPath = rootPath + IMAGE_CHECKBOX_UNCHECKED;
            }
            String criteriaText = assessmentCriteria.getCriteriaArea().getName();
            float criteriaTextWidth = getTextWidth(criteriaText, font, fontSize);
            if (x + CHECKBOX_WIDTH + checkboxPadding + criteriaTextWidth > getPageWidth() - sideMargin) {
                x = initialx;
                addToCurLine(CHECKBOX_HEIGHT + lineHeight);
            }
            writeImage(checkboxPath, x, curLine, CHECKBOX_WIDTH, CHECKBOX_HEIGHT);
            x += CHECKBOX_WIDTH + checkboxPadding;
            writeText(font, fontSize, x, curLine - CHECKBOX_HEIGHT, criteriaText);
            x += criteriaTextWidth + criteriaPadding;
        }

        addToCurLine(CHECKBOX_HEIGHT + lineHeight * 3);
    }

    private void addEvaluation() throws IOException {
        // add summary title
        String appraisalSectionText = resource.getString("appraisal-summary");
        appraisalSectionText = appraisalSectionText.toUpperCase();
        writeText(fontBold, fontSizeBold, sideMargin, curLine, appraisalSectionText, true, true, false);
        addToCurLine(lineHeight + 5);

        // add overall evaluation
        String evaluationLbl = resource.getString("appraisal-evaluation");
        String evaluationText = appraisal.getEvaluation();
        writeText(fontBoldItalic, fontSizeBold, sideMargin + tabSize, curLine, evaluationLbl);
        addToCurLine(lineHeight);
        writeText(font, fontSize, sideMargin + tabSize, curLine, evaluationText, false, false, true);
        addToCurLine(lineHeight * 2);

        addRating();

        // only Classified IT evals get the salary table
        if (appraisal.getIsSalaryUsed()) {
            addSalaryTable();
        }

        addToCurLine(lineHeight * 2);
    }

    private void addRating() throws IOException {
        // rating section title
        String ratingLbl = resource.getString("appraisal-select-rating");
        writeText(fontBoldItalic, fontSizeBold, sideMargin + tabSize, curLine, ratingLbl);
        addToCurLine(lineHeight);

        float checkboxX = sideMargin + tabSize;
        for (Rating rating : ratings) {
            if (appraisal.getRating() != null && appraisal.getRating().equals(rating.getRate())) {
                // add checked box
                writeImage(rootPath + IMAGE_CHECKBOX_CHECKED, checkboxX, curLine + CHECKBOX_HEIGHT, CHECKBOX_WIDTH, CHECKBOX_HEIGHT);
            } else {
                // add unchecked box
                writeImage(rootPath + IMAGE_CHECKBOX_UNCHECKED, checkboxX, curLine + CHECKBOX_HEIGHT, CHECKBOX_WIDTH, CHECKBOX_HEIGHT);
            }

            writeText(font, fontSize, checkboxX + CHECKBOX_WIDTH + 7, curLine, rating.toString());
            addToCurLine(lineHeight + 2);
        }
    }

    private void addSalaryTable() throws IOException {
        addToCurLine();

        String salarySectionTitle = resource.getString("appraisal-salary-section-title");
        writeText(fontBoldItalic, fontSizeBold, sideMargin + tabSize, curLine, salarySectionTitle);
        addToCurLine();

        // Above/Below Control Point
        String belowOrAboveOrAt = resource.getString("appraisal-salary-at");
        if (appraisal.getSalary().getCurrent() > appraisal.getSalary().getMidPoint()) {
            belowOrAboveOrAt = resource.getString("appraisal-salary-above");
        } else if (appraisal.getSalary().getCurrent() < appraisal.getSalary().getMidPoint()) {
            belowOrAboveOrAt = resource.getString("appraisal-salary-below");
        }
        // Recommended Increase
        Double increase = appraisal.getSalary().getIncrease();
        if (increase == null) {
            increase = 0d;
        }
        DateTime salaryEligibilityDate = new DateTime(appraisal.getSalaryEligibilityDate()).withTimeAtStartOfDay();
        String salaryEligibilityString = salaryEligibilityDate.toString("MM/dd");
        String[][] salaryTable = new String[][] {
            {
                resource.getString("appraisal-salary-current") + ": ",
                resource.getString("appraisal-salary-control-point-value") + ": ",
                resource.getString("appraisal-salary-control-low") + ": ",
                resource.getString("appraisal-salary-control-high") + ": ",
                resource.getString("appraisal-salary-control-point") + ": ",
                resource.getString("appraisal-salary-recommended-increase") + ": ",
                resource.getString("appraisal-salary-after-increase") + ": ",
                resource.getString("appraisal-salary-eligibility-date") + ": "
            },
            {
                CWSUtil.formatCurrency(appraisal.getSalary().getCurrent()),
                CWSUtil.formatCurrency(appraisal.getSalary().getMidPoint()),
                CWSUtil.formatCurrency(appraisal.getSalary().getLow()),
                CWSUtil.formatCurrency(appraisal.getSalary().getHigh()),
                belowOrAboveOrAt,
                increase.toString(),
                CWSUtil.formatCurrency(appraisal.getSalary().getCurrent() * (1 + increase / 100)),
                salaryEligibilityString
            }
        };

        float tableWidth = getPageWidth() - sideMargin * 2;
        contStream.setLineWidth(.5f);
        float y = curLine;
        float nextY = y;
        float textX = sideMargin + cellMargin;
        float textY = y - (baseRowHeight * (float).75);
        float rowHeight = baseRowHeight;
        for (int i = 0; i <= salaryTable.length; i++) {
            // draw row line
            contStream.drawLine(sideMargin, nextY, sideMargin + tableWidth, nextY);

            if (i < salaryTable.length) {
                PDFont curFont = font;
                float curFontSize = fontSize;
                if (i == 0) {
                    curFont = fontBold;
                    curFontSize = fontSizeBold;
                }

                float nextX = sideMargin;
                float baseColWidth = tableWidth / (float)salaryTable[i].length;
                rowHeight = baseRowHeight;
                float textHeight = getTextHeight(font, fontSize);
                int fontAdjustedColWidth = Math.round((baseColWidth - 25f) / 5f);
                for (int j = 0; j < salaryTable[i].length; j++) {
                    String wrappedText[] = WordUtils.wrap(salaryTable[i][j], fontAdjustedColWidth, "\n", true).split("\n");
                    float newRowHeight = wrappedText.length * lineHeight + 2f;

                    if (rowHeight < newRowHeight) {
                        rowHeight = newRowHeight;
                    }
                }

                for (int j = 0; j <= salaryTable[i].length; j++) {
                    // draw column line
                    contStream.drawLine(nextX, nextY, nextX, nextY - rowHeight);

                    if (j < salaryTable[i].length) {
                        float startTextY = textY;
                        String text = salaryTable[i][j];
                        float textWidth = getTextWidth(text, font, fontSize);

                        if (textWidth + 5f > baseColWidth) {
                            String[] wrappedText = WordUtils.wrap(text, fontAdjustedColWidth, "\n", true).split("\n");
                            for (int k=0; k < wrappedText.length; k++) {
                                writeText(curFont, curFontSize, textX, textY, wrappedText[k]);
                                textY -= textHeight;
                            }
                        } else {
                            writeText(curFont, curFontSize, textX, textY, text);
                        }
                        textX += baseColWidth;
                        nextX += baseColWidth;
                        textY = startTextY;
                    }
                }
                textY -= rowHeight;
                textX = sideMargin + cellMargin;
                nextY -= rowHeight;
            }
        }

        addToCurLine((curLine - nextY) - lineHeight);
        addToCurLine(lineHeight * 2);
    }

    private void addEmployeeResponse() throws IOException {
        addSignature();

        if (appraisal.getRebuttal() != null) {
            String rebuttalType =  "rebuttal";
            if (appraisal.getJob().isUnclassified()) {
                rebuttalType = "feedback";
            }

            // add section title
            addToCurLine();
            String rebuttalLblText= resource.getString("appraisal-employee-response-" + rebuttalType).toUpperCase();
            writeText(fontBold, fontSizeBold, sideMargin, curLine, rebuttalLblText, true, true, false);
            addToCurLine();

            // add employee response
            writeText(font, fontSize, sideMargin + tabSize, curLine, appraisal.getRebuttal(), false, false, true);

            addToCurLine();
        }
    }

    private void addSignature() throws IOException {
        // create new page if signature box doesn't fit on current page
        if (curLine < 200f) {
            addPage();
            addToCurLine(lineHeight * 2);
        }

        // add border top
        float borderMargin = 10f;
        float borderStartHeight = curLine + borderMargin + fontSize;
        contStream.setLineWidth(.5f);
        contStream.drawLine(sideMargin, borderStartHeight, getPageWidth() - sideMargin, borderStartHeight);

        // add border sides if this box will overflow to next page
        if (curLine < 200f) {
            contStream.drawLine(sideMargin, borderStartHeight, sideMargin, 0);
            contStream.drawLine(getPageWidth() - sideMargin, borderStartHeight, getPageWidth() - sideMargin, 0);
        }

        float cellSize = (getPageWidth() - (2 * (sideMargin + tabSize))) / 4f;
        float cellStart = sideMargin + tabSize;

        // employee name/signed date
        String employeeName = appraisal.getJob().getEmployee().getName();
        DateTime employeeSignedDate = new DateTime(appraisal.getEmployeeSignedDate()).withTimeAtStartOfDay();
        String employeeSignDate = employeeSignedDate.toString(Constants.DATE_FORMAT);
        writeText(font, fontSize, cellStart, curLine, employeeName);
        writeText(font, fontSize, cellSize + cellStart, curLine, employeeSignDate);
        // add underline
        contStream.drawLine(sideMargin + tabSize, curLine - 3f, cellSize * 2 + cellStart - sideMargin, curLine - 3f);

        // supervisor name/signed date
        if (appraisal.getReleaseDate() != null) {
            DateTime releaseDate = new DateTime(appraisal.getReleaseDate()).withTimeAtStartOfDay();
            String supervisorName = "";
            if (appraisal.getEvaluator() != null) {
                supervisorName = appraisal.getEvaluator().getName();
            }
            String supervisorSignDate = releaseDate.toString(Constants.DATE_FORMAT);
            writeText(font, fontSize, cellSize * 2 + cellStart, curLine, supervisorName);
            writeText(font, fontSize, cellSize * 3 + cellStart, curLine, supervisorSignDate);
            // add underline
            contStream.drawLine(cellSize * 2 + cellStart, curLine - 3f, cellSize * 4 + cellStart - sideMargin, curLine - 3f);
        }
        addToCurLine(lineHeight);

        // name/date labels
        String employeeSignLabel = resource.getString("appraisal-employee-signature-pdf");
        String dateLabel = resource.getString("appraisal-date");
        String supervisorSignLabel = resource.getString("appraisal-supervisor-signature");
        writeText(font, fontSize, cellStart, curLine, employeeSignLabel);
        writeText(font, fontSize, cellSize + cellStart, curLine, dateLabel);
        writeText(font, fontSize, cellSize * 2 + cellStart, curLine, supervisorSignLabel);
        writeText(font, fontSize, cellSize * 3 + cellStart, curLine, dateLabel);

        addToCurLine(lineHeight * 2);

        // reviewer
        boolean displayReviewer =
                   appraisal.getReleaseDate() != null
                && appraisal.getReviewSubmitDate() != null
                && !appraisal.getJob().isUnclassified();
        if (displayReviewer) {
            String reviewerName = appraisal.getReviewer().getName();
            DateTime reviewSubmitDate = new DateTime(appraisal.getReviewSubmitDate()).withTimeAtStartOfDay();
            String reviewDate = reviewSubmitDate.toString(Constants.DATE_FORMAT);
            writeText(font, fontSize, cellSize * 2 + cellStart, curLine, reviewerName);
            writeText(font, fontSize, cellSize * 3 + cellStart, curLine, reviewDate);
            contStream.setLineWidth(.5f);
            contStream.drawLine(cellSize * 2 + cellStart, curLine - 3f, cellSize * 4 + cellStart - sideMargin, curLine - 3f);
            String reviewerLabel = resource.getString("role-reviewer");
            writeText(font, fontSize, cellSize * 2 + cellStart, curLine - lineHeight, reviewerLabel);
            writeText(font, fontSize, cellSize * 3 + cellStart, curLine - lineHeight, dateLabel);
        }

        // acknowledgement text
        String employeeSignDesc = resource.getString("appraisal-acknowledge-read");
        String supervisorSignDesc = resource.getString("appraisal-supervisor-ack-read");
        writeText(font, fontSize, cellStart, curLine, employeeSignDesc, false, false, true, 10f);
        addToCurLine(lineHeight * 2);
        writeText(font, fontSize, cellStart, curLine, supervisorSignDesc, false, false, true, 10f);

        addToCurLine(lineHeight * 2);
        String electronicSignature = resource.getString("appraisal-electronic-signature-desc");
        writeText(fontBold, fontSizeBold, cellStart, curLine, electronicSignature);

        // add border edges
        contStream.setLineWidth(.5f);
        if (borderStartHeight < curLine) {
            // subtract .1 otherwise the border will overflow to the bottom of the page
            borderStartHeight = getPageHeight() - .1f;
        }
        contStream.drawLine(sideMargin, borderStartHeight, sideMargin, curLine - borderMargin);
        contStream.drawLine(getPageWidth() - sideMargin, borderStartHeight, getPageWidth() - sideMargin, curLine - borderMargin);
        // add border bottom
        contStream.drawLine(sideMargin, curLine - borderMargin, getPageWidth() - sideMargin, curLine - borderMargin);

        addToCurLine(lineHeight * 2);
    }

    private void addCriteriaLegend() throws IOException {
        contStream.setLineWidth(1f);
        contStream.drawLine(sideMargin, curLine, getPageWidth() - sideMargin, curLine);
        addToCurLine();

        String criteriaHeader = resource.getString("appraisal-criteria-legend");
        writeText(fontBoldItalic, fontSizeBold, sideMargin, curLine, criteriaHeader, true);
        addToCurLine();

        List<CriterionArea> sortedCriteriaArea = getSortedCriteria();
        for (CriterionArea criterionArea : sortedCriteriaArea) {
            float curLineStart = curLine;
            writeText(font, fontSize, sideMargin + tabSize, curLine, criterionArea.getName(), false, false, true, 40f);
            // current line will get pushed down if above text wraps
            // this resets it so the description lines up with the criteria title
            addToCurLine(curLine - curLineStart);
            writeText(font, fontSize, getPageWidth() * .3f, curLine, criterionArea.getDescription(), false, false, true);

            addToCurLine(lineHeight * 1.5f);
        }
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

    private void writeImage(String path, float x, float y, float width, float height) throws IOException {
        PDImageXObject pdImage = PDImageXObject.createFromFile(path, doc);
        contStream.drawImage(pdImage, x, y - height, width, height);
    }

    private void writeTable(float y, String[][][] content) throws IOException {
        float tableWidth = getPageWidth() - sideMargin * 2;
        contStream.setLineWidth(.5f);

        float nextY = y;
        float textX = sideMargin + cellMargin;
        float textY = y - (baseRowHeight * (float).75);
        float rowHeight = baseRowHeight;
        for (int i = 0; i <= content.length; i++) {
            // draw row line
            contStream.drawLine(sideMargin, nextY, sideMargin + tableWidth, nextY);

            if (i < content.length) {
                float nextX = sideMargin;
                float baseColWidth = tableWidth / (float)content[i].length;
                rowHeight = findRowHeight(baseRowHeight, baseColWidth, content[i]);
                for (int j = 0; j <= content[i].length; j++) {
                    if (j == content[i].length || !content[i][j][0].isEmpty() || !content[i][j][1].isEmpty()) {
                        // draw column line
                        contStream.drawLine(nextX, nextY, nextX, nextY - rowHeight);

                        if (j < content[i].length) {
                            String text = content[i][j][0];
                            writeText(font, fontSize, textX, textY, text);
                            float textWidth = getTextWidth(text, font, fontSize);

                            // write bold text
                            text = content[i][j][1];
                            float boldX = textX;
                            float boldY = textY;

                            // determine if we need to line break text
                            float boldWidth = getTextWidth(text, fontBold, fontSizeBold);
                            float colWidth = baseColWidth;
                            if (j + 1 < content[i].length && content[i][j + 1][0].isEmpty() && content[i][j + 1][1].isEmpty()) {
                                colWidth = baseColWidth * 2;
                            }
                            boldX += textWidth;
                            if (textWidth + boldWidth > colWidth) {
                                float textHeight = getTextHeight(font, fontSize);
                                String[] splitText = text.split("((?<=[- ]))");
                                writeText(fontBold, fontSizeBold, boldX, boldY, splitText[0]);
                                boldX -= textWidth;
                                boldY -= textHeight;
                                writeText(fontBold, fontSizeBold, boldX, boldY, Arrays.asList(splitText).stream().skip(1).collect(Collectors.joining("")));
                            } else {
                                writeText(fontBold, fontSizeBold, boldX, boldY, text);
                            }
                        }
                    }
                    textX += baseColWidth;
                    nextX += baseColWidth;
                }
                textY -= rowHeight;
                textX = sideMargin + cellMargin;
            }
            nextY -= rowHeight;
        }

        addToCurLine((curLine - nextY) - lineHeight);
    }

    private void writeText(PDFont font, float fontSize, float x, float y, String text) throws IOException {
        writeText(font, fontSize, x, y, text, false);
    }

    private void writeText(PDFont font, float fontSize, float x, float y, String text, boolean underline) throws IOException {
        writeText(font, fontSize, x, y, text, underline, false, false);
    }

    private void writeText(PDFont font, float fontSize, float x, float y, String text, boolean underline, boolean fullUnderline, boolean wordWrap) throws IOException {
        writeText(font, fontSize, x, y, text, underline, fullUnderline, wordWrap, 5f);
    }

    private void writeText(PDFont font, float fontSize, float x, float y, String text, boolean underline, boolean fullUnderline, boolean wordWrap, float wordWrapConstant) throws IOException {
        contStream.setFont(font, fontSize);
        contStream.beginText();
        contStream.moveTextPositionByAmount(x, y);
        if (text != null) {
            if (wordWrap) {
                String[] wrappedText = WordUtils.wrap(text, Math.round((getPageWidth() - x - sideMargin) / wordWrapConstant), "\n", false).split("\n");
                for (int i=0; i < wrappedText.length; i++) {
                    if (i > 0) {
                        if (addToCurLine(lineHeight)) {
                            contStream.setFont(font, fontSize);
                            contStream.beginText();
                            contStream.moveTextPositionByAmount(x, curLine);
                        } else {
                            contStream.moveTextPositionByAmount(0, -lineHeight);
                        }
                    }
                    contStream.drawString(wrappedText[i]);
                }
            } else {
                contStream.drawString(text);
            }
        }
        contStream.endText();

        if(underline) {
            float textWidth;
            if (fullUnderline) {
                textWidth = getPageWidth() - (2 * x);
            } else {
                textWidth = getTextWidth(text, font, fontSize);
            }
            float lineHeight = y - 2f;

            contStream.setLineWidth(1f);
            contStream.drawLine(x, lineHeight, textWidth + x, lineHeight);
        }
    }

    private float findRowHeight(float baseRowHeight, float baseColWidth, String[][] content) throws IOException {
        for (int i = 0; i < content.length; i++) {
            float textWidth = getTextWidth(content[i][0], font, fontSize);
            float boldTextWidth = getTextWidth(content[i][1], fontBold, fontSizeBold);

            if (textWidth + boldTextWidth > baseColWidth) {
                return baseRowHeight * 2;
            }
        }

        return baseRowHeight;
    }

    private static float getTextWidth(String text, PDFont curFont, float curFontSize) throws IOException {
        return curFont.getStringWidth(text) / 1000 * curFontSize;
    }

    private static float getTextHeight(PDFont font, float fontSize) throws IOException { 
        return font.getBoundingBox().getHeight() / 1000 * fontSize;
    }

    private float getPageWidth() {
        return pages.get(0).getMediaBox().getWidth();
    }

    private float getPageHeight() {
        return pages.get(0).getMediaBox().getHeight();
    }

    private PDPage getCurrentPage() {
        return pages.get(pages.size() - 1);
    }

    private void addPage() throws IOException {
        PDPage page = new PDPage();
        page.setMediaBox(PDRectangle.LETTER);
        doc.addPage(page);
        pages.add(page);
        if (contStream != null) {
            contStream.close();
        }
        contStream = new PDPageContentStream(doc, page);
        curLine = getPageHeight() - topMargin;
    }

    private boolean addToCurLine() throws IOException {
        return addToCurLine(lineHeight);
    }

    private boolean addToCurLine(float height) throws IOException {
        if (curLine - height <= topMargin) {
            try {
                contStream.endText();
            } catch (Exception exc) {}
            addPage();

            return true;
        } else {
            curLine -= height;
        }

        return false;
    }
}

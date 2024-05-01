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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


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

    private PDDocument doc;
    private List<PDPage> pages;
    private PDPageContentStream contStream;
    private float curLine;

    public EvalsPDFBox(ResourceBundle resource, String rootPath, Appraisal appraisal, List<Rating> ratings) throws Exception {
        this.resource = resource;
        this.rootPath = rootPath;
        this.appraisal = appraisal;
        this.ratings = ratings;
        this.permRule = appraisal.getPermissionRule();

        doc = new PDDocument();
        try {
            pages = new ArrayList<PDPage>();
            addPage();

            addHeader();

            addInfoTable();

            addGoalVersions();

            if (contStream != null) {
                contStream.close();
            }
            File file = new File("/opt/evals/pdf/" + "testFile.pdf");
            file.createNewFile();
            doc.save(file);
        } finally {
            doc.close();

            System.out.println("PDF done");
        }
    }

    private void addHeader() throws IOException {
        writeImage(rootPath + IMAGE_OSU_LOGO, sideMargin, curLine, OSU_LOGO_WIDTH, OSU_LOGO_HEIGHT);
        addToCurLine(OSU_LOGO_HEIGHT);

        String officeHr = resource.getString("office-hr");
        float headerTextWidth = getTextWidth(officeHr, font, fontSize);
        float headerTextx = getPageWidth() / 2f - (headerTextWidth / 2);
        float headerTexty = curLine;
        writeText(font, fontSize, headerTextx, headerTexty, officeHr);

        String jobType = appraisal.getJob().getAppointmentType();
        String appraisalTitle = resource.getString("appraisal-title");
        float textWidth = getTextWidth(appraisalTitle, fontBold, fontSizeHeaderBold);
        headerTextx = getPageWidth() - sideMargin - textWidth;
        writeText(fontBold, fontSizeHeaderBold, headerTextx, headerTexty, appraisalTitle);
        float perfEvalHeight = getTextHeight(fontBold, fontSizeHeaderBold);
        writeText(fontBold, fontSizeHeaderBold, headerTextx, headerTexty + perfEvalHeight, jobType);

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
        // CHANGE THIS FOR REAL TESTING 
        // boolean displayRating = StringUtils.containsAny(permRule.getEvaluation(), "ev");
        boolean displayRating = true;
        if (appraisal.getRating() != null && displayRating) {
            for (Rating rating : ratings) {
                if (appraisal.getRating().equals(rating.getRate())) {
                    ratingText = rating.getName();
                }
            }
        }
        String[][][] content = new String[][][]{
            { { "employee", empName }, { "ts-org-code-desc", job.getOrgCodeDescription() } },
            { { "jobTitle", job.getJobTitle() }, { "supervisor", supName } },
            { { "appraisal-employee-id", emp.getOsuid() }, { "position-class", job.getPositionClass() }, { "position-no", job.getPositionNumber() }, { "appraisal-type-pdf", resource.getString(appraisalTypeKey) } },
            { { "reviewPeriod", appraisal.getReviewPeriod() }, { "", "" }, { "appraisal-status", resource.getString(appraisal.getViewStatus()) }, { "appraisal-rating", ratingText } }
        };
        writeTable(curLine, content);
    }

    private void addGoalVersions() throws IOException {
        int displayedGoals = 0;

        // CHANGE THIS FOR REAL TESTING 
        // boolean displayGoals = StringUtils.containsAny(permRule.getApprovedGoals(), "ev");
        boolean displayGoals = true;
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
        // CHANGE THIS FOR REAL TESTING 
        // boolean displayEmployeeResults = StringUtils.containsAny(permRule.getResults(), "ev");
        // boolean displaySupervisorResults = StringUtils.containsAny(permRule.getSupervisorResults(), "ev");
        boolean displayEmployeeResults = true;
        boolean displaySupervisorResults = true;

        for(Assessment assessment : sortedAssessments) {
            displayedGoals ++;

            // add employee goal
            String goalLabel = resource.getString("appraisal-goals") + displayedGoals;
            writeText(fontBoldItalic, fontSize, sideMargin, curLine, goalLabel, true);
            addToCurLine(lineHeight);
            writeText(font, fontSize, sideMargin + tabSize, curLine, assessment.getGoal());
            addToCurLine(lineHeight * 2);

            addAssessmentCriteria(assessment);

            // add employee result
            String empResultText = resource.getString("appraisal-employee-results");
            writeText(fontBoldItalic, fontSize, sideMargin + tabSize, curLine, empResultText);
            addToCurLine(lineHeight);
            writeText(font, fontSize, sideMargin + tabSize, curLine, assessment.getEmployeeResult());
            addToCurLine(lineHeight * 2);

            // add supervisor result
            String supResultText = resource.getString("appraisal-result-comments");
            writeText(fontBoldItalic, fontSize, sideMargin + tabSize, curLine, supResultText);
            addToCurLine(lineHeight);
            writeText(font, fontSize, sideMargin + tabSize, curLine, assessment.getSupervisorResult());
            addToCurLine(lineHeight * 2);
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
            String criteriaText = assessmentCriteria.getCriteriaArea().getName().toUpperCase();
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

    private void writeImage(String path, float x, float y, float width, float height) throws IOException {
        PDImageXObject pdImage = PDImageXObject.createFromFile(path, doc);
        contStream.drawImage(pdImage, x, y - height, width, height);
    }

    private void writeTable(float y, String[][][] content) throws IOException {
        float tableWidth = getPageWidth() - sideMargin * 2;
        contStream.setLineWidth(.5f);

        float nexty = y;
        float textx = sideMargin + cellMargin;
        float texty = y - (baseRowHeight * (float).75);
        float rowHeight = baseRowHeight;
        for (int i = 0; i <= content.length; i++) {
            // draw row line
            contStream.drawLine(sideMargin, nexty, sideMargin + tableWidth, nexty);

            if (i < content.length) {
                float nextx = sideMargin;
                float baseColWidth = tableWidth / (float)content[i].length;
                rowHeight = findRowHeight(baseRowHeight, baseColWidth, content[i]);
                for (int j = 0; j <= content[i].length; j++) {
                    if (j == content[i].length || !content[i][j][0].isEmpty()) {
                        // draw column line
                        contStream.drawLine(nextx, nexty, nextx, nexty - rowHeight);

                        if (j < content[i].length) {
                            String text = resource.getString(content[i][j][0]) + ": ";
                            writeText(font, fontSize, textx, texty, text);
                            float textWidth = getTextWidth(text, font, fontSize);

                            // write bold text
                            text = content[i][j][1];
                            float boldx = textx;
                            float boldy = texty;

                            // determine if we need to line break text
                            float boldWidth = getTextWidth(text, fontBold, fontSizeBold);
                            float colWidth = baseColWidth;
                            if (j + 1 < content[i].length && !content[i][j][0].isEmpty()) {
                                colWidth = baseColWidth * 2;
                            }
                            if (textWidth + boldWidth > colWidth) {
                                float textHeight = getTextHeight(font, fontSize);
                                boldy -= textHeight;
                            } else {
                                boldx += textWidth;
                            }
                            writeText(fontBold, fontSizeBold, boldx, boldy, text);
                        }
                    }
                    textx += baseColWidth;
                    nextx += baseColWidth;
                }
                texty -= rowHeight;
                textx = sideMargin + cellMargin;
            }
            nexty -= rowHeight;
        }

        addToCurLine((curLine - nexty) - lineHeight);
    }

    private void writeText(PDFont font, float fontSize, float x, float y, String text) throws IOException {
        writeText(font, fontSize, x, y, text, false);
    }

    private void writeText(PDFont font, float fontSize, float x, float y, String text, boolean underline) throws IOException {
        contStream.setFont(font, fontSize);
        contStream.beginText();
        contStream.moveTextPositionByAmount(x, y);
        contStream.drawString(text);
        contStream.endText();

        if(underline) {
            float textWidth = getTextWidth(text, font, fontSize);
            float lineHeight = y - 2f;

            contStream.setLineWidth(1f);
            contStream.drawLine(x, lineHeight, textWidth + x, lineHeight);
        }
    }

    private float findRowHeight(float baseRowHeight, float baseColWidth, String[][] content) throws IOException {
        for (int i = 0; i < content.length; i++) {
            float textWidth = getTextWidth(resource.getString(content[i][0]) + ": ", font, fontSize);
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

    private void addToCurLine(float height) throws IOException {
        if (curLine - height <= topMargin) {
            addPage();
        } else {
            curLine -= height;
        }
    }
}

package edu.osu.cws.evals.util;

import edu.osu.cws.evals.models.*;
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
    private static float fontSize = 10.0f;
    private static float fontSizeBold = 11.0f;
    private static float fontSizeHeaderBold = 14.0f;

    private static float sideMargin = 50f;
    private static float topMargin = 20f;
    private static float lineHeight = 13f;

    private static final String IMAGE_OSU_LOGO = "images/pdf-osu-logo.png";
    private static final float OSU_LOGO_HEIGHT = 35.875f;
    private static final float OSU_LOGO_WIDTH = 116.625f;

    public static void createPDF(ResourceBundle resource, String rootPath) throws IOException {
        PDDocument doc = new PDDocument();
        try {
            PDPage page = new PDPage();
            page.setMediaBox(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream contStream = new PDPageContentStream(doc, page);

            writeImage(contStream, doc, page, rootPath + IMAGE_OSU_LOGO, topMargin);

            String officeHr = resource.getString("office-hr");
            float headerTextWidth = getTextWidth(officeHr, font, fontSize);
            float headerTextx = page.getMediaBox().getWidth() / 2f - (headerTextWidth / 2);
            float headerTexty = page.getMediaBox().getHeight() - topMargin - OSU_LOGO_HEIGHT;
            writeText(contStream, font, fontSize, headerTextx, headerTexty, officeHr);

            String jobType = "Professional Faculty";
            String appraisalTitle = resource.getString("appraisal-title");
            float textWidth = getTextWidth(appraisalTitle, fontBold, fontSizeHeaderBold);
            headerTextx = page.getMediaBox().getWidth() - sideMargin - textWidth;
            writeText(contStream, fontBold, fontSizeHeaderBold, headerTextx, headerTexty, appraisalTitle);
            float perfEvalHeight = getTextHeight(fontBold, fontSizeHeaderBold);
            writeText(contStream, fontBold, fontSizeHeaderBold, headerTextx, headerTexty + perfEvalHeight, jobType);
            contStream.close();

            float curLineHeight = topMargin + OSU_LOGO_HEIGHT + lineHeight;

            String[][][] content = new String[][][]{
                { { "Employee:", "evals, test" }, { "Org Code Description:", "test orgn description" } },
                { { "Job Title:", "Evals testing" }, { "Supervisor:", "ruef, alex" } },
                { { "ID:", "932776660" }, { "Position Class:", "A" }, { "Position No:", "E1" }, { "Type:", "Annual" } },
                { { "Review Period:", "11/15/19 - 11/14/20" }, { "", "" }, { "Status:", "Signature Due" }, { "Rating:", "Strong Performance" } }
            };
            writeTable(doc, page, curLineHeight, content);

            File file = new File("/opt/evals/pdf/" + "testFile.pdf");
            file.createNewFile();
            doc.save(file);
        } finally {
            doc.close();

            System.out.println("PDF done");
        }
    }

    private static void writeImage(PDPageContentStream contStream, PDDocument doc, PDPage page, String path, float y) throws IOException {
        PDImageXObject pdImage = PDImageXObject.createFromFile(path, doc);
        float initialHeight = page.getMediaBox().getHeight() - y;

        contStream.drawImage(pdImage, sideMargin, initialHeight - OSU_LOGO_HEIGHT, OSU_LOGO_WIDTH, OSU_LOGO_HEIGHT);
    }

    private static void writeTable(PDDocument doc, PDPage page, float y, String[][][] content) throws IOException {
        float baseRowHeight = 17f;
        float tableWidth = page.getMediaBox().getWidth() - sideMargin * 2;
        float cellMargin = 5f;
        PDPageContentStream contStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true);
        contStream.setLineWidth(.5f);
        float initialHeight = page.getMediaBox().getHeight() - y;

        float nexty = initialHeight;
        float textx = sideMargin + cellMargin;
        float texty = initialHeight - (baseRowHeight * (float).75);
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
                            String text = content[i][j][0] + " ";
                            writeText(contStream, font, fontSize, textx, texty, text);
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
                            writeText(contStream, fontBold, fontSizeBold, boldx, boldy, text);
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

        contStream.close();
    }

    private static void writeText(PDPageContentStream contStream, PDFont font, float fontSize, float x, float y, String text) throws IOException {
        contStream.setFont(font, fontSize);
        contStream.beginText();
        contStream.moveTextPositionByAmount(x, y);
        contStream.drawString(text);
        contStream.endText();
    }

    private static float findRowHeight(float baseRowHeight, float baseColWidth, String[][] content) throws IOException {
        for (int i = 0; i < content.length; i++) {
            float textWidth = getTextWidth(content[i][0] + " ", font, fontSize);
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
}

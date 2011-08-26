package edu.osu.cws.pass.models;

import java.util.ArrayList;
import java.util.Date;

public class NolijCopy extends Pass {

    private int id;
    private int appraisalId;
    private Date submitDate;
    private String filename;

    private static final String validAppraisalIdRequired = "Invalid appraisal Id";
    private static final String validFilenameRequired = "A non-empty filename is required";
    private static final String filenameTooLong = "The filename is longer than the allowed length";

    /**
     * Validates to make sure that the appraisalId is present and non-zero.
     * @return
     */
    public boolean validateAppraisalId() {
        ArrayList<String> appraisalErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("appraisalId");
        if (this.appraisalId == 0) {
            appraisalErrors.add(validAppraisalIdRequired);
        }

        if (appraisalErrors.size() > 0) {
            this.errors.put("appraisalId", appraisalErrors);
            return false;
        }
        return true;
    }

    /**
     * Validates to make sure that the filename is valid and not too long.
     *
     * @return
     */
    public boolean validateFilename() {
        ArrayList<String> filenameErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("filename");
        if (this.filename == null || this.filename.equals("")) {
            filenameErrors.add(validFilenameRequired);
        }

        if (this.filename.length() > 255) {
            filenameErrors.add(filenameTooLong);
        }

        if (filenameErrors.size() > 0) {
            this.errors.put("filename", filenameErrors);
            return false;
        }
        return true;
    }

    public NolijCopy() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppraisalId() {
        return appraisalId;
    }

    public void setAppraisalId(int appraisalId) {
        this.appraisalId = appraisalId;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

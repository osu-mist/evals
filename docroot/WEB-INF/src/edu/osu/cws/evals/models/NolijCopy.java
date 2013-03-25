package edu.osu.cws.evals.models;

import edu.osu.cws.evals.portlet.ActionHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

public class NolijCopy extends Evals {

    private int id;
    private int appraisalId;
    private Date submitDate;
    private String filename;
    private static ActionHelper actionHelper;

    /**
     * Validates to make sure that the appraisalId is present and non-zero.
     * @return
     */
    public boolean validateAppraisalId() {
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        ArrayList<String> appraisalErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("appraisalId");
        if (this.appraisalId == 0) {
            appraisalErrors.add(resource.getString("NolijCopy-validAppraisalIdRequired"));
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
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        ArrayList<String> filenameErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them.
        this.errors.remove("filename");
        if (this.filename == null || this.filename.equals("")) {
            filenameErrors.add(resource.getString("NolijCopy-validFilenameRequired"));
        }

        if (this.filename.length() > 255) {
            filenameErrors.add(resource.getString("NolijCopy-filenameTooLong"));
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

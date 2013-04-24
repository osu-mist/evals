package edu.osu.cws.evals.models;

import java.util.*;

public class GoalVersion {
    private int id;

    private Appraisal appraisal;

    private int approverPidm;

    private Date createDate;

    private Date approvedDate;

    private Set<Assessment> assessments = new HashSet<Assessment>();

    public GoalVersion() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Appraisal getAppraisal() {
        return appraisal;
    }

    public void setAppraisal(Appraisal appraisal) {
        this.appraisal = appraisal;
    }

    public int getApproverPidm() {
        return approverPidm;
    }

    public void setApproverPidm(int approverPidm) {
        this.approverPidm = approverPidm;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Set<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(Set<Assessment> assessments) {
        this.assessments = assessments;
    }

    /**
     * Returns a sorted list of assessments. The assessment pojo class
     * implements comparable interface which makes this easy. It removes deleted assessments from
     * the list.
     *
     * @return
     */
    public List<Assessment> getSortedAssessments() {
        List<Assessment> sortedAssessments = new ArrayList(assessments);
        Collections.sort(sortedAssessments);

        // Remove deleted assessments.
        // Using an iterator since otherwise we would get ConcurrentModificationException
        Iterator<Assessment> iterator = sortedAssessments.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isDeleted()) {
                iterator.remove();
            }
        }

        return sortedAssessments;
    }

    /**
     * Returns the sequence of the last assessment in the collection.
     *
     * @return
     */
    public Integer getLastSequence() {
        Integer lastSequence = 0;
        for (Assessment assessment : assessments) {
            if (assessment.getSequence() > lastSequence) {
                lastSequence = assessment.getSequence();
            }
        }
        return lastSequence;
    }
}

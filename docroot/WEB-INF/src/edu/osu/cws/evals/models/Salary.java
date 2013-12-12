package edu.osu.cws.evals.models;

public class Salary {
    private Integer id;

    private Double low;

    private Double midPoint;

    private Double high;

    private Double current;

    private Double increase;

    private String sgrpCode;

    private Integer appraisalId;

    private Double twoIncrease;

    private Double oneMax;

    private Double oneMin;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getMidPoint() {
        return midPoint;
    }

    public void setMidPoint(Double midPoint) {
        this.midPoint = midPoint;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getCurrent() {
        return current;
    }

    public void setCurrent(Double current) {
        this.current = current;
    }

    public Double getIncrease() {
        return increase;
    }

    public void setIncrease(Double increase) {
        this.increase = increase;
    }

    public String getSgrpCode() {
        return sgrpCode;
    }

    public void setSgrpCode(String sgrpCode) {
        this.sgrpCode = sgrpCode;
    }

    public Integer getAppraisalId() {
        return appraisalId;
    }

    public void setAppraisalId(Integer appraisalId) {
        this.appraisalId = appraisalId;
    }

    public Double getTwoIncrease() {
        return twoIncrease;
    }

    public void setTwoIncrease(Double twoIncrease) {
        this.twoIncrease = twoIncrease;
    }

    public Double getOneMax() {
        return oneMax;
    }

    public void setOneMax(Double oneMax) {
        this.oneMax = oneMax;
    }

    public Double getOneMin() {
        return oneMin;
    }

    public void setOneMin(Double oneMin) {
        this.oneMin = oneMin;
    }
}

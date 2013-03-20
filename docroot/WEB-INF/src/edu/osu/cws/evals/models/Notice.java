package edu.osu.cws.evals.models;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 10/9/12
 * Time: 9:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class Notice {
    private int id;

    private int ancestorID;

    private String name;

    private String text;

    private Employee creator;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Employee getCreator() {
        return creator;
    }

    public void setCreator(Employee creator) {
        this.creator = creator;
    }

    public int getAncestorID() {
        return ancestorID;
    }

    public void setAncestorID(int ancestorID) {
        this.ancestorID = ancestorID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private Date createDate;


}

package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Notice;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.HibernateUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 11/1/12
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoticeMgr {

    public static Notice getYellowBoxMsg() {
        Session session = HibernateUtil.getCurrentSession();
        Notice notice = (Notice)session.createQuery("from edu.osu.cws.evals.models.Notice as notice01 " +
                "where notice01.id = (select max (id) from edu.osu.cws.evals.models.Notice as notice02 " +
                "where notice02.ancestorID = 1)").uniqueResult();
        return notice;
    }
}

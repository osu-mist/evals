package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Notice;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.HibernateUtil;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 11/1/12
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoticeMgr {

    public static Notice getYellowBoxMsg() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Notice notice = (Notice)session.createQuery("from edu.osu.cws.evals.models.Notice as notice01 " +
                "where notice01.id = (select max (id) from edu.osu.cws.evals.models.Notice as notice02 " +
                "where notice02.ancestorID = 1)").uniqueResult();
        return notice;
    }


    public static ArrayList<Notice> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<Notice> noticeList = (ArrayList<Notice> )session.createQuery("from edu.osu.cws.evals.models.Notice as notice01 " +
                "where notice01.id = (select max (id) from edu.osu.cws.evals.models.Notice as notice02 " +
                "where notice02.ancestorID = notice02.ancestorID) order by ancestorID").list();
        return noticeList;
    }

    public static boolean edit(Employee employee, int ancestorId, String text, String name) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Notice notice = new Notice();
        notice.setAncestorID(ancestorId);
        notice.setName(name);
        notice.setText(text);
        Calendar calendar = Calendar.getInstance();
        notice.setCreateDate(calendar.getTime());
        notice.setCreator(employee);
        session.save(notice);
        return true;
    }

   public static boolean add(Employee employee, String text, String name) throws Exception {
       Session session = HibernateUtil.getCurrentSession();
       Notice notice = (Notice)session.createQuery("from edu.osu.cws.evals.models.Notice as notice01 " +
               "where notice01.ancestorID = (select max (ancestorID) " +
               "from edu.osu.cws.evals.models.Notice as notice02)").list().get(0);
       int ancestorId = notice.getAncestorID();
       ancestorId++;
       edit(employee, ancestorId, text, name);
       return true;
   }
}

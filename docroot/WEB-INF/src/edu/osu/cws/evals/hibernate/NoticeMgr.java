package edu.osu.cws.evals.hibernate;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import edu.osu.cws.evals.models.Notice;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.HibernateUtil;

import java.util.*;

import org.hibernate.Query;
import org.hibernate.Session;

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

    public static Notice get(int ancestorID) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Notice notice = (Notice)session.createQuery("from edu.osu.cws.evals.models.Notice as notice01 " +
                "where notice01.id = (select max (id) from edu.osu.cws.evals.models.Notice as notice02 " +
                "where notice02.ancestorID = :ancestorID)").setInteger("ancestorID", ancestorID).uniqueResult();
        return notice;
    }


    public static ArrayList<Notice> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<Notice> noticeList = (ArrayList<Notice> )session.createQuery("from edu.osu.cws.evals.models.Notice as notice01 " +
                "where notice01.id = (select max (id) from edu.osu.cws.evals.models.Notice as notice02 " +
                "where notice01.ancestorID = notice02.ancestorID) order by ancestorID").list();
        return noticeList;
    }

    public static boolean edit(Notice upDatedNotice) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String text = upDatedNotice.getText();
        String name = upDatedNotice.getName();
        boolean nameChanged = false;
        boolean textChanged = false;
        Notice notice = get(upDatedNotice.getAncestorID());
        int textHash = notice.getText().hashCode();
        int updateTextHash = text.hashCode();
        if (!notice.getName().equals(name)) {
            nameChanged = true;
        }
        if (textHash != updateTextHash) {
            textChanged = true;
        }
        if (!nameChanged && !textChanged) {
            return true;
        }
        session.save(upDatedNotice);
        return true;
    }
}

package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Notice;
import edu.osu.cws.evals.util.HibernateUtil;
import java.util.*;
import org.hibernate.Session;


public class NoticeMgr {

    /**
     * fetch the latest notice from notice table and addToRequestMap as yellowBox message
     * @return Notice object of yellowBox message
     * @throws Exception
     */
    public static Notice getHomePageNotice() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (Notice)session.getNamedQuery("notice.homePageNotice").uniqueResult();
    }

    /**
     * fetch a notice from notice table in input ancestorID
     * @return Notice object
     * @throws Exception
     */
    public static Notice get(int ancestorID) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (Notice)session.getNamedQuery("notice.singleNotice").setInteger("ancestorID", ancestorID).uniqueResult();
    }

    public static ArrayList<Notice> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<Notice> noticeList = (ArrayList<Notice>)session.getNamedQuery("notice.noticeList").list();
        return noticeList;
    }

    /**
     * edit Notice data score in the table, validate Notice transported from edit page to see if it is the same as the
     * latest score in database, if not. save it as the new score
     * @param upDatedNotice transported from edit page
     * @return boolean object to tell if the database changed
     * @throws Exception
     */
    public static boolean edit(Notice upDatedNotice) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String text = upDatedNotice.getText();
        Notice notice = get(upDatedNotice.getAncestorID());
        int textHash = notice.getText().hashCode();
        int updateTextHash = text.hashCode();
        if (textHash == updateTextHash) {
            return false;
        }
        session.save(upDatedNotice);
        return true;
    }
}

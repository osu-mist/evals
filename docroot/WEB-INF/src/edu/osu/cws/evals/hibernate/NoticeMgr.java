package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Notice;
import edu.osu.cws.evals.util.HibernateUtil;
import java.util.*;
import org.hibernate.Session;


public class NoticeMgr {

    /**
     * fetch a notice from notice table in input ancestorID
     * @return Notice object
     * @throws Exception
     */
    public static Notice get(int ancestorID) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (Notice)session.getNamedQuery("notice.singleNoticeByAncestorID").setInteger("ancestorID", ancestorID).uniqueResult();
    }

    /**
     * build a map from notice list
     * @return Map of Notices
     * @throws Exception
     */
    public static Map getNotices() throws Exception {
        ArrayList<Notice> notices = list();
        Map noticeMap = new HashMap();
        for (int i = 0; i < notices.size(); i++) {
            noticeMap.put(notices.get(i).getName(), notices.get(i));
        }
        return noticeMap;
    }

    /**
     * get a list notices from notice table
     * @return Arraylist of Notice
     * @throws Exception
     */
    public static ArrayList<Notice> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<Notice> noticeList = (ArrayList<Notice>)session.getNamedQuery("notice.noticeList").list();
        return noticeList;
    }

    /**
     * get Notice data score in the table, validate Notice transported from edit page to see if it is the same as the
     * latest score in database. if not, save it as the new score
     * @param upDatedNotice transported from edit page
     * @return boolean object to tell if the database changed
     * @throws Exception
     */
    public static boolean compareNotices(Notice upDatedNotice) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String textToUpdate = upDatedNotice.getText();
        Notice notice = get(upDatedNotice.getAncestorID());
        int textHash;
        if(notice.getText() == null) {
            textHash = 0;
        } else {
            textHash = notice.getText().hashCode();
        int updateTextHash = textToUpdate.hashCode();
        if (textHash == updateTextHash) {
            return false;
        }
        session.save(upDatedNotice);
        return true;
    }
}

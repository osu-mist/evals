package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Notice;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NoticeMgr {

    /**
     * fetch a notice from notice table in input ancestorID
     * @return Notice object
     * @throws Exception
     */
    public static Notice get(int ancestorID) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (Notice)session.getNamedQuery("notice.singleNoticeByAncestorID")
                .setInteger("ancestorID", ancestorID)
                .uniqueResult();
    }

    /**
     * build a map from notice list
     * @return Map of Notices
     * @throws Exception
     */
    public static Map getNotices() throws Exception {
        Map noticeMap = new HashMap();
        for (Notice notice : list()) {
            noticeMap.put(notice.getName(), notice);
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
        return (ArrayList<Notice>)session.getNamedQuery("notice.noticeList")
                .list();
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
        int textHash = 0;
        if(notice.getText() != null) {
            textHash = notice.getText().hashCode();
        }
        int updateTextHash = textToUpdate.hashCode();
        if (textHash == updateTextHash) {
            return false;
        }
        session.save(upDatedNotice);
        return true;
    }
}

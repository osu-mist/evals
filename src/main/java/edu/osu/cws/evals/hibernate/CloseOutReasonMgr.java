package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.CloseOutReason;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.HibernateUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;

public class CloseOutReasonMgr {
    /**
     * Retrieves a list of closed out reasons from the database sorted alphabetically
     * by reason.
     *
     * @param showDeleted       Whether or not deleted reasons should be listed
     * @return  List<CloseOutReason>
     * @throws Exception
     */
    public static ArrayList<CloseOutReason> list(boolean showDeleted) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Criteria criteria = session.createCriteria(CloseOutReason.class);
        criteria.addOrder(Order.asc("reason"));
        if (!showDeleted) {
            criteria.add(Restrictions.isNull("deleteDate"));
        }

        return (ArrayList<CloseOutReason>) criteria.list();
    }

    /**
     * Sets the deleteDate for a given close out reason. This is a soft delete.
     * Checks that one row was updated.
     *
     * @param id    Close out reason id
     * @return  success
     * @throws Exception
     */
    public static boolean delete(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        int deletedRows = session.getNamedQuery("reason.delete")
                .setInteger("id", id)
                .setDate("deleteDate", new Date())
                .executeUpdate();

        return deletedRows == 1;
    }

    public static boolean add(String reason, Employee loggedInUser) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        reason = StringUtils.trim(reason);
        CloseOutReason existingReason = get(reason);
        if (existingReason != null) {
            existingReason.setDeleteDate(null);
            session.save(existingReason);
            return true;
        }

        CloseOutReason closeOutReason = new CloseOutReason();
        closeOutReason.setReason(reason);
        closeOutReason.setCreator(loggedInUser);
        closeOutReason.setCreateDate(new Date());
        session.save(closeOutReason);

        return closeOutReason.getId() != 0;
    }

    public static CloseOutReason get(String reasonText) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        CloseOutReason reason = (CloseOutReason) session.getNamedQuery("reason.getReason")
                .setString("reason", reasonText)
                .uniqueResult();

        return reason;
    }

    public static CloseOutReason get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (CloseOutReason) session.get(CloseOutReason.class, id);
    }
}

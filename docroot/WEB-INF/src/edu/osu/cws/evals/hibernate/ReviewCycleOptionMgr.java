package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ReviewCycleOption;
import edu.osu.cws.evals.util.HibernateUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;

public class ReviewCycleOptionMgr {

    /**
     * Retrieves a list of review cycle options from the database sorted by sequence.
     *
     * @return  List<ReviewCycleOption>
     * @throws Exception
     */
    public static ArrayList<ReviewCycleOption> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Criteria criteria = session.createCriteria(ReviewCycleOption.class);
        criteria.addOrder(Order.asc("sequence"));
        criteria.addOrder(Order.asc("name"));
        criteria.add(Restrictions.isNull("deleteDate"));

        return (ArrayList<ReviewCycleOption>) criteria.list();
    }

    /**
     * Sets the deleteDate for a given review cycle option. This is a soft delete.
     * Checks that one row was updated.
     *
     * @param id    review cycle option id to delete
     * @return  success
     * @throws Exception
     */
    public static boolean delete(int id, Employee loggedOnUser) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        int deletedRows = session.getNamedQuery("reviewcycleoption.delete")
                .setInteger("id", id)
                .setDate("deleteDate", new Date())
                .setParameter("deleter", loggedOnUser)
                .executeUpdate();

        return deletedRows == 1;
    }

    /**
     * Handles add and edit operations for a review cycle option. If the option being added was previously deleted,
     * it un-deletes the option and updates the value and sequence.
     *
     * @param name
     * @param value
     * @param sequence
     * @param loggedInUser
     * @param id
     * @return
     * @throws Exception
     */
    public static boolean add(String name, Integer value, Integer sequence, Employee loggedInUser, Integer id)
            throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        name = StringUtils.trim(name);
        ReviewCycleOption reviewCycleOption;

        if (id != null && id != 0) {
            // edit - retrieve review cycle to update based on form fields
            reviewCycleOption = get(id);
        } else {
            reviewCycleOption = get(name);

            // add - check if option was previously deleted and set to null delete flags
            if (reviewCycleOption != null) {
                // clear out old delete flag values
                reviewCycleOption.setDeleteDate(null);
                reviewCycleOption.setDeleter(null);
            } else {
                // add - option was not previously deleted in db. create new object
                reviewCycleOption = new ReviewCycleOption();
            }
        }

        reviewCycleOption.setName(name);
        reviewCycleOption.setValue(value);
        reviewCycleOption.setSequence(sequence);
        reviewCycleOption.setCreator(loggedInUser);
        reviewCycleOption.setCreateDate(new Date());
        session.save(reviewCycleOption);

        return reviewCycleOption.getId() != 0;
    }

    /**
     * Fetch review cycle option that matches name. It doesn't filter out deleted options.
     *
     * @param name
     * @return
     * @throws Exception
     */
    public static ReviewCycleOption get(String name) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ReviewCycleOption option = (ReviewCycleOption) session.getNamedQuery("reviewcycleoption.getOption")
                .setString("name", name)
                .uniqueResult();

        return option;
    }

    /**
     * Fetch review cycle option that matches the id. It doesn't filter out deleted options.
     *
     * @param id
     * @return
     * @throws Exception
     */
    public static ReviewCycleOption get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (ReviewCycleOption) session.get(ReviewCycleOption.class, id);
    }

}

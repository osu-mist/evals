package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ReviewCycleOption;
import edu.osu.cws.evals.util.HibernateUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

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
    public static boolean delete(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        int deletedRows = session.getNamedQuery("reviewcycleoption.delete")
                .setInteger("id", id)
                .setDate("deleteDate", new Date())
                .executeUpdate();

        return deletedRows == 1;
    }

    public static boolean add(String name, Integer value, Integer sequence, Employee loggedInUser) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        name = StringUtils.trim(name);
        ReviewCycleOption existingCycleOption = get(name);
        if (existingCycleOption != null) {
            existingCycleOption.setDeleteDate(null);
            session.save(existingCycleOption);
            return true;
        }

        ReviewCycleOption reviewCycleOption = new ReviewCycleOption();
        reviewCycleOption.setName(name);
        reviewCycleOption.setValue(value);
        reviewCycleOption.setSequence(sequence);
        reviewCycleOption.setCreator(loggedInUser);
        reviewCycleOption.setCreateDate(new Date());
        session.save(reviewCycleOption);

        return reviewCycleOption.getId() != 0;
    }

    public static ReviewCycleOption get(String name) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ReviewCycleOption option = (ReviewCycleOption) session.getNamedQuery("reviewcycleoption.getOption")
                .setString("name", name)
                .uniqueResult();

        return option;
    }

    public static ReviewCycleOption get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (ReviewCycleOption) session.get(ReviewCycleOption.class, id);
    }

}

package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.hibernate.AppraisalMgr;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

/**
 * This class is used to create appraisals in the test oracle db. This is not a test class. It is
 * placed in the tests package to make it easy to remove once we deploy things to production.
 */
public class AppraisalCreation {

    public static void main(String[] args) throws Exception {
        HibernateUtil.setConfig("hibernate-oracle.cfg.xml");
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Configuration goalsDueConfig = (Configuration) session.load(Configuration.class, 1);
        String whereClause = "WHERE status != 'T' and appointmentType ='Classified' " +
                "AND positionNumber in " +
//                "('C30132', 'C30134', 'C30135', 'C30136', 'C30137', 'C30138', 'C30141', 'C30143', 'C30144', 'C30146')";
                "('C13716', 'C14380', 'C30013', 'C30022', 'C12403', 'C14075', 'C30132', 'C30134', 'C30135', 'C30136', 'C16100', 'C30137', 'C10099', 'C10471', 'C30138', 'C30141', 'C18024', 'C30143', 'C10210', 'C30144', 'C25003', 'C30146', 'C30008', 'C30479')";

        String query = "from edu.osu.cws.pass.models.Job " + whereClause;
        List<Job> results = (List<Job>) session.createQuery(query).list();
        tx.commit();

        for (Job job : results) {
            Calendar startDay = job.getNewAnnualStartDate();
            Calendar newDay = Calendar.getInstance();
            //this create date is far in the future, let's create the one started last year.
            newDay.add(Calendar.DAY_OF_MONTH, 30);
            if (startDay.after(newDay)) {
                startDay.add(Calendar.YEAR, -1); //last year
            }
            Date startDate = startDay.getTime();
            AppraisalMgr.createAppraisal(job, startDate, Appraisal.TYPE_ANNUAL, goalsDueConfig);
        }
    }
}
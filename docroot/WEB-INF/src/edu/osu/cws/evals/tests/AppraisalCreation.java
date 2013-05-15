package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;

import java.util.*;

/**
 * This class is used to create appraisals in the test oracle db. This is not a test class. It is
 * placed in the tests package to make it easy to remove once we deploy things to production.
 */
public class AppraisalCreation {

    public static void main(String[] args) throws Exception {
        //@ todo add proper parameters
//        HibernateUtil.setHibernateConfig("hibernate.c3p0.ecs.dev.cfg.xml", "", "");

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Configuration goalsDueConfig = (Configuration) session.load(Configuration.class, 1);
        String whereClause = "WHERE status != 'T' and appointmentType ='Classified' " +
                "AND positionNumber in " +
//                "('C30132', 'C30134', 'C30135', 'C30136', 'C30137', 'C30138', 'C30141', 'C30143', 'C30144', 'C30146')";
                "('C13716', 'C14380', 'C30013', 'C30022', 'C12403', 'C14075', 'C30132', 'C30134', 'C30135', 'C30136', 'C16100', 'C30137', 'C10099', 'C10471', 'C30138', 'C30141', 'C18024', 'C30143', 'C10210', 'C30144', 'C25003', 'C30146', 'C30008', 'C30479')";

        String query = "from edu.osu.cws.evals.models.Job " + whereClause;
        List<Job> results = (List<Job>) session.createQuery(query).list();
        tx.commit();

        for (Job job : results) {
            DateTime startDate = job.getNewAnnualStartDate();
            //this create date is far in the future, let's create the one started last year.
            DateTime newDay = new DateTime().plusDays(30);
            if (startDate.isAfter(newDay)) {
                startDate = startDate.minusYears(1); //last year
            }

            // comment out to create test appraisals
//            AppraisalMgr.createAppraisal(job, startDate, Appraisal.TYPE_ANNUAL);
        }
    }
}
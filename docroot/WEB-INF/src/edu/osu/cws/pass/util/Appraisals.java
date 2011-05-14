package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.Appraisal;
import edu.osu.cws.pass.models.Job;
import edu.osu.cws.pass.models.ModelException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;

public class Appraisals {

    /**
     * This method creates an appraisal for the given job by calling the Hibernate
     * class. It returns the id of the created appraisal.
     *
     * @param job
     * @return appraisal.id
     * @throws ModelException
     */
    public int createAppraisal(Job job) throws ModelException {
        Appraisal appraisal = new Appraisal();
        appraisal.setJob(job);
        appraisal.setStatus("goals-due");
        //@todo: how do we define the start and end date ?
        appraisal.setStartDate(new Date());
        appraisal.setEndDate(new Date());
        appraisal.setCreateDate(new Date());

        if (appraisal.validate()) {
            Session session = HibernateUtil.getCurrentSession();
            Transaction tx = session.beginTransaction();
            session.save(appraisal);
            tx.commit();
        }

        return appraisal.getId();
    }
}

package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.OptOut;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class OptOutMgr {

    /**
     * Returns list of OptOuts for a given pidm
     *
     * @param pidm pidm to search for
     * @return List of optouts
     * @throws Exception
     */
    public static List<OptOut> list(int pidm) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        List<OptOut> optOuts = session.createQuery("from edu.osu.cws.evals.models.OptOut optOut where optOut.employee.id = :pidm")
            .setParameter("pidm", pidm)
            .list();

        // call employee to initialize for jsp
        for (OptOut optOut : optOuts) {
            if (optOut.getEmployee() != null) {
                optOut.getEmployee().getName();
            }
        }

        return optOuts;
    }

    /**
     * Updates opt outs for an employee based on Map values
     *
     * @param pidm employee to update opt outs for
     * @param optOutValues Map of enable/disable values for each opt out type 
     * @throws Exception
     */
    public static void updateOptOuts(String pidm, Map<String, Boolean> optOutValues, Employee creator) throws Exception {
        Employee employee = EmployeeMgr.findById(Integer.parseInt(pidm), null);
        List<OptOut> optOuts = list(employee.getId());
        // iterate through values checking for an changes that need to be made
        for(String type : optOutValues.keySet()) {
            OptOut optOut = getByType(optOuts, type);
            // if optout exists we updated
            // if it does not exist check if we need to create one
            if (optOut != null) {
                if (optOut.isActive() != optOutValues.get(type)) {
                    if (optOutValues.get(type)) {
                        undeleteOptOut(optOut);
                    } else {
                        deleteOptOut(optOut, creator);
                    }
                }
            // if an optout does not exist and the desired value is false we dont need to create one
            } else if (optOutValues.get(type)) {
                createOptOut(employee, type, creator);
            }
        }
    }

    /**
     * undeletes optout by removing deleter and date
     *
     * @param optOut optout to update
     * @return updated optOut
     */
    public static OptOut undeleteOptOut(OptOut optOut) {
        optOut.setDeleter(null);
        optOut.setDeleteDate(null);

        Session session = HibernateUtil.getCurrentSession();
        session.save(optOut);

        return optOut;
    }

    /**
     * deletes optout by setting deleter and date
     *
     * @param optOut optout to update
     * @param deleter employee who deleted this optOut
     * @return updated optOut
     */
    public static OptOut deleteOptOut(OptOut optOut, Employee deleter) {
        optOut.setDeleter(deleter);
        optOut.setDeleteDate(new Date());

        Session session = HibernateUtil.getCurrentSession();
        session.save(optOut);

        return optOut;
    }

    /**
     * Creates opt out
     *
     * @param employee employee to create opt out for
     * @param type type of opt out
     * @param creator employee who created this opt out
     * @return created optOut
     */
    public static OptOut createOptOut(Employee employee, String type, Employee creator) {
        OptOut optOut = new OptOut(employee, type, creator);

        Session session = HibernateUtil.getCurrentSession();
        session.save(optOut);

        return optOut;
    }

    /**
     * Creates opt out
     *
     * @param optOuts list of optOuts to search
     * @param type type of opt out to find
     * @return OptOut from list with given type
     */
    public static OptOut getByType(List<OptOut> optOuts, String type) {
        return optOuts.stream().filter(optOut -> type.equals(optOut.getType())).findFirst().orElse(null);
    }
}

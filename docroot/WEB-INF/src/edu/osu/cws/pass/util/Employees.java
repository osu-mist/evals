package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class Employees {

    /**
     * Returns the employee with a matching onid username who is active in the employees table.
     * If no active employee is found a new Employee object is returned.
     *
     * @param username      Onid username
     * @return  Employee
     * @throws Exception
     */
    public Employee findByOnid(String username) throws Exception {
        Session hsession = HibernateUtil.getCurrentSession();
        Employee employee = new Employee();
        try {
            Transaction tx = hsession.beginTransaction();
            List employees = hsession.createQuery(
                    "from edu.osu.cws.pass.models.Employee where onid = ? and status = 'A'")
                    .setString(0, username)
                    .list();
            for (Object user : employees) {
                employee = (Employee) user;
                break;
            }

            tx.commit();
        } catch (Exception e){
            hsession.close();
            throw e;
        }
        return employee;

    }
}

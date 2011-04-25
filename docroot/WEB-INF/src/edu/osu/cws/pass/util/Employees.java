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
     */
    public Employee findByOnid(String username) {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        List employees = hsession.createQuery(
                "from edu.osu.cws.pass.models.Employee where onid = ? and active = 1")
                .setString(0, username)
                .list();
        for (Object employee : employees) {
            tx.commit();
            return (Employee) employee;
        }

        tx.commit();
        return new Employee();

    }
}

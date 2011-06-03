package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class Employees {

    /**
     * Returns the employee with a matching onid username who is active in the employees table.
     * If no active employee is found a new Employee object is returned.
     *
     * @param username      Onid username
     * @return  Employee
     */
    public Employee findByOnid(String username) throws Exception {
        Session hsession = HibernateUtil.getCurrentSession();
        Employee employee = new Employee();
        try {
            Transaction tx = hsession.beginTransaction();
            List employees = hsession.createQuery(
                    "from edu.osu.cws.pass.models.Employee where onid = ? and active = 1")
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

    /**
     * Returns a list of employees that are active. This is used by the demo and can be
     * removed after the demo is done.
     *
     * @return
     */
    public ArrayList<Employee> list() throws Exception {
        ArrayList<Employee> employees = new ArrayList<Employee>();
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            employees = (ArrayList<Employee>) session
                    .createQuery("from edu.osu.cws.pass.models.Employee where active = 1").list();
        } catch (Exception e){
            session.close();
            throw e;
        }

        return employees;
    }

    /**
     * Returns employee object that matches the given pidm.
     *
     * @param pidm
     * @return
     */
    public Employee findEmployee(int pidm) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Employee employee = new Employee();
        try {
            Transaction tx = session.beginTransaction();
            employee = (Employee) session.get(Employee.class, pidm);
            tx.commit();
        } catch (Exception e){
            session.close();
        }

        return employee;
    }

    /**
     * Returns a list of employees that are active. This is used by the demo and can be
     * removed after the demo is done.
     *
     * @return
     */
    public ArrayList<Employee> list() {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ArrayList<Employee> employees = (ArrayList<Employee>) session
                .createQuery("from edu.osu.cws.pass.models.Employee where active = 1").list();

        return employees;
    }

    /**
     * Returns employee object that matches the given pidm.
     *
     * @param pidm
     * @return
     */
    public Employee findEmployee(int pidm) {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Employee employee = (Employee) session.get(Employee.class, pidm);
        tx.commit();

        return employee;
    }
}

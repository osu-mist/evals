package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.ConfigurationMgr;
import edu.osu.cws.evals.hibernate.RatingMgr;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.Rating;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
public class RatingsTest {

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    public void shouldMapRatingsByAppointment() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Map<String, List<Rating>> ratings = RatingMgr.mapByAppointmentType();
        tx.commit();

        assert ratings.size() == 2;
        assert ratings.containsKey("Classified");
        assert ratings.containsKey("ClassifiedIT");

        List<Rating> classifiedRatings = (List<Rating>) ratings.get("Classified");
        assert classifiedRatings.size() == 2;
        assert classifiedRatings.get(0).getDescription().equals("You're awesome");
        assert classifiedRatings.get(1).getDescription().equals("You're great");

    }

}

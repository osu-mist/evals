package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Rating;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.*;

public class RatingMgr {

    /**
     * Returns a hashmap of Configurations using the configuration name as the map key.
     *
     * @return
     * @throws Exception
     */
    public static Map<String, List<Rating>> mapByAppointmentType() throws Exception {
        HashMap<String, List<Rating>> ratingsMap = new HashMap<String, List<Rating>>();

        for (Rating rating : RatingMgr.list()) {
            // get the rating list for the appointment type
            String appointmentType = rating.getAppointmentType().replace(" ", "");
            List<Rating> ratingList = ratingsMap.get(appointmentType);
            if (ratingList == null) {
                ratingList = new ArrayList<Rating>();
                ratingsMap.put(appointmentType, ratingList);
            }

            ratingList.add(rating);
        }

        // sort the ratings for each appointment type
        for (Map.Entry<String, List<Rating>> entry : ratingsMap.entrySet()) {
            List<Rating> ratingList = entry.getValue();
            Collections.sort(ratingList);
        }
        return ratingsMap;
    }

    /**
     * Grabs a list of configurations.
     *
     * @throws Exception
     * @return
     */
    public static List<Rating> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return  (List<Rating>) session.getNamedQuery("rating.list").list();
    }

    public static List<Rating> getRatings(Map<String, List<Rating>> ratingsMap, String appointmentType) {
        return ratingsMap.get(appointmentType.replace(" ", ""));
    }

}

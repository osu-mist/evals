package edu.osu.cws.evals.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author edwin
 */
public class RandomDateGenerator extends Thread {

    private Date from;
    private Date to;


    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public RandomDateGenerator() {
    }



    public Date getRandomDateBetween() {
        Calendar cal = Calendar.getInstance();

        cal.setTime(from);
        BigDecimal decFrom = new BigDecimal(cal.getTimeInMillis());

        cal.setTime(to);
        BigDecimal decTo = new BigDecimal(cal.getTimeInMillis());

        BigDecimal selisih = decTo.subtract(decFrom);
        BigDecimal factor = selisih.multiply(new BigDecimal(Math.random()));

        return new Date((factor.add(decFrom)).longValue());
    }


}

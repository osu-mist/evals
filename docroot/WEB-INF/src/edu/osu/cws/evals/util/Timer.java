package edu.osu.cws.evals.util;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 6/27/12
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */
//new

public class Timer {

    long start;

    long end;

    public void start() {

        start = System.currentTimeMillis();

    }

    public void stop() {

        end = System.currentTimeMillis();

    }

    public double getTime() {

        long time = end - start;

        double t = (double) time;

        return t;

    }

    /*public static void main(String[] args) throws InterruptedException {

        Timer tt = new Timer();

        tt.start();

        Thread.sleep(1000);

        tt.stop();

        System.out.println("hello, use time:" + tt.getTime());

    }       */

}
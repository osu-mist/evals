package edu.osu.cws.evals.util;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 7/20/12
 * Time: 9:53 AM
 * To change this template use File | Settings | File Templates.
 */
import java.util.Random;

public class RandomNumberString
{

    private static final char[] symbols = new char[10];

    static {
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
    }

    private final Random random = new Random();

    private final char[] buf;

    public RandomNumberString(int length)
    {
        if (length < 1)
            throw new IllegalArgumentException("length < 1: " + length);
        buf = new char[length];
    }

    public String nextString()
    {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

}
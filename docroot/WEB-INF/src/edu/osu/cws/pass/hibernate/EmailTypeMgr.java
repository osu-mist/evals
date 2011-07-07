package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.EmailType;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 7/5/11
 * Time: 9:17 AM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Map;
import java.util.HashMap;

public class EmailTypeMgr {
    public static Map<String, EmailType>  getMap()
    {
        Map<String, EmailType> typeMap  = new HashMap();

        return  typeMap;
    }
}

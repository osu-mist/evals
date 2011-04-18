/**
 * Parent class for the Pass Models.
 *
 * Common functionality of the PASS maodels should be stored in this class.
 */
package edu.osu.cws.pass.models;

import org.apache.commons.lang.WordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;


import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class Pass {

    /**
     * HashMap used to store validation error messages.
     */
    protected HashMap errors = new HashMap();

    public boolean validate() {
        ArrayList<String> errors = new ArrayList<String>();
        String validateMethodName;

        try {
            for (Field field : CriterionArea.class.getDeclaredFields()) {
                validateMethodName = WordUtils.capitalize("validate" + field.getName());
                Method validateMethod = CriterionArea.class.getDeclaredMethod(validateMethodName);
                validateMethod.invoke(this);
            }
        } catch (NoSuchMethodException e) {
            _log.error("failed to call validation methods - NoSuchMethodException");
        } catch (InvocationTargetException e) {
            _log.error("failed to call validation methods - InvocationTargetException");
        } catch (IllegalAccessException e) {
            _log.error("failed to call validation methods - IllegalAccessException");
        }

        return this.errors.size() == 0;
    }


    /**
     * Returns the validation errors of the current POJO object.
     * @return
     */
    public HashMap getErrors() {
        return this.errors;
    }

    private static Log _log = LogFactoryUtil.getLog(CriterionArea.class);
}

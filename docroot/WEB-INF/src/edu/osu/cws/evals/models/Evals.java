/**
 * Parent class for the Evals Models.
 *
 * Common functionality of the PASS maodels should be stored in this class.
 */
package edu.osu.cws.evals.models;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;


import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class Evals {

    /**
     * HashMap used to store validation error messages.
     */
    protected HashMap errors = new HashMap();

    public boolean validate() throws ModelException {
        String validateMethodName;

        for (Field field : this.getClass().getDeclaredFields()) {
            validateMethodName = "validate"+WordUtils.capitalize(field.getName());
            try {
                Method validateMethod = this.getClass().getDeclaredMethod(validateMethodName);
                validateMethod.invoke(this);
            } catch (NoSuchMethodException e) {
//                _log.error("failed to call "+validateMethodName+" validation method - NoSuchMethodException" );
            } catch (InvocationTargetException e) {
//                _log.error("failed to call validation methods - InvocationTargetException");
            } catch (IllegalAccessException e) {
//                _log.error("failed to call validation methods - IllegalAccessException");
            }
        }
        if (this.errors.size() > 0) {
            throw new ModelException(StringUtils.join(getErrorKeys(), "\n"));
        }
        return true;
    }


    /**
     * Returns the validation errors of the current POJO object.
     * @return
     */
    public HashMap getErrors() {
        return this.errors;
    }

    /**
     * Takes HashMap of errors and iterates over the ArrayList of errors for each
     * field name.
     *
     * @return
     */
    public ArrayList<String> getErrorKeys() {
        ArrayList<String> aggregateErrors = new ArrayList<String>();
        ArrayList<String> fieldErrors;
        for (Object errorArray : this.errors.values()) {
            fieldErrors = (ArrayList<String>) errorArray;
            for (String errorKey : fieldErrors) {
                aggregateErrors.add(errorKey);
            }
        }

        return aggregateErrors;
    }

    private static Log _log = LogFactoryUtil.getLog(CriterionArea.class);

    public static ResourceBundle resources = ResourceBundle.getBundle("edu.osu.cws.evals.portlet.Language");
}

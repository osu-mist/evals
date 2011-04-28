package edu.osu.cws.pass.models;

/**
 * This Exception class is used by the pass models to throw exceptions which contain
 * error messages that will need to be displayed in the jsp files.
 */
public class ModelException extends Exception {

    public ModelException(String message) {
        super(message);
    }
}

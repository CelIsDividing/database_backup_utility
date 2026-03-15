package com.databasebackup.exception;

/**
 * Exception thrown when database connection configuration is invalid.
 * This includes missing parameters, invalid values, or unsupported database types.
 */
public class InvalidConnectionConfigException extends Exception {

    private final String parameterName;

    /**
     * Constructor with message and cause
     */
    public InvalidConnectionConfigException(String message, Throwable cause) {
        super(message, cause);
        this.parameterName = null;
    }

    /**
     * Constructor with message only
     */
    public InvalidConnectionConfigException(String message) {
        super(message);
        this.parameterName = null;
    }

    /**
     * Constructor with parameter name and message
     */
    public InvalidConnectionConfigException(String parameterName, String message) {
        super(String.format("Invalid parameter '%s': %s", parameterName, message));
        this.parameterName = parameterName;
    }

    /**
     * Constructor with parameter name, message, and cause
     */
    public InvalidConnectionConfigException(String parameterName, String message, Throwable cause) {
        super(String.format("Invalid parameter '%s': %s", parameterName, message), cause);
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}

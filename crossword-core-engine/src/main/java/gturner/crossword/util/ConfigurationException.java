package gturner.crossword.util;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/25/11, Time: 11:01 PM
 * <br> An exception that occurs when the environment is misconfigured.
 *
 * @author George.Turner
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

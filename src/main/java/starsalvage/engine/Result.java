package starsalvage.engine;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Inspired by Rust's Result system and works
 * as an alternative to Java Exceptions, with the additional
 * ability to return either a failed result and no data,
 * or a successful result with any data, as well as a
 * failed message
 */
public class Result<T> {
    /**
     * Types of errors
     */
    public enum ErrType {
        NONE,
        INVALID_MOVE,
        OUT_OF_BOUNDS,
        INVALID_DIRECTION,
        MOVE_ONTO_BLOCKED_CELL,
        NO_HISTORY,
        NO_DRILL_CHARGES_AVAILABLE,
        NON_DESTRUCTIBLE_CELL,
        NO_CELL_AT_POSITION,
        INVALID_BOARD_SIZE,
        NO_IMAGE,
        GAME_SAVE_FAILED,
        GAME_LOAD_FAILED,
        GAME_NOT_STARTED,
        GAME_IN_PROGRESS,
        NO_SHIP_IN_SECTOR
    }

    /**
     * Value of result - null if Error
     */
    private final T value;
    /**
     * Type of error - null if Ok
     */
    private final ErrType errType;
    /**
     * Error message - "NO ERROR" if Ok
     */
    private final String message;
    /**
     * Whether the result is Ok or Err
     */
    public final boolean isSuccess;
    /**
     * Used for logging to console and is null if Ok
     */
    private Logger logger;

    /**
     * Sets parameters for Ok
     * @param value Type and content of value
     */
    private Result(T value) {
        this.isSuccess = true;
        this.value = value;
        this.errType = ErrType.NONE;
        this.message = "NO ERROR";
    }
    /**
     * Sets parameters for Err
     * @param type Error type
     * @param message Error message
     * @param logger Error logger
     */
    private Result(ErrType type, String message, Logger logger) {
        this.isSuccess = false;
        this.value = null;
        this.errType = type;
        this.message = message;
        this.logger = logger;
    }

    /**
     * Create a successful result with value
     * @param value The stored result value
     * @return A successful result
     * @param <T> The result value type
     */
    public static <T> Result<T> ok(T value) {
        return new Result<T>(value);
    }

    /**
     * Create an error result with no value
     * @param type Error type
     * @param message Error message
     * @param logger Error logger
     * @return Failed result
     * @param <T> Result value type
     */
    public static <T> Result<T> err(ErrType type, String message, Logger logger) {
        return new Result<T>(type, message, logger);
    }

    /**
     * Get the stored value or null for Err
     * Should not be called without checking value of isSuccess is true
     * @return Stored value
     */
    public T getValueOrNull() { return value; }

    /**
     * Gets the error type, or null if no error
     * Should not be called without checking value of isSuccess is false
     * @return Error type
     */
    public ErrType getErrType() { return errType; }
    /**
     * Gets the error message, or "NO ERROR" if no error
     * Should not be called without checking value of isSuccess is false
     * @return Error message
     */
    public String getMessage() { return message; }

    /**
     * Prints the error message to console using the provided
     * logger
     */
    public void printErr() { logger.warning(errType.name() + " : " + message); }
    /**
     * Prints the error message to console using the provided
     * logger and returns getValueOrNull()
     * Should only be used if you really don't care if a value is null
     * or if never expected to actually occur
     */
    public T printErrOrGetValue() {
        if(!isSuccess)
            printErr();
        return getValueOrNull();
    }
}

package edu.univ.erp.util;

public class Result<T> {
    private final boolean success;
    private final String message;
    private final T data;

    // Constructor
    public Result(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Static factories for convenience
    public static <T> Result<T> success(String message) {
        return new Result<>(true, message, null);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
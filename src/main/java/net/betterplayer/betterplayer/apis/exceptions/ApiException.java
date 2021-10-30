package net.betterplayer.betterplayer.apis.exceptions;

public abstract class ApiException extends RuntimeException {
    private final String message;

    public ApiException(String msg) {
        this.message = msg;
    }

    public String getMessage() {
        return this.message;
    }

}

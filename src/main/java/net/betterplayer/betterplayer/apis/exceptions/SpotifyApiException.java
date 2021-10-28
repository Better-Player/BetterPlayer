package net.betterplayer.betterplayer.apis.exceptions;

public class SpotifyApiException extends RuntimeException {
    private final String message;

    public SpotifyApiException(String msg) {
        this.message = msg;
    }

    public String getMessage() {
        return this.message;
    }
}

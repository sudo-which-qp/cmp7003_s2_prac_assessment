package org.godsendjoseph.pet_app.auth;

/**
 * Class to encapsulate authentication results (login, registration, etc.)
 * Includes success status and a message.
 */
public class AuthResult {
    private boolean success;
    private String message;

    public AuthResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "AuthResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}

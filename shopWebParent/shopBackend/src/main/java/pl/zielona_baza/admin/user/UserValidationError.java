package pl.zielona_baza.admin.user;

public enum UserValidationError {
    PASSWORD_REQUIRED_FOR_NEW_USER("Password cannot be empty for a new user."),
    EMAIL_MUST_BE_UNIQUE("Given email already taken."),
    FILE_NOT_IMG("Incorrect type of file.");

    final String message;

    UserValidationError(String message) {
        this.message = message;
    }
}

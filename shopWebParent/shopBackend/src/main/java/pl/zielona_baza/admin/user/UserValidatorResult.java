package pl.zielona_baza.admin.user;

public record UserValidatorResult(String message) {

    private static final String SUCCESS_MESSAGE = "success";

    public static UserValidatorResult success() {
        return new UserValidatorResult(SUCCESS_MESSAGE);
    }

    public static UserValidatorResult failure(String message) {
        return new UserValidatorResult(message);
    }

    public boolean isNotValid() {
        return !SUCCESS_MESSAGE.equals(message);
    }
}

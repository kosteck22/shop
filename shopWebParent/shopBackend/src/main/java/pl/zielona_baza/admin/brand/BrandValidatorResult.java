package pl.zielona_baza.admin.brand;

public record BrandValidatorResult(String message) {
    private static final String SUCCESS_MESSAGE = "success";

    public static BrandValidatorResult success() {
        return new BrandValidatorResult(SUCCESS_MESSAGE);
    }

    public static BrandValidatorResult failure(String message) {
        return new BrandValidatorResult(message);
    }

    public boolean isNotValid() {
        return !SUCCESS_MESSAGE.equals(message);
    }
}

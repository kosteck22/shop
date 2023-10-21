package pl.zielona_baza.admin.category;

import pl.zielona_baza.admin.brand.BrandValidatorResult;

public record CategoryValidatorResult(String message) {
    private static final String SUCCESS_MESSAGE = "success";

    public static CategoryValidatorResult success() {
        return new CategoryValidatorResult(SUCCESS_MESSAGE);
    }

    public static CategoryValidatorResult failure(String message) {
        return new CategoryValidatorResult(message);
    }

    public boolean isNotValid() {
        return !SUCCESS_MESSAGE.equals(message);
    }
}

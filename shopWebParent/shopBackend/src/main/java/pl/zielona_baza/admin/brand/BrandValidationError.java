package pl.zielona_baza.admin.brand;

public enum BrandValidationError {
    NAME_SIZE_INCORRECT("Brand name must be between 2 and 45 letters."),
    NAME_NOT_UNIQUE("Choose another brand name. This one is already taken."),
    FILE_NOT_IMG("Incorrect type of file."),
    EMPTY_IMG_FOR_NEW_BRAND("New brand must have img.");

    final String message;

    BrandValidationError(String message) {
        this.message = message;
    }
}

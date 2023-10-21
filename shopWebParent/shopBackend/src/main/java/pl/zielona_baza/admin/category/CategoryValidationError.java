package pl.zielona_baza.admin.category;

public enum CategoryValidationError {
    NAME_SIZE_INCORRECT("Category name must be between 2 and 128 letters."),
    NAME_NOT_UNIQUE("Choose another category name. This one is already taken."),
    ALIAS_SIZE_INCORRECT("Category alias must be between 2 and 64 letters."),
    ALIAS_NOT_UNIQUE("Choose another category alias. This one is already taken."),
    FILE_NOT_IMG("Incorrect type of file."),
    EMPTY_IMG_FOR_NEW_BRAND("New category must have img.");


    final String message;

    CategoryValidationError(String message) {
        this.message = message;
    }
}

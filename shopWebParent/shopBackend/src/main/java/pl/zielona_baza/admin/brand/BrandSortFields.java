package pl.zielona_baza.admin.brand;

public class BrandSortFields {

    private static final String SORT_BY_ID = "id";
    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_DEFAULT = "name";

    public static String getValidatedSortField(String name) {
        if (validateFieldName(name)) return name;

        return SORT_BY_DEFAULT;
    }

    private static boolean validateFieldName(String name) {
        return name.equals(SORT_BY_ID) || name.equals(SORT_BY_NAME);
    }
}

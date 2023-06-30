package pl.zielona_baza.admin.paging;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PagingAndSortingValidator {

    public static Integer validatePage(Integer page) {
        if (page == null || page < 1) return 1;

        return page;
    }

    public static Integer validateLimit(Integer limit, Integer defaultLimit) {
        if (limit == null) return defaultLimit;
        if (limit < 1) return 1;
        if (limit > 100) return 100;

        return limit;
    }

    public static String validateSortField(String sortField, List<String> availableSortFields, String defaultSortField) {
        if (sortField != null && availableSortFields.contains(sortField)) return sortField;

        if (availableSortFields.contains(defaultSortField)) return defaultSortField;

        if (availableSortFields.size() > 0) return availableSortFields.get(0);

        return "id";
    }

    public static String validateSortDir(String sortDir) {
        return (sortDir != null && sortDir.equals("desc")) ? "desc" : "asc";
    }
}

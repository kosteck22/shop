package pl.zielona_baza.admin.paging;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class PagingAndSortingArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(PagingAndSortingParam.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer model,
                                  NativeWebRequest request,
                                  WebDataBinderFactory binderFactory) throws Exception {
        String sortDir = getSortDirection(request);
        String sortField = request.getParameter("sortField");
        String keyword = request.getParameter("keyword");
        Integer page = getPageNumber(request);

        String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", reverseSortDir);
        model.addAttribute("keyword", keyword);

        PagingAndSortingParam annotation = parameter.getParameterAnnotation(PagingAndSortingParam.class);

        return new PagingAndSortingHelper(model, annotation.listName(), sortField, sortDir, keyword, page);
    }

    private String getSortDirection(NativeWebRequest request) {
        String sortDir = request.getParameter("sortDir");
        if (sortDir != null && sortDir.equalsIgnoreCase("desc")) return "desc";

        return "asc";
    }

    private Integer getPageNumber(NativeWebRequest request) {
        String page = request.getParameter("page");
        if (page == null) return 1;
        try {
            return Integer.parseInt(page);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}

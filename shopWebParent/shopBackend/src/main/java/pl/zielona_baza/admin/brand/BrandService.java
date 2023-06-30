package pl.zielona_baza.admin.brand;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.paging.PagingAndSortingValidator;
import pl.zielona_baza.common.entity.Brand;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.*;

@Service
public class BrandService {

    private static final List<String> availableSortFields = new ArrayList<>(List.of("id", "name"));
    private static final int BRANDS_PER_PAGE = 10;

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public void listByPage(Integer pageNumber, String sortField, String sortDir, Integer limit, String keyword, ModelAndViewContainer model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, BRANDS_PER_PAGE);
        sortField = validateSortField(sortField, availableSortFields, "name");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper(model, "listBrands", sortField, sortDir, keyword, limit);

        helper.listEntities(pageNumber, brandRepository);
    }

    public Brand save(Brand brand) {
        return brandRepository.save(brand);
    }

    public Brand get(Integer id) throws BrandNotFoundException {
        try {
            return brandRepository.findById(id).get();
        } catch (NoSuchElementException ex) {
            throw new BrandNotFoundException("Brand with ID " + id + " not found.");
        }
    }

    public void delete(Integer id) throws BrandNotFoundException {
        Brand brand = get(id);
        brandRepository.delete(brand);
    }

    public String checkUnique(Integer id, String name) {
        boolean isCreatingNew = (id == null || id == 0);
        Brand brandByName = brandRepository.findByName(name);

        if(isCreatingNew) {
            if (brandByName != null) return "Duplicate";
        } else {
            if (id != brandByName.getId() && brandByName != null) {
                return "Duplicate";
            }
        }

        return "OK";
    }

    public List<Brand> listAll() {
        return brandRepository.findAllProjection();
    }
}

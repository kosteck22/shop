package pl.zielona_baza.admin.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.FileUploadUtil;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.entity.product.ProductImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductSaveHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSaveHelper.class);

    static void deleteExtraImagesWereRemovedOnForm(Product product) {
        String extraImageDir = "product-images/" + product.getId() + "/extras";
        List<String> listObjectKeys = AmazonS3Util.listFolder(extraImageDir);

        for (String objectKey : listObjectKeys) {
            int lastIndexOfSlash = objectKey.lastIndexOf("/");
            String fileName = objectKey.substring(lastIndexOfSlash + 1);
            if (!product.containsImageName(fileName)) {
                AmazonS3Util.deleteFile(objectKey);
            }
        }
    }

    static void setExistingExtraImageNames(String[] imageIds, String[] imageNames, Product product) {
        if (imageIds == null || imageIds.length == 0) return;

        Set<ProductImage> images = new HashSet<>();
        Arrays.stream(imageIds).forEach(image -> {

        });
        for (int i = 0; i < imageIds.length; i++) {
            Integer id = Integer.parseInt(imageIds[i]);
            String name = imageNames[i];
            if (!name.isEmpty()) images.add(ProductImage.builder()
                    .id(id)
                    .name(name)
                    .product(product).build());
        }
        product.setImages(images);
    }

    static void setProductDetails(String[] detailIds,String[] detailNames, String[] detailValues, Product product) {
        if (detailNames == null || detailNames.length == 0) return;

        for (int i = 0; i < detailNames.length; i++) {
            String name = detailNames[i];
            String value = detailValues[i];
            Integer id = Integer.parseInt(detailIds[i]);

            if (id != 0) {
                product.addDetail(id, name, value);
            } else if (!name.isEmpty() && !value.isEmpty()) {
                product.addDetail(name, value);
            }
        }
    }

    static void saveUploadedImage(MultipartFile mainImageMultipart, MultipartFile[] extraImageMultiparts, Product savedProduct) throws IOException {
        if (!mainImageMultipart.isEmpty()) {
            String fileName = StringUtils.cleanPath(mainImageMultipart.getOriginalFilename());
            String uploadDir = "product-images/" + savedProduct.getId();

            List<String> listObjectKeys = AmazonS3Util.listFolder(uploadDir + "/");
            for (String objectKey : listObjectKeys) {
                if (!objectKey.contains("/extras/")) {
                    AmazonS3Util.deleteFile(objectKey);
                }
            }

            AmazonS3Util.uploadFile(uploadDir, fileName, mainImageMultipart.getInputStream());

            //FileUploadUtil.cleanDir(uploadDir);
            //FileUploadUtil.saveFile(uploadDir, fileName, mainImageMultipart);
        }

        if (extraImageMultiparts.length > 0) {
            String uploadDir = "product-images/" + savedProduct.getId() + "/extras";

            for (MultipartFile multipartFile : extraImageMultiparts) {
                if(multipartFile.isEmpty()) continue;

                String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

                AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());
                //FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
            }
        }
    }

    static void setNewExtraImageNames(MultipartFile[] extraImageMultiparts, Product product) {
        if (extraImageMultiparts.length > 0) {
            for (MultipartFile multipartFile : extraImageMultiparts) {
                if(!multipartFile.isEmpty()) {
                    String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
                    if (!product.containsImageName(fileName)) {
                        product.addExtraImage(fileName);
                    }
                }
            }
        }
    }

    static void setMainImageName(MultipartFile mainImageMultipart, Product product) {
        if (!mainImageMultipart.isEmpty()) {
            String fileName = StringUtils.cleanPath(mainImageMultipart.getOriginalFilename());
            product.setMainImage(fileName);
        }
    }
}

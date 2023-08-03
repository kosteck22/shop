package pl.zielona_baza.admin.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.FileUploadUtil;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.entity.product.ProductDetail;
import pl.zielona_baza.common.entity.product.ProductImage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    static void setMainImageName(MultipartFile mainImageMultipart, Product product) {
        if (!mainImageMultipart.isEmpty()) {
            String fileName = product.getShortName() + UUID.randomUUID();
            product.setMainImage(fileName);
        }
    }

    static void setExistingExtraImageNames(String[] imageIds, String[] imageNames, Product product) {
        if (imageIds == null || imageIds.length == 0) return;
        if (imageIds.length != imageNames.length) throw new IndexOutOfBoundsException("Size of the imageIds array and imageNames array must be the same");

        Iterator<ProductImage> iterator = product.getImages().iterator();

        while(iterator.hasNext()) {
            ProductImage productImage = iterator.next();
            boolean stillExistImage = false;

            for(int i = 0; i < imageIds.length; i++) {
                if (stillExistImage) break;

                Integer id = Integer.parseInt(imageIds[i]);
                String name = StringUtils.cleanPath(imageNames[i]);

                if (!name.isEmpty()) {
                    if (productImage.getId().equals(id) && productImage.getName().equals(name)) {
                        stillExistImage = true;
                    }
                }
            }

            if (!stillExistImage) {
                iterator.remove();
            }
        }
    }

    static List<String> setNewExtraImageNames(MultipartFile[] extraImageMultiparts, Product product) {
        List<String> imgNames = new ArrayList<>();

        if (extraImageMultiparts.length > 0) {
            for (MultipartFile multipartFile : extraImageMultiparts) {
                if(!multipartFile.isEmpty() && isImage(multipartFile)) {
                    String fileName = UUID.randomUUID().toString();
                    product.addExtraImage(fileName);
                    imgNames.add(fileName);
                }
            }
        }
        return imgNames;
    }

    static void setProductDetails(String[] detailIds, String[] detailNames, String[] detailValues, Product product) {
        if (detailNames == null || detailNames.length == 0) return;
        if (detailNames.length != detailValues.length || detailNames.length != detailIds.length) throw new IndexOutOfBoundsException("Size of the imageIds array and imageNames array must be the same");

        for (int i = 0; i < detailNames.length; i++) {
            String name = detailNames[i];
            String value = detailValues[i];
            int id = Integer.parseInt(detailIds[i]);

            if (id != 0 && !name.isEmpty() && !value.isEmpty()) {
                product.addDetail(id, name, value);

            } else if (!name.isEmpty() && !value.isEmpty()) {
                product.addDetail( name, value);
            }
        }
    }

    static void updateProductDetails(String[] detailIds, String[] detailNames, String[] detailValues, Product product) {
        if (detailNames == null || detailNames.length == 0) return;
        if (detailNames.length != detailValues.length || detailNames.length != detailIds.length) throw new IndexOutOfBoundsException("Size of the imageIds array and imageNames array must be the same");

        Iterator<ProductDetail> iterator = product.getDetails().iterator();

        while (iterator.hasNext()) {
            ProductDetail productDetail = iterator.next();
            boolean stillExistDetail = false;

            for (int i = 0; i < detailNames.length; i++) {
                if (stillExistDetail) break;

                Integer id = Integer.parseInt(detailIds[i]);
                String name = StringUtils.cleanPath(detailNames[i]);
                String value = StringUtils.cleanPath(detailValues[i]);

                if (!name.isEmpty() && !value.isEmpty() && id > 0) {
                    if (productDetail.getId().equals(id)) {
                        productDetail.setName(name);
                        productDetail.setValue(value);
                        stillExistDetail = true;
                    }
                }
            }

            if (!stillExistDetail) {
                iterator.remove();
            }
        }

        for (int i = 0; i < detailNames.length; i++) {
            Integer id = Integer.parseInt(detailIds[i]);
            String name = StringUtils.cleanPath(detailNames[i]);
            String value = StringUtils.cleanPath(detailValues[i]);

            if (id == 0 && !name.isEmpty() && !value.isEmpty()) {
                product.addDetail(name, value);
            }
        }
    }

    static void saveUploadedImage(MultipartFile mainImageMultipart, MultipartFile[] extraImageMultiparts, Product savedProduct, List<String> newExtraImgNames) throws IOException {
        if (!mainImageMultipart.isEmpty()) {
            String fileName = savedProduct.getMainImage();
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
            int j = 0;

            for (MultipartFile extraImageMultipart : extraImageMultiparts) {
                if (extraImageMultipart.isEmpty() || !isImage(extraImageMultipart)) continue;

                String fileName = newExtraImgNames.get(j);
                j++;

                AmazonS3Util.uploadFile(uploadDir, fileName, extraImageMultipart.getInputStream());
                //FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
            }
        }
    }

    public static boolean isImage(MultipartFile file)
    {
        try {
            return ImageIO.read(file.getInputStream()) != null;
        } catch (Exception e) {
            return false;
        }
    }
}

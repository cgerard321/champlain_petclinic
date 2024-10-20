package com.petclinic.products.utils;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductBundle;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.presentationlayer.images.ImageRequestModel;
import com.petclinic.products.presentationlayer.images.ImageResponseModel;
import com.petclinic.products.presentationlayer.products.ProductBundleRequestModel;
import com.petclinic.products.presentationlayer.products.ProductBundleResponseModel;
import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.presentationlayer.ratings.RatingRequestModel;
import com.petclinic.products.presentationlayer.ratings.RatingResponseModel;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class EntityModelUtil {

    public static ProductResponseModel toProductResponseModel(Product product) {
        ProductResponseModel productResponseModel = new ProductResponseModel();
        BeanUtils.copyProperties(product, productResponseModel);
        return productResponseModel;
    }

    public static Product toProductEntity(ProductRequestModel productRequestModel) {
        return Product.builder()
                .productId(generateUUIDString())
                .imageId(productRequestModel.getImageId())
                .productName(productRequestModel.getProductName())
                .productDescription(productRequestModel.getProductDescription())
                .productSalePrice(productRequestModel.getProductSalePrice())
                .productType(productRequestModel.getProductType())
                .productQuantity(productRequestModel.getProductQuantity())
                .isUnlisted(productRequestModel.getIsUnlisted())
                .releaseDate(productRequestModel.getReleaseDate())
                .productStatus(productRequestModel.getProductStatus())
                .deliveryType(productRequestModel.getDeliveryType())
                .build();
    }

    public static RatingResponseModel toRatingResponseModel(Rating rating){
        RatingResponseModel responseModel = new RatingResponseModel();
        BeanUtils.copyProperties(rating, responseModel);
        return responseModel;
    }

    public static Rating toRatingEntity(RatingRequestModel requestModel, String productId, String customerId){
        return Rating.builder()
                .productId(productId)
                .customerId(customerId)
                .rating(requestModel.getRating())
                .review(requestModel.getReview() != null ? requestModel.getReview() : "")
                .build();
    }

    public static ImageResponseModel toImageResponseModel(Image image) {
        ImageResponseModel imageResponseModel = new ImageResponseModel();
        BeanUtils.copyProperties(image, imageResponseModel);
        return imageResponseModel;
    }

    public static Image toImageEntity(ImageRequestModel imageRequestModel){
        return Image.builder()
                .imageId(generateUUIDString())
                .imageName(imageRequestModel.getImageName())
                .imageType(imageRequestModel.getImageType())
                .imageData(imageRequestModel.getImageData())
                .build();
    }

    public static String generateUUIDString() {
        return UUID.randomUUID().toString();
    }

    public static Mono<ImageRequestModel> handleAddImage(String imageName, String imageType, FilePart imageData) {
        return DataBufferUtils.join(imageData.content())
                .map(dataBuffer -> {
                    byte[] imageDataBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(imageDataBytes);
                    DataBufferUtils.release(dataBuffer);

                    return new ImageRequestModel(imageName, imageType, imageDataBytes);
                });
    }

    public static ProductBundleResponseModel toProductBundleResponseModel(ProductBundle bundle) {
        return ProductBundleResponseModel.builder()
                .bundleId(bundle.getBundleId())
                .bundleName(bundle.getBundleName())
                .bundleDescription(bundle.getBundleDescription())
                .productIds(bundle.getProductIds())
                .originalTotalPrice(bundle.getOriginalTotalPrice())
                .bundlePrice(bundle.getBundlePrice())
                .build();
    }
    public static ProductBundle toProductBundleEntity(ProductBundleRequestModel requestModel) {
        return ProductBundle.builder()
                .bundleName(requestModel.getBundleName())
                .bundleDescription(requestModel.getBundleDescription())
                .productIds(requestModel.getProductIds())
                .bundlePrice(requestModel.getBundlePrice())
                .build();
    }
}

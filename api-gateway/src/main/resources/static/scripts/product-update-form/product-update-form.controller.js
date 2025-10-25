'use strict';

angular.module('productUpdateForm')
    .controller('ProductUpdateFormController', [
        "$http", "$state", "$stateParams",
        function ($http, $state, $stateParams) {
            var self = this;
            var productId = $stateParams.productId;

            // dropdown lists
            self.deliveryType   = ["DELIVERY", "PICKUP", "DELIVERY_AND_PICKUP", "NO_DELIVERY_OPTION"];
            self.productStatus  = ["AVAILABLE", "PRE_ORDER", "OUT_OF_STOCK"];
            self.productType    = ["FOOD", "MEDICATION", "ACCESSORY", "EQUIPMENT"];

            // will hold file user selected (optional)
            self.selectedFile = null;

            //console.log("ProductId: " + productId);

            // Load product by ID
            $http.get('/api/gateway/products/' + productId).then(function (resp) {
                self.product = resp.data;

                if (self.product.isUnlisted === undefined || self.product.isUnlisted === null) {
                    self.product.isUnlisted = false;
                }

                // fetch current image (for preview)
                if (self.product.imageId) {
                    $http.get('api/gateway/images/' + self.product.imageId)
                        .then(function (imageResp) {
                            if (imageResp.data === "") {
                                //console.log("no image found");
                                alert("No image found for this product");
                                return;
                            }
                            self.product.imageData = imageResp.data.imageData;
                            self.product.imageType = imageResp.data.imageType;
                        })
                        .catch(function (err) {
                            //console.error("Error fetching image: ", err);
                            alert("Failed to load image data.");
                        });
                }
            });

            // Handle file selection from <input type="file">
            self.onFileSelected = function(files) {
                if (files && files.length > 0) {
                    var file = files[0];
                    var validTypes = ["image/png", "image/jpeg"];

                    if (!validTypes.includes(file.type)) {
                        alert("Please select a PNG or JPEG image file.");
                        document.getElementById("imageFile").value = "";
                        self.selectedFile = null;
                        return;
                    }

                    self.selectedFile = file;
                    //console.log("New file chosen for update:", self.selectedFile);
                }
            };

            // Called when user submits the update form
            self.submitProductUpdateForm = function () {

                // If user chose a new image, we first upload new image
                if (self.selectedFile) {
                    var imgFormData = new FormData();
                    imgFormData.append('imageName', self.selectedFile.name);
                    imgFormData.append('imageType', self.selectedFile.type);
                    imgFormData.append('imageData', self.selectedFile);

                    $http.post('/api/gateway/images', imgFormData, {
                        transformRequest: angular.identity,
                        headers: { 'Content-Type': undefined }
                    }).then(function (uploadResp) {
                        var newImageId = uploadResp.data.imageId;
                        //console.log("Uploaded new image for update. imageId:", newImageId);

                        // now PUT product with the new imageId
                        updateProductOnServer(newImageId);

                    }, function (err) {
                        //console.error("Image upload failed on update:", err);
                        alert("Image upload failed. Please use PNG or JPEG only.");
                    });

                } else {
                    // no new image picked → keep existing imageId
                    updateProductOnServer(self.product.imageId);
                }
            };

            // Helper to send PUT to update product
            function updateProductOnServer(finalImageId) {
                var data = {
                    productName:        self.product.productName,
                    productDescription: self.product.productDescription,
                    productPrice:       self.product.productPrice,
                    productQuantity:    self.product.productQuantity,
                    productSalePrice:   self.product.productSalePrice,
                    productType:        self.product.productType,
                    productStatus:      self.product.productStatus,
                    deliveryType:       self.product.deliveryType,
                    imageId:            finalImageId,
                    isUnlisted:         self.product.isUnlisted
                };

                if (!productId) {
                    //console.error("Product ID is missing");
                    alert("Product ID is missing.");
                    return;
                }

                $http.put('/api/gateway/products/' + productId, data)
                    .then(function (response) {
                        //console.log("Product updated:", response);
                        $state.go('productList');
                    }, function (response) {
                        var error = response.data;
                        error.errors = error.errors || [];
                        alert(
                            error.error + "\r\n" +
                            error.errors.map(function (e) {
                                return e.field + ": " + e.defaultMessage;
                            }).join("\r\n")
                        );
                    });
            }
        }
    ]);

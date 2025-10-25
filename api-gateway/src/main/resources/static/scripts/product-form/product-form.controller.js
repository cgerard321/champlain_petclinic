'use strict';

angular.module('productForm')
    .controller('ProductFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        // Dropdown values
        self.deliveryType   = ["DELIVERY", "PICKUP", "DELIVERY_AND_PICKUP", "NO_DELIVERY_OPTION"];
        self.productStatus  = ["AVAILABLE", "PRE_ORDER", "OUT_OF_STOCK"];
        self.productType    = ["FOOD", "MEDICATION", "ACCESSORY", "EQUIPMENT"];

        // Product model
        self.product = { isUnlisted: false };
        self.selectedFile = null;

        // Called when user picks a file
        self.onFileSelected = function(files) {
            if (files && files.length > 0) {
                var file = files[0];
                var validTypes = ["image/png", "image/jpeg"];

                // Validate file type
                if (!validTypes.includes(file.type)) {
                    alert("Please select a PNG or JPEG image file.");
                    document.getElementById("imageFile").value = "";
                    self.selectedFile = null;
                    return;
                }

                self.selectedFile = file;
                //console.log('Selected file:', self.selectedFile);
            }
        };

        // Called when the form is submitted
        self.submitProductForm = function () {
            // If image is required
            if (!self.selectedFile) {
                alert("Please select a valid PNG or JPEG image before submitting.");
                return;
            }

            // Upload image
            var imgFormData = new FormData();
            imgFormData.append('imageName', self.selectedFile.name);
            imgFormData.append('imageType', self.selectedFile.type);
            imgFormData.append('imageData', self.selectedFile);

            $http.post('/api/gateway/images', imgFormData, {
                transformRequest: angular.identity,
                headers: { 'Content-Type': undefined }
            }).then(function (uploadResp) {
                var newImageId = uploadResp.data.imageId;
                //console.log('Uploaded imageId:', newImageId);
                createProduct(newImageId);

            }, function (err) {
                //console.error('Image upload failed:', err);
                alert('Image upload failed. Please use PNG or JPEG only.');
            });
        };

        // Helper: send JSON to /api/gateway/products
        function createProduct(imageId) {
            var data  = {
                productName:        self.product.productName,
                productDescription: self.product.productDescription,
                productPrice:       self.product.productPrice,
                productQuantity:    self.product.productQuantity,
                productSalePrice:   self.product.productSalePrice,
                productType:        self.product.productType,
                productStatus:      self.product.productStatus,
                deliveryType:       self.product.deliveryType,
                imageId:            imageId,
                isUnlisted:         self.product.isUnlisted
            };

            $http.post('/api/gateway/products', data)
                .then(function (response) {
                    //console.log('Product created:', response.data);
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
    }]);

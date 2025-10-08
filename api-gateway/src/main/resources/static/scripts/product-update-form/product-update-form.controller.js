'use strict';



angular.module('shopProductUpdateForm')
    .controller('ShopProductUpdateFormController', [
        "$http", "$state", "$stateParams",
        function ($http, $state, $stateParams) {
            var self = this;
            var productId = $stateParams.productId;
            //hardcoded because no currently existing way to get
            self.deliveryType = ["DELIVERY", "PICKUP", "DELIVERY_AND_PICKUP", "NO_DELIVERY_OPTION"];
            self.productStatus = ["AVAILABLE", "PRE_ORDER", "OUT_OF_STOCK"];
            self.productType = ["FOOD", "MEDICATION", "ACCESSORY", "EQUIPMENT"];

            console.log("ProductId: " + productId);

            // Load product by ID
            $http.get('/api/gateway/products/' + productId).then(function (resp) {
                self.product = resp.data;
                var prod = self.product;
                //fetch image
                            $http.get('api/v2/gateway/images/' + prod.imageId).then(function (imageResp){
                            if(imageResp.data === ""){
                               console.log("no image found");
                               return;
                            }
                               prod.imageData = imageResp.data.imageData;
                               prod.imageType = imageResp.data.imageType;
                            })
                               .catch(function (err) {
                               console.error("Error fetching image: ", err);
                            });
            });

            // update
            self.submitProductUpdateForm = function () {
                var data = {
                    productName: self.product.productName,
                    productDescription: self.product.productDescription,
                    productPrice: self.product.productPrice,
                    productQuantity: self.product.productQuantity,
                    productSalePrice: self.product.productSalePrice,
                    productType: self.product.productType,
                    productStatus: self.product.productStatus,
                    deliveryType: self.product.deliveryType,
                    imageId: self.product.imageId
                };

                $http.put('/api/gateway/products/' + productId, data)
                    .then(function (response) {
                        console.log(response);
                        $state.go('shopProductList');
                    }, function (response) {
                        var error = response.data;
                        error.errors = error.errors || [];
                        alert(error.error + "\r\n" + error.errors.map(function (e) {
                            return e.field + ": " + e.defaultMessage;
                        }).join("\r\n"));
                    });

                if (!productId) {
                    console.error("Product ID is missing");
                }
            };
        }
    ]);
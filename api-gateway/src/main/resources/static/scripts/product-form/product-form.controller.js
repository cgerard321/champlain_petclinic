'use strict';


angular.module('productForm')
    .controller('ProductFormController', ["$http", '$state', '$stateParams', function ($http, $state , $stateParams) {
        var self = this;
            //hardcoded because no currently existing way to get
            self.deliveryType = ["DELIVERY", "PICKUP", "DELIVERY_AND_PICKUP", "NO_DELIVERY_OPTION"];
            self.productStatus = ["AVAILABLE", "PRE_ORDER", "OUT_OF_STOCK"];
            self.productType = ["FOOD", "MEDICATION", "ACCESSORY", "EQUIPMENT"];
        // post request to create a new product
        self.submitProductForm = function () {
            var data  = {
                productName: self.product.productName,
                productDescription: self.product.productDescription,
                productPrice: self.product.productPrice,
                productQuantity: self.product.productQuantity,
                productSalePrice: self.product.productSalePrice,
                productType: self.product.productType,
                productStatus: self.product.productStatus,
                deliveryType: self.product.deliveryType,
                imageId: self.product.imageId,
                isUnlisted: self.product.isUnlisted
            }
            $http.post('/api/gateway/products', data
            )
                .then(function (response) {
                    //console.log(response);
                    $state.go('productList');

                }, function (response) {
                    var error = response.data;
                    error.errors = error.errors || [];
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });
        }
    }]);
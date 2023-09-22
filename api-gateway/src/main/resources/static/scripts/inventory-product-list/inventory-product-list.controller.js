'use strict';

angular.module('inventoryProductList')
    .controller('InventoryProductController', ['$http', '$scope', function ($http, $scope) {
        var self = this;
        var inventoryId = 1;
        const productsJSON = [{
            "id": "6503974c55c4ec4e8ce0fde1",
            "productId": "191cfaa4-4834-4621-a0b3-40fcd8c97871",
            "inventoryId": "1",
            "productName": "Acepromazine",
            "productDescription": "Sedative for dogs",
            "productPrice": 40.0,
            "productQuantity": 4
        }, {
            "id": "6503974c55c4ec4e8ce0fde2",
            "productId": "191cfaa4-4834-4621-a0b3-40fcd8c97872",
            "inventoryId": "1",
            "productName": "Albuterol",
            "productDescription": "Bronchodilator for dogs and cats",
            "productPrice": 20.0,
            "productQuantity": 5
        }]
        self.inventoryProductList = productsJSON
        // self.inventoryProductList = productsJSON;
        //         $http.get('api/gateway/inventory/' + inventoryId + '/products').then(function (resp) {
        //             self.inventoryProductList = productsJSON;
        //
        //         });
                $scope.deleteBundle = function (bundleUUID) {
                    let varIsConf = confirm('Want to delete Bundle with Bundle Id:' + bundleUUID + '. Are you sure?');
                    if (varIsConf) {

                        $http.delete('api/gateway/bundles/' + bundleUUID)
                            .then(successCallback, errorCallback)

                        function successCallback(response) {
                            $scope.errors = [];
                            alert(bundleUUID + " Deleted Successfully!");
                            console.log(response, 'res');
                            //refresh list
                            $http.get('api/gateway/inventory/' + inventoryId + '/products').then(function (resp) {
                                //self.inventoryProductList = resp.data;
                                self.inventoryProductList = productsJSON;
                                arr = resp.data;
                            });
                        }

                        function errorCallback(error) {
                            alert(data.errors);
                            console.log(error, 'can not get data.');
                        }
                    }
                };
            }]);

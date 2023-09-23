'use strict';

angular.module('inventoryProductList')
    .controller('InventoryProductController', ['$http', '$scope', '$stateParams', function ($http, $scope, $stateParams) {
        var self = this;
        var inventoryId = $stateParams.inventoryId;
        console.log("State params: " + $stateParams)

                $http.get('api/gateway/inventory/' + inventoryId + '/products').then(function (resp) {
                    self.inventoryProductList = resp.data;


                });
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

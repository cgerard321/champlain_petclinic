'use strict';

angular.module('inventoryList')
    .controller('InventoryListController', ['$http', '$scope', function ($http, $scope) {
        var self = this;

                $http.get('api/gateway/inventory').then(function (resp) {
                    self.inventoryList = resp.data;
                    console.log("Resp data: " + resp.data)
                    console.log("inventory list: " + self.inventoryList)
                });
                /*$scope.deleteBundle = function (bundleUUID) {
                    let varIsConf = confirm('Want to delete Bundle with Bundle Id:' + bundleUUID + '. Are you sure?');
                    if (varIsConf) {

                        $http.delete('api/gateway/bundles/' + bundleUUID)
                            .then(successCallback, errorCallback)

                        function successCallback(response) {
                            $scope.errors = [];
                            alert(bundleUUID + " Deleted Successfully!");
                            console.log(response, 'res');
                            //refresh list
                            $http.get('api/gateway/bundles').then(function (resp) {
                                self.bundleList = resp.data;
                                arr = resp.data;
                            });
                        }

                        function errorCallback(error) {
                            alert(data.errors);
                            console.log(error, 'can not get data.');
                        }
                    }
                }*/
            }]);

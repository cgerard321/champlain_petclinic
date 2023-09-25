'use strict';

angular.module('inventoryList')
    .controller('InventoryListController', ['$http', '$scope', function ($http, $scope) {
        var self = this;

                $http.get('api/gateway/inventory').then(function (resp) {
                    self.inventoryList = resp.data;
                    console.log("Resp data: " + resp.data)
                    console.log("inventory list: " + self.inventoryList)
                });


        $scope.deleteAllInventories = function () {
            let varIsConf = confirm('Are you sure you want to clear all entries from the inventory?');
            if (varIsConf) {
                $http.delete('api/gateway/inventory')
                    .then(function(response) {
                        alert("All inventory entries have been cleared!");

                        $http.get('api/gateway/inventory').then(function (resp) {
                            self.inventoryList = [];
                        });

                    }, function(error) {
                        alert(error.data.errors);
                        console.log(error, 'Failed to clear inventory entries.');
                    });
            }
        };

    }]);

'use strict';

angular.module('inventoryList')
    .controller('InventoryListController', ['$http', '$scope', function ($http, $scope) {
        var self = this;

                $http.get('api/gateway/inventory').then(function (resp) {
                    self.inventoryList = resp.data;
                    console.log("Resp data: " + resp.data)
                    console.log("inventory list: " + self.inventoryList)
                });
//search by inventory field
        $scope.searchInventory = function (inventoryName, inventoryType, inventoryDescription){

            var queryString = '';

            if (inventoryName != null && inventoryName !== '') {
                queryString += "inventoryName=" + inventoryName;
            }

            if (inventoryType != null && inventoryType !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "inventoryType=" + inventoryType;
            }

            if (inventoryDescription != null && inventoryDescription !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "inventoryDescription=" + inventoryDescription;
            }

            if (queryString !== '') {
                $http.get("api/gateway/inventory?" + queryString)
                    .then(function(resp) {
                        self.inventoryList = resp.data;
                        arr = resp.data;
                    })
                    .catch(function(error) {
                        if (error.status === 404) {
                            alert('inventory not found.');
                        } else {
                            alert('An error occurred: ' + error.statusText);
                        }
                    });
            } else {
                $http.get("api/gateway/inventory")
                    .then(function(resp) {
                        self.inventoryList = resp.data;
                        arr = resp.data;
                    })
                    .catch(function(error) {
                        if (error.status === 404) {
                            alert('inventory not found.');
                        } else {
                            alert('An error occurred: ' + error.statusText);
                        }
                    });
            }
        };


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

$scope.fetchInventoryList = function() {
    $http.get('api/gateway/inventory').then(function (resp) {
        self.inventoryList = resp.data;
        arr = resp.data;
    });
};
}]);

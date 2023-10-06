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
        $scope.searchInventory = function (inventoryName, inventoryType, inventoryDescription) {
            var queryString = '';

            if (inventoryName) {
                queryString += "inventoryName=" + inventoryName;
            }

            if (inventoryType) {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "inventoryType=" + inventoryType;
            }

            if (inventoryDescription) {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "inventoryDescription=" + inventoryDescription;
            }

            if (queryString !== '') {
                $http.get("api/gateway/inventory?" + queryString)
                    .then(function(resp) {
                        self.inventoryList = resp.data;
                        arr = resp.data; // Ensure 'arr' is declared or handled accordingly
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
                        arr = resp.data; // Ensure 'arr' is declared or handled accordingly
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

        $scope.deleteInventory = function (inventory) {
            let ifConfirmed = confirm('Are you sure you want to remove this inventory?');
            if (ifConfirmed) {

                $http.delete('api/gateway/inventory/' + inventory.inventoryId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert(inventory.inventoryName + " Successfully Removed!");
                    console.log(response, 'res');
                    location.reload();

                }
                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'Data is inaccessible.');
                }
            }
        };


    }]);

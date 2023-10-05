'use strict';

angular.module('inventoryList')
    .controller('InventoryListController', ['$http', '$scope', '$stateParams', '$state', function ($http, $scope, $stateParams, $state) {
        var self = this;
        self.currentPage = $stateParams.page = 0
        self.listSize= $stateParams.size = 10
        self.realPage = parseInt(self.currentPage) + 1

        loadList()
        function loadList(){
            $state.transitionTo('inventoryList', {page: self.currentPage, size: self.listSize}, {notify: false});

            $http.get('api/gateway/inventory/pages?page=' + self.currentPage + "&size=" + self.listSize).then(function (resp) {
                self.inventoryList = resp.data;
            });
        }

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

        $scope.pageBefore = function () {
            if (self.currentPage - 1 >= 0){
                self.currentPage = (parseInt(self.currentPage) - 1).toString();
                self.realPage = parseInt(self.currentPage) + 1
                loadList();
            }
        }

        $scope.pageAfter = function () {
                self.currentPage = (parseInt(self.currentPage) + 1).toString();
                self.realPage = parseInt(self.currentPage) + 1
                loadList();
        }


    }]);

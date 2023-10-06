'use strict';

angular.module('inventoryList')
    .controller('InventoryListController', ['$http', '$scope', '$stateParams', '$state', function ($http, $scope, $stateParams, $state) {
        var self = this;
        self.currentPage = $stateParams.page = 0
        self.listSize = $stateParams.size = 10
        self.realPage = parseInt(self.currentPage) + 1
        var numberOfPage
        var name
        var type
        var desc

        getInventoryList()


        $http.get('api/gateway/inventory').then(function (resp) {
            self.inventoryList = resp.data;
            console.log("Resp data: " + resp.data)
            console.log("inventory list: " + self.inventoryList)
        });

        $scope.searchInventory = function (inventoryName, inventoryType, inventoryDescription){
            getInventoryList(inventoryName, inventoryType, inventoryDescription)
        }
//search by inventory field
        function getInventoryList(inventoryName, inventoryType, inventoryDescription){

            $state.transitionTo('inventoryList', {page: self.currentPage, size: self.listSize}, {notify: false});
            var queryString = '';
            name = ""
            type = ""
            desc = ""

            if (inventoryName != null && inventoryName !== '') {
                name = inventoryName

                queryString += "inventoryName=" + inventoryName;
            }

            if (inventoryType) {
                if (queryString !== '') {
                    queryString += "&";
                }
                type = inventoryType
                queryString += "inventoryType=" + inventoryType;
            }

            if (inventoryDescription) {
                if (queryString !== '') {
                    queryString += "&";
                }
                desc = inventoryDescription
                queryString += "inventoryDescription=" + inventoryDescription;
            }

            if (queryString !== '') {
                self.currentPage = 0
                self.realPage = parseInt(self.currentPage) + 1

                $http.get("api/gateway/inventory?page=" + self.currentPage + "&size=" + self.listSize + "&" + queryString)
                    .then(function(resp) {
                        numberOfPage = Math.ceil(resp.data.length / 10)
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
                $http.get("api/gateway/inventory?page=" + self.currentPage + "&size=" + self.listSize)
                    .then(function(resp) {
                        numberOfPage = Math.ceil(resp.data.length / 10)
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

        $scope.pageBefore = function () {
            if (self.currentPage - 1 >= 0){
                self.currentPage = (parseInt(self.currentPage) - 1).toString();
                self.realPage = parseInt(self.currentPage) + 1

                getInventoryList(name, type, desc)
            }
        }

        $scope.pageAfter = function () {
            if (self.currentPage + 1 <= numberOfPage) {
                self.currentPage = (parseInt(self.currentPage) + 1).toString();
                self.realPage = parseInt(self.currentPage) + 1

                getInventoryList(name, type, desc)
            }
        }
    }]);

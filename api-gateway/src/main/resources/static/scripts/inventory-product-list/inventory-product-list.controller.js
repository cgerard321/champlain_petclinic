'use strict';

angular.module('inventoryProductList')
    .controller('InventoryProductController', ['$http', '$scope', '$stateParams','$window', 'InventoryService', function ($http, $scope, $stateParams, $window, InventoryService) {
        var self = this;
        var inventoryId
        self.currentPage = $stateParams.page || 0;
        self.pageSize = $stateParams.size || 2;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;

        fetchProductList();

        // $http.get('api/gateway/inventory/' + $stateParams.inventoryId + '/products-pagination?page=' + 0 + '&size=2').then(function (resp) {
        //     self.inventoryProductList = resp.data;
        //     inventoryId = $stateParams.inventoryId;
        //     InventoryService.setInventoryId(inventoryId);
        //     if (resp.data.length === 0) {
        //         // Handle if inventory is empty
        //         console.log("The inventory is empty!");
        //     }
        // }).catch(function (error) {
        //     if (error.status === 404) {
        //         console.clear()
        //         console.log("State params: " + $stateParams)
        //         console.log("Inventory is now empty.")
        //     } else {
        //         console.error('An error occurred:', error);
        //     }
        // });


            $scope.deleteProduct = function (product) {
            let varIsConf = confirm('Are you sure you want to remove this product?');
            if (varIsConf) {

                $http.delete('api/gateway/inventory/' + product.inventoryId + '/products/' + product.productId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    alert(product.productName + " Successfully Removed!");
                    console.log(response, 'res');
                    //refresh list

                    $http.get('api/gateway/inventory/' + product.inventoryId + '/products').then(function (resp) {
                        self.inventoryProductList = resp.data;
                        arr = resp.data;
                        inventoryId = $stateParams.inventoryId;

                    }).catch(function (error) {
                        if (error.status === 404) {
                            $window.location.reload();
                        } else {
                            console.error('An error occurred:', error);
                        }
                    });
                }
                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'Data is inaccessible.');
                }
            }
        }

        $scope.searchProduct = function(productName, productQuantity, productPrice, productSalePrice) {
            var inventoryId = $stateParams.inventoryId;
            var queryString = '';

            if (productName) {
                queryString += "productName=" + productName;
            }

            if (productQuantity) {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "productQuantity=" + productQuantity;
            }

            if (productPrice) {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "productPrice=" + productPrice;
            }

            if (productSalePrice) {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "productSalePrice=" + productSalePrice;
            }


            var apiUrl = "api/gateway/inventory/" + inventoryId + "/products";
            if (queryString !== '') {
                apiUrl += "?" + queryString;
            }

            $http.get(apiUrl)
                .then(function(resp) {
                    self.inventoryProductList = resp.data;
                    // Just a heads-up: ensure 'arr' is declared elsewhere or handled accordingly
                    arr = resp.data;
                })
                .catch(function(error) {
                    if (error.status === 404) {
                        alert('Product not found.');
                    } else {
                        alert('An error occurred: ' + error.statusText);
                    }
                });
        };

        $scope.deleteAllProducts = function () {
            let varIsConf = confirm('Are you sure you want to delete all products for this inventory?');
            if (varIsConf) {
                let inventoryId = $stateParams.inventoryId;  // Retrieve the inventoryId from the appropriate location

                $http.delete('api/gateway/inventory/' + inventoryId + '/products')
                    .then(function(response) {
                        alert("All products for this inventory have been deleted!");


                        fetchProductList();
                    }, function(error) {
                        alert(error.data.errors);
                        console.log(error, 'Failed to delete all products.');
                    });
            }
        };

        function fetchProductList() {
            let inventoryId = $stateParams.inventoryId;
            $http.get('api/gateway/inventory/' + $stateParams.inventoryId + '/products-pagination?page='+ self.currentPage + '&size=' + self.pageSize).then(function (resp) {
                self.inventoryProductList = resp.data;
                inventoryId = $stateParams.inventoryId;
                InventoryService.setInventoryId(inventoryId);
                if (resp.data.length === 0) {
                    // Handle if inventory is empty
                    console.log("The inventory is empty!");
                }
            }).catch(function (error) {
                console.error('An error occurred:', error);
            });
        }
        self.nextPage = function () {
            if (parseInt(self.currentPage) + 1 < self.totalPages) {

                var currentPageInt = parseInt(self.currentPage) + 1
                self.currentPage = currentPageInt.toString();
                updateActualCurrentPageShown();
                // Refresh the owner's list with the new page size
                fetchProductList();
            }
        }

        self.previousPage = function () {
            console.log("Previous page called, current self.page: ")
            if (self.currentPage - 1 >= 0) {
                var currentPageInt = parseInt(self.currentPage) - 1
                self.currentPage = currentPageInt.toString();
                updateActualCurrentPageShown();
                console.log("Called the previous page and the new current page " + self.currentPage)
                // Refresh the owner's list with the new page size
                fetchProductList();
            }
        }

        function updateActualCurrentPageShown() {
            self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
            console.log(self.currentPage);
        }
        function loadTotalItem() {
            return $http.get('api/gateway/inventory/' + $stateParams.inventoryId + '/products-count')
                .then(function (resp) {
                    console.log(resp);
                    return resp.data;
                });
        }
        loadTotalItem().then(function (totalItems) {
            self.totalItems = totalItems;
            self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));
        });
    }]);
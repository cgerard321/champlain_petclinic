'use strict';

angular.module('inventoryProductList')
    .controller('InventoryProductController', ['$http', '$scope', '$stateParams','$window', 'InventoryService', function ($http, $scope, $stateParams, $window, InventoryService) {
        var self = this;
        var inventoryId
        self.currentPage = $stateParams.page || 0;
        self.pageSize = $stateParams.size || 2;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        self.baseUrl = "api/gateway/inventory/" + $stateParams.inventoryId + "/products-pagination?page=" + self.currentPage + "&size=" + self.pageSize;
        self.baseURLforTotalNumberOfProductsByFiltering = "api/gateway/inventory/" + $stateParams.inventoryId + "/products-count";
        self.lastParams = {
            productName: '',
            productQuantity: '',
            productPrice: '',
            productSalePrice: ''
        }

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
                    alert(error.errors);
                    console.log(error, 'Data is inaccessible.');
                }
            }
        }

        $scope.searchProduct = function(productName, productQuantity, productPrice, productSalePrice) {
            var inventoryId = $stateParams.inventoryId;
            var queryString = '';

            if (productName && productName !== '') {
                queryString += "productName=" + productName;
                self.lastParams.productName = productName;
            }

            if (productQuantity && productQuantity !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "productQuantity=" + productQuantity;
                self.lastParams.productQuantity = productQuantity;
            }

            if (productPrice && productPrice !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "productPrice=" + productPrice;
                self.lastParams.productPrice = productPrice;
            }

            if (productSalePrice && productSalePrice !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "productSalePrice=" + productSalePrice;
                self.lastParams.productSalePrice = productSalePrice;
            }


            var apiUrl = "api/gateway/inventory/" + inventoryId + "/products";
            if (queryString !== '') {
                apiUrl += "?" + queryString;
            }

            $http.get(apiUrl)
                .then(function(resp) {
                    self.inventoryProductList = resp.data;
                    loadTotalItem(productName, productPrice, productQuantity)
                    InventoryService.setInventoryId(inventoryId);
                })
                .catch(function(error) {
                    if (error.status === 404) {
                        //alert('Product not found.');
                        self.inventoryProductList = [];
                        self.currentPage = 0;
                        updateActualCurrentPageShown();

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

        function fetchProductList(productName, productPrice, productQuantity, productSalePrice) {
            if (productName || productPrice || productQuantity) {
                self.lastParams.productName = productName;
                self.lastParams.productPrice = productPrice;
                self.lastParams.productQuantity = productQuantity;
                self.lastParams.productSalePrice = productSalePrice;
                self.searchProduct(productName, productPrice, productQuantity)
            }
            else {

                self.lastParams.productSalePrice = null;
                self.lastParams.productName = null;
                self.lastParams.productPrice = null;
                self.lastParams.productQuantity = null;
                let inventoryId = $stateParams.inventoryId;
                $http.get('api/gateway/inventory/' + $stateParams.inventoryId + '/products-pagination?page=' + self.currentPage + '&size=' + self.pageSize).then(function (resp) {
                    self.inventoryProductList = resp.data;
                    inventoryId = $stateParams.inventoryId;
                    loadTotalItem(productName, productPrice, productQuantity, productSalePrice)
                    InventoryService.setInventoryId(inventoryId);
                    if (resp.data.length === 0) {
                        // Handle if inventory is empty
                        console.log("The inventory is empty!");
                    }
                }).catch(function (error) {
                    console.error('An error occurred:', error);
                });
            }
        }
        self.nextPage = function () {
            if (parseInt(self.currentPage) + 1 < self.totalPages) {
                var currentPageInt = parseInt(self.currentPage) + 1
                self.currentPage = currentPageInt.toString();
                updateActualCurrentPageShown();
                //refresh product list
                fetchProductList(self.lastParams.productName, self.lastParams.productPrice, self.lastParams.productQuantity, self.lastParams.productSalePrice);
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
                fetchProductList(self.lastParams.productName, self.lastParams.productPrice, self.lastParams.productQuantity, self.lastParams.productSalePrice);
            }
        }

        function updateActualCurrentPageShown() {
            self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
            console.log(self.currentPage);
        }
        function loadTotalItem(productName, productPrice, productQuantity, productSalePrice) {
            var query = ''
            if(productName){
                if (query === ''){
                    query +='?productName='+productName
                } else{
                    query += '&productName='+productName
                }
            }
            if (productPrice){
                if (query === ''){
                    query +='?productPrice='+productPrice
                } else{
                    query += '&productPrice='+productPrice
                }
            }
            if (productQuantity){
                if (query === ''){
                    query +='?productQuantity='+productQuantity
                } else{
                    query += '&productQuantity='+productQuantity
                }
            }
            if (productSalePrice){
                if (query === ''){
                    query +='?productSalePrice='+productSalePrice
                } else{
                    query += '&productSalePrice='+productSalePrice
                }
            }
            $http.get('api/gateway/inventory/' + $stateParams.inventoryId + '/products-count' + query)
                .then(function (resp) {
                    self.totalItems = resp.data;
                    self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));
                });
        }
    }]);
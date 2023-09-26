'use strict';

angular.module('inventoryProductList')
    .controller('InventoryProductController', ['$http', '$scope', '$stateParams','$window', 'InventoryService', function ($http, $scope, $stateParams, $window, InventoryService) {
        var self = this;
        var inventoryId

                $http.get('api/gateway/inventory/' + $stateParams.inventoryId + '/products').then(function (resp) {
                    self.inventoryProductList = resp.data;
                    inventoryId = $stateParams.inventoryId;
                    InventoryService.setInventoryId(inventoryId);
                }).catch(function (error) {
                    if (error.status === 404) {
                        console.clear()
                        console.log("State params: " + $stateParams)
                        console.log("Inventory is now empty.")
                    } else {
                        console.error('An error occurred:', error);
                    }
                });


                $scope.deleteProduct = function (product) {
                    let varIsConf = confirm('Are you sure you want to remove this product?');
                    if (varIsConf) {

                        $http.delete('api/gateway/inventory/' + product.inventoryId + '/products/' + product.productId)
                            .then(successCallback, errorCallback)

                        function successCallback(response) {
                            $scope.errors = [];
                            alert(product.productName + " Successfully Removed!");
                            console.log(response, 'res');
                            //refresh list

                            $http.get('api/gateway/inventory/' + product.inventoryId + '/products').then(function (resp) {
                                self.inventoryProductList = resp.data;
                                arr = resp.data;

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
                };
                $scope.searchProduct = function (productName, productQuantity, productPrice){
                    if (productName != null && productQuantity != null && productPrice != null){
                        $http.get('api/gateway/inventory/' + inventoryId + "/products?productPrice=" + productPrice + "&productQuantity=" + productQuantity)
                            .then(function () {
                                alert("Cannot search by all fields");
                            });
                    }
                    if (productName != null && productQuantity != null){
                        $http.get("api/gateway/inventory/" + inventoryId + "/products?productName=" + productName + "&productQuantity=" + productQuantity)
                            .then(function (resp) {
                                self.inventoryProductList = resp.data;
                                arr = resp.data;
                            })
                            .catch(function (error) {
                                if (error.status === 404) {
                                    alert('Product not found.');
                                } else {
                                    alert('An error occurred: ' + error.statusText);
                                }
                            });

                    }
                    if (productName != null && productPrice != null){
                        $http.get("api/gateway/inventory/" + inventoryId + "/products?productName=" + productName + "&productPrice=" + productPrice)
                            .then(function (resp) {
                                self.inventoryProductList = resp.data;
                                arr = resp.data;
                            })
                            .catch(function (error) {
                                if (error.status === 404) {
                                    alert('Product not found.');
                                } else {
                                    alert('An error occurred: ' + error.statusText);
                                }
                            });
                    }
                    if (productPrice != null && productQuantity != null){
                        $http.get("api/gateway/inventory/" + inventoryId + "/products?productQuantity=" + productQuantity + "&productPrice=" + productPrice)
                            .then(function (resp) {
                                self.inventoryProductList = resp.data;
                                arr = resp.data;
                            })
                            .catch(function (error) {
                                if (error.status === 404) {
                                    alert('Product not found.');
                                } else {
                                    alert('An error occurred: ' + error.statusText);
                                }
                            });
                    }
                    if (productPrice != null){
                        $http.get("api/gateway/inventory/" + inventoryId + "/products?productPrice=" + productPrice)
                            .then(function (resp) {
                            self.inventoryProductList = resp.data;
                                arr = resp.data;
                            })
                            .catch(function (error) {
                                if (error.status === 404) {
                                    alert('Product not found.');
                                } else {
                                    alert('An error occurred: ' + error.statusText);
                                }
                            });
                    }
                    if (productQuantity != null){
                        $http.get("api/gateway/inventory/" + inventoryId + "/products?productQuantity=" + productQuantity)
                            .then(function (resp) {
                                self.inventoryProductList = resp.data;
                                arr = resp.data;
                            })
                            .catch(function (error) {
                                if (error.status === 404) {
                                    alert('Product not found.');
                                } else {
                                    alert('An error occurred: ' + error.statusText);
                                }
                            });
                    }
                    if (productName != null){
                        $http.get("api/gateway/inventory/" + inventoryId + "/products?productName=" + productName)
                            .then(function (resp) {
                                self.inventoryProductList = resp.data;
                                arr = resp.data;
                            })
                            .catch(function (error) {
                            if (error.status === 404) {
                                alert('Product not found.');
                            } else {
                                alert('An error occurred: ' + error.statusText);
                            }
                        });

                    }
                }
        $scope.deleteAllProducts = function () {
            let varIsConf = confirm('Are you sure you want to delete all products for this inventory?');
            if (varIsConf) {
                let inventoryId = $stateParams.inventoryId;  // Retrieve the inventoryId from the appropriate location

                $http.delete('api/gateway/inventory/' + inventoryId + '/products')
                    .then(function(response) {
                        alert("All products for this inventory have been deleted!");


                        $scope.fetchProductList();
                    }, function(error) {
                        alert(error.data.errors);
                        console.log(error, 'Failed to delete all products.');
                    });
            }
        };

        $scope.fetchProductList = function() {
            let inventoryId = $stateParams.inventoryId;
            $http.get('api/gateway/inventory/' + inventoryId + '/products').then(function (resp) {
                $ctrl.inventoryProductList = resp.data;
            });
        };
            }]);

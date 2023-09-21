'use strict';

angular.module('inventoryProductList')
    .controller('InventoryProductController', ['$http', '$scope', '$stateParams', function ($http, $scope, $stateParams) {
        var self = this;
        var inventoryId = $stateParams.inventoryId;
        console.log("State params: " + $stateParams)

                $http.get('api/gateway/inventory/' + inventoryId + '/products').then(function (resp) {
                    self.inventoryProductList = resp.data;


                });
                $scope.deleteProduct = function (inventoryId, productId) {
                    let varIsConf = confirm('Are you sure you want to delete this product?');
                    if (varIsConf) {

                        $http.delete('api/gateway/inventory/' + inventoryId + '/products/' + productId)
                            .then(successCallback, errorCallback)

                        function successCallback(response) {
                            $scope.errors = [];
                            alert(productId + " Deleted Successfully!");
                            console.log(response, 'res');
                            //refresh list
                            $http.get('api/gateway/inventory/' + inventoryId + '/products').then(function (resp) {
                                self.inventoryProductList = resp.data;
                                arr = resp.data;
                            });
                        }

                        function errorCallback(error) {
                            alert(data.errors);
                            console.log(error, 'can not get data.');
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
            }]);

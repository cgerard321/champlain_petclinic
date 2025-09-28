angular.module('productList')
    .controller('ProductController', ['$http', '$scope', '$stateParams','$window', function ($http, $scope, $stateParams, $window) {
        var self = this;
        const pageSize = 15;
        self.currentPage = $stateParams.page || 0;
        self.pageSize = $stateParams.size || pageSize;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        self.baseUrl = "api/v2/gateway/products";
        self.lastParams = {
            productSalePrice: '',
        }
        fetchProductList();

        // DELETE
        $scope.deleteProduct = function(product) {
            let ifConfirmed = confirm('Are you sure you want to remove this product?');
            if (ifConfirmed) {
                // Step 1: Mark as temporarily deleted on frontend.
                product.isTemporarilyDeleted = true;

                // Display an Undo button for say, 5 seconds.
                setTimeout(function() {
                    if (product.isTemporarilyDeleted) {
                        // If it's still marked as deleted after 5 seconds, proceed with actual deletion.
                        proceedToDelete(product);
                    }
                }, 5000);  // 5 seconds = 5000ms.
            }
        };

        $scope.undoDelete = function(product) {
            product.isTemporarilyDeleted = false;
            // Hide the undo button.
        };

        function proceedToDelete(product) {
            if (!product.isTemporarilyDeleted) return;  // In case the user clicked undo just before the timeout.

            $http.delete('api/v2/gateway/products/' + product.productId)
                .then(successCallback, errorCallback)

            function showNotification(message) {
                const notificationElement = document.getElementById('notification');
                notificationElement.innerHTML = message;
                notificationElement.style.display = 'block';

                setTimeout(() => {
                    notificationElement.style.display = 'none';
                }, 5000);  // Hide after 5 seconds
            }

            function successCallback(response) {
                $scope.errors = [];
                console.log(response, 'res');

                // After deletion, wait for a short moment (e.g., 1 second) before showing the notification
                setTimeout(() => {
                    showNotification(product.productName + " has been deleted successfully!");
                    // Then, after displaying the notification for 5 seconds, reload the page
                    setTimeout(() => {
                        location.reload();
                    }, 1000);
                }, 1000);  // Wait for 1 second before showing notification
            }
            function errorCallback(error) {
                // If the error message is nested under 'data.errors' in your API response:
                alert(error.data.errors);
                console.log(error, 'Data is inaccessible.');
            }
        }


        $scope.clearQueries = function (){
            self.lastParams.productSalePrice = '';

            // Clear the input fields
            $scope.productSalePrice = '';
            $scope.searchProduct('');
        }
        //currently removed implementation
        $scope.searchProduct = function(productSalePrice){
            var queryString = '';
            resetDefaultValues()

            if (productSalePrice && productSalePrice !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "maxPrice=" + productSalePrice;
                self.lastParams.productSalePrice = productSalePrice;
            }

            var apiUrl = "api/v2/gateway/products";
            if (queryString !== '') {
                apiUrl += "?" + queryString;
            }
            let response = [];
            $http.get(apiUrl)
                .then(function(resp) {
                    self.productList = parseProductsFromResponse(resp.data);
                })
                .catch(function(error) {
                    if (error.status === 404) {
                        //alert('Product not found.');
                        self.currentPage = 0;
                        updateActualCurrentPageShown();

                    } else {
                        alert('An error occurred: ' + error.statusText);
                    }
                });
        };

        function fetchProductList(productSalePrice) {
            if (productSalePrice) {
                self.lastParams.productSalePrice = productSalePrice;
                self.searchProduct(productSalePrice)
            }
            else {
                self.lastParams.productPrice = null;

                $http.get('api/v2/gateway/products').then(function (resp) {
                    //console.log(resp);
                    self.productList = parseProductsFromResponse(resp.data);
                    if (resp.data === 0) {
                        // Handle if inventory is empty
                        console.log("The products list is empty!");
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
                fetchProductList(self.lastParams.productSalePrice);
            }
        }

        self.previousPage = function () {
            if (self.currentPage - 1 >= 0) {
                var currentPageInt = parseInt(self.currentPage) - 1
                self.currentPage = currentPageInt.toString();
                updateActualCurrentPageShown();
                // Refresh the owner's list with the new page size
                fetchProductList(self.lastParams.productSalePrice);
            }
        }
        function resetDefaultValues() {
            self.currentPage = 0;
            self.pageSize = pageSize;
            self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
            self.lastParams = {
                productName: '',
                productQuantity: '',
                productSalePrice: ''
            }

        }

        function updateActualCurrentPageShown() {
            self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        }

        function parseProductsFromResponse(rawData) {
            let products = [];

            // Split by "\n\n" to get each data line
            let lines = rawData.split('\n\n');

            lines.forEach(line => {
                line = line.trim();
                if (line.startsWith('data:')) {
                    let jsonString = line.substring(5); // remove 'data:'
                    try {
                        let product = JSON.parse(jsonString);
                        product.productSalePrice = parseFloat(product.productSalePrice || 0).toFixed(2);
                        products.push(product);
                    } catch (e) {
                        console.error('Failed to parse product:', e, jsonString);
                    }
                }
            });
            //console.log(products);
            return products;
        }
    }]);
'use strict';

angular.module('inventoriesProductList')
    .controller('InventoriesProductController', ['$http', '$scope', '$stateParams','$window', '$location', 'InventoryService', function ($http, $scope, $stateParams, $window, $location, InventoryService) {
        var self = this;
        var inventoryId
        const pageSize = 15;
        self.currentPage = $stateParams.page || 0;
        self.pageSize = $stateParams.size || pageSize;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        self.baseUrl = "api/gateway/inventories/" + $stateParams.inventoryId + "/products-pagination?page=" + self.currentPage + "&size=" + self.pageSize;
        self.baseURLforTotalNumberOfProductsByFiltering = "api/gateway/inventories/" + $stateParams.inventoryId + "/products-count";
        self.lastParams = {
            productName: '',
            productQuantity: '',
            minPrice: '',
            maxPrice: '',
            minSalePrice: '',
            maxSalePrice: ''
        }
        $scope.inventory = {};

        $http.get('api/gateway/inventories/' + $stateParams.inventoryId).then(function (resp) {
            $scope.inventory = resp.data;
        });
        fetchProductList();

        // ===== helper to get the logged-in user's email (stored at login) =====
        function getCurrentEmail() {
            try {
                var stored = localStorage.getItem('auth.user');
                if (stored) {
                    var obj = JSON.parse(stored);
                    return obj && obj.email ? obj.email : null;
                }
            } catch (e) {}
            return null;
        }

        // Fallback: if email is stored as raw string, support that too (non-breaking)
        (function patchEmailFallback(){
            if (!getCurrentEmail.__patched) {
                var _orig = getCurrentEmail;
                getCurrentEmail = function(){
                    var v = _orig.call(this);
                    if (v) return v;
                    try { return localStorage.getItem('email') || null; } catch(e) { return null; }
                };
                getCurrentEmail.__patched = true;
            }
        })();

        // Temporarily whitelist current route so a 401 from the login probe won't purge/redirect
        function withTempWhitelist(run) {
            var added = false;
            try {
                var key = $location.path().substring(1); // same logic as interceptor
                if (typeof whiteList !== 'undefined' && !whiteList.has(key)) {
                    whiteList.add(key);
                    added = true;
                }
            } catch (e) {}
            return Promise.resolve()
                .then(run)
                .finally(function () {
                    try {
                        if (added) {
                            var key = $location.path().substring(1);
                            whiteList.delete(key);
                        }
                    } catch (e) {}
                });
        }

        // Prompt for creds with up to N retries; never logs the user out
        function promptAndVerify(maxTries) {
            var tries = 0;
            return new Promise(function(resolve, reject) {
                (function ask() {
                    var email = getCurrentEmail() || prompt('Enter your account email to confirm:');
                    if (!email) return reject(new Error('cancelled'));
                    var password = prompt('Enter ADMIN or INVENTORY MANAGER password to confirm:');
                    if (!password) return reject(new Error('cancelled'));

                    withTempWhitelist(function () {
                        return $http.post('/api/gateway/users/login', { email: email, password: password });
                    })
                    .then(function(){ resolve({ email: email, password: password }); })
                    .catch(function(err){
                        tries++;
                        if (err && err.status === 401) {
                            alert('Wrong password. Please try again.');
                            if (tries < maxTries) return ask();
                            return reject(new Error('max-tries'));
                        }
                        alert('Authentication failed. Please try again.');
                        reject(err || new Error('auth-failed'));
                    });
                })();
            });
        }

        // ===== refined delete: warn -> (retrying) verify -> delete =====
        $scope.deleteProduct = function (product) {
            // Acceptance criteria: show irreversible warning
            if (!confirm('Warning: Deleting this product cannot be undone. Continue?')) {
                return;
            }

            // Verify with retries (3 tries) WITHOUT logging out on 401
            promptAndVerify(3)
                .then(function () {
                    // Only after successful auth, proceed to actual deletion
                    return proceedToDelete(product);
                })
                .catch(function (e) {
                    if (e && e.message === 'cancelled') return;        // user cancelled a prompt
                    if (e && e.message === 'max-tries') { alert('Too many failed attempts.'); return; }
                    // Other errors already alerted in promptAndVerify
                });
        };

        // Keeping undo function to avoid breaking other parts, but it no longer gates deletion.
        $scope.undoDelete = function(product) {
            product.isTemporarilyDeleted = false;
            // Hide the undo button.
        };

        function proceedToDelete(product) {
            // Do not rely on temporary flag; delete only after auth above
            return $http.delete('api/gateway/inventories/' + product.inventoryId + '/products/' + product.productId)
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

                // After deletion, wait for a short moment before showing the notification
                setTimeout(() => {
                    showNotification(product.productName + " has been deleted successfully!");
                    // Then, quick refresh (kept your existing behavior)
                    setTimeout(() => {
                        location.reload();
                    }, 1000);
                }, 1000);  // Wait for 1 second before showing notification
            }
            function errorCallback(error) {
                // Do not change UI; deletion didn’t happen
                // If the error message is nested under 'data.errors' in your API response:
                try {
                    alert(error.data.errors || error.data.message || 'Delete failed');
                } catch (e) {
                    alert('Delete failed');
                }
                console.log(error, 'Data is inaccessible.');
            }
        }

        $scope.clearQueries = function (){
            self.lastParams.productName = '';
            self.lastParams.productQuantity = '';
            self.lastParams.minPrice = '';
            self.lastParams.maxPrice = '';
            self.lastParams.minSalePrice = '';
            self.lastParams.maxSalePrice = '';

            // Clear the input fields
            $scope.productName = '';
            $scope.productQuantity = '';
            $scope.minPrice = '';
            $scope.maxPrice = '';
            $scope.minSalePrice = '';
            $scope.maxSalePrice = '';
            $scope.searchProduct('', '', '', '', '', '');
        }

        $scope.searchProduct = function(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice) {
            if (productQuantity !== undefined && productQuantity !== null && productQuantity !== '' && productQuantity <= 0) {
                alert('Quantity cannot be 0 or negative. Please enter a valid quantity.');
                return;
            }

            if (minPrice !== undefined && minPrice !== null && minPrice !== '' && minPrice <= 0) {
                alert('Min Price cannot be 0 or negative. Please enter a valid min price.');
                return;
            }
            if (maxPrice !== undefined && maxPrice !== null && maxPrice !== '' && maxPrice <= 0) {
                alert('Max Price cannot be 0 or negative. Please enter a valid max price.');
                return;
            }
            if (minSalePrice !== undefined && minSalePrice !== null && minSalePrice !== '' && minSalePrice <= 0) {
                alert('Min Sale Price cannot be 0 or negative. Please enter a valid min sale price.');
                return;
            }
            if (maxSalePrice !== undefined && maxSalePrice !== null && maxSalePrice !== '' && maxSalePrice <= 0) {
                alert('Max Sale Price cannot be 0 or negative. Please enter a valid max sale price.');
                return;
            }

            if (
                minPrice !== undefined && minPrice !== null && minPrice !== '' &&
                maxPrice !== undefined && maxPrice !== null && maxPrice !== '' &&
                maxPrice < minPrice
            ) {
                alert('Max price must be larger than min price.');
                return;
            }
            if (
                minSalePrice !== undefined && minSalePrice !== null && minSalePrice !== '' &&
                maxSalePrice !== undefined && maxSalePrice !== null && maxSalePrice !== '' &&
                maxSalePrice < minSalePrice
            ) {
                alert('Max sale price must be larger than min sale price.');
                return;
            }

            var inventoryId = $stateParams.inventoryId;
            var queryString = '';
            resetDefaultValues()

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

            if (minPrice && minPrice !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "minPrice=" + minPrice;
                self.lastParams.minPrice = minPrice;
            }

            if (maxPrice && maxPrice !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "maxPrice=" + maxPrice;
                self.lastParams.maxPrice = maxPrice;
            }

            if (minSalePrice && minSalePrice !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "minSalePrice=" + minSalePrice;
                self.lastParams.minSalePrice = minSalePrice;
            }

            if (maxSalePrice && maxSalePrice !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "maxSalePrice=" + maxSalePrice;
                self.lastParams.maxSalePrice = maxSalePrice;
            }

            var apiUrl = "api/gateway/inventories/" + inventoryId + "/products";
            if (queryString !== '') {
                apiUrl += "?" + queryString;
            }
            let response = [];
            $http.get(apiUrl)
                .then(function(resp) {
                    resp.data.forEach(function (current) {
                        current.productPrice = current.productPrice.toFixed(2);
                        current.productSalePrice = current.productSalePrice.toFixed(2);
                        response.push(current);
                    });
                    self.inventoryProductList = response;
                    loadTotalItem(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice)
                    InventoryService.setInventoryId(inventoryId);
                })
                .catch(function(error) {
                    if (error.status === 404) {
                        self.inventoryProductList = [];
                        self.currentPage = 0;
                        updateActualCurrentPageShown();
                    } else {
                        alert('An error occurred: ' + error.statusText);
                    }
                });
        };

        function fetchProductList(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice) {
            if (productName || productQuantity || minPrice || maxPrice || minSalePrice || maxSalePrice) {
                self.lastParams.productName = productName;
                self.lastParams.productQuantity = productQuantity;
                self.lastParams.minPrice = minPrice;
                self.lastParams.maxPrice = maxPrice;
                self.lastParams.minSalePrice = minSalePrice;
                self.lastParams.maxSalePrice = maxSalePrice;
                self.searchProduct(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice)
            }
            else {
                self.lastParams.productName = null;
                self.lastParams.productQuantity = null;
                self.lastParams.minPrice = null;
                self.lastParams.maxPrice = null;
                self.lastParams.minSalePrice = null;
                self.lastParams.maxSalePrice = null;
                let inventoryId = $stateParams.inventoryId;
                let response = [];
                $http.get('api/gateway/inventories/' + $stateParams.inventoryId + '/products-pagination?page=' + self.currentPage + '&size=' + self.pageSize).then(function (resp) {
                    resp.data.forEach(function (current) {
                        current.productPrice = current.productPrice.toFixed(2);
                        current.productSalePrice = current.productSalePrice.toFixed(2);
                        response.push(current);
                    });
                    self.inventoryProductList = response;
                    inventoryId = $stateParams.inventoryId;
                    loadTotalItem(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice)
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
                fetchProductList(self.lastParams.productName, self.lastParams.productQuantity, self.lastParams.minPrice, self.lastParams.maxPrice, self.lastParams.minSalePrice, self.lastParams.maxSalePrice);
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
                fetchProductList(self.lastParams.productName, self.lastParams.productQuantity, self.lastParams.minPrice, self.lastParams.maxPrice, self.lastParams.minSalePrice, self.lastParams.maxSalePrice);
            }
        }
        function resetDefaultValues() {
            self.currentPage = 0;
            self.pageSize = pageSize;
            self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
            self.lastParams = {
                productName: '',
                productQuantity: '',
                minPrice: '',
                maxPrice: '',
                minSalePrice: '',
                maxSalePrice: ''
            }

        }

        function updateActualCurrentPageShown() {
            self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
            console.log(self.currentPage);
        }
        function loadTotalItem(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice) {
            var query = ''
            if (productName) {
                if (query === ''){
                    query +='?productName='+productName
                }
            }
            if (productQuantity){
                if (query === ''){
                    query +='?productQuantity='+productQuantity
                } else{
                    query += '&productQuantity='+productQuantity
                }
            }
            if (minPrice){
                if (query === ''){
                    query +='?minPrice='+minPrice
                } else{
                    query += '&minPrice='+minPrice
                }
            }
            if (maxPrice){
                if (query === ''){
                    query +='?maxPrice='+maxPrice
                } else{
                    query += '&maxPrice='+maxPrice
                }
            }
            if (minSalePrice){
                if (query === ''){
                    query +='?minSalePrice='+minSalePrice
                } else{
                    query += '&minSalePrice='+minSalePrice
                }
            }
            if (maxSalePrice){
                if (query === ''){
                    query +='?maxSalePrice='+maxSalePrice
                } else{
                    query += '&maxSalePrice='+maxSalePrice
                }
            }
            $http.get('api/gateway/inventories/' + $stateParams.inventoryId + '/products-count' + query)
                .then(function (resp) {
                    self.totalItems = resp.data;
                    self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));
                });
        }
    }]);

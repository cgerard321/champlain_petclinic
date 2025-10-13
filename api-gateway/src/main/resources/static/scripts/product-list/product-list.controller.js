angular.module('productList')
    .controller('ProductController', ['$http', '$scope', '$stateParams','$window', '$uibModal', '$interval',
        function ($http, $scope, $stateParams, $window, $uibModal, $interval) {
        var self = this;
        const pageSize = 15;
        self.currentPage = $stateParams.page || 0;
        self.pageSize = $stateParams.size || pageSize;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        self.baseUrl = "api/gateway/products";
        self.lastParams = {
            productSalePrice: '',
        }
        self.productType = ["FOOD", "MEDICATION", "ACCESSORY", "EQUIPMENT"];
        fetchProductList();

            $scope.toast = { visible: false, title: '', lines: [], actions: [] };
            let toastTimer = null;

            function toastShow({ title = '', lines = [], actions = [], autoHideMs = 0 }) {
                // clear any previous timer
                if (toastTimer) { clearTimeout(toastTimer); toastTimer = null; }
                // assign
                $scope.toast.title = title;
                $scope.toast.lines = lines;
                $scope.toast.actions = actions;
                $scope.toast.visible = true;
                // optional auto hide
                if (!actions.length && autoHideMs > 0) {
                    toastTimer = setTimeout(toastHide, autoHideMs);
                }
            }
            function toastHide() {
                $scope.toast.visible = false;
                $scope.toast.title = ''; $scope.toast.lines = []; $scope.toast.actions = [];
                if (toastTimer) { clearTimeout(toastTimer); toastTimer = null; }
            }
            // wire action buttons -> run stored callbacks
            $scope.onToastAction = function (index) {
                try {
                    const action = $scope.toast.actions[index];
                    toastHide();
                    if (action && typeof action.onClick === 'function') action.onClick();
                } catch (e) { /* no-op */ }
            };

            // ---------- API helpers ----------
            function performDelete(productId, cascade) {
                const url = self.baseUrl + '/' + encodeURIComponent(productId);
                const cfg = cascade ? { params: { cascadeBundles: true } } : undefined;
                return $http.delete(url, cfg);
            }

            // ---------- 5s soft delete with Undo → then handle 409 ----------
            function scheduleSoftDelete(product) {
                product.isTemporarilyDeleted = true;

                toastShow({
                    title: 'Deleting in 5 seconds…',
                    lines: [ product.productName ],
                    actions: [
                        { label: 'Undo', kind: 'secondary', onClick: function () {
                                product.isTemporarilyDeleted = false;
                            }
                        }
                    ]
                });

                setTimeout(function () {
                    if (!product.isTemporarilyDeleted) return;

                    // 1st attempt: normal delete (no cascade)
                    performDelete(product.productId, /*cascade*/ false)
                        .then(function () {
                            toastShow({ title: 'Success', lines: ['Product deleted.'], autoHideMs: 2000 });
                            setTimeout(function(){ location.reload(); }, 800);
                        })
                        .catch(function (err) {
                            if (err.status === 409) {
                                // product is part of bundle(s) — ask for cascade, no HTML in controller
                                toastShow({
                                    title: 'Product is part of a bundle',
                                    lines: ['Deleting will also delete its bundle(s). Continue?'],
                                    actions: [
                                        { label: 'Delete all', kind: 'danger', onClick: function () {
                                                performDelete(product.productId, /*cascade*/ true)
                                                    .then(function () {
                                                        toastShow({ title: 'Success', lines: ['Product and bundle(s) deleted.'], autoHideMs: 2000 });
                                                        setTimeout(function(){ location.reload(); }, 800);
                                                    })
                                                    .catch(function (err2) {
                                                        product.isTemporarilyDeleted = false;
                                                        const msg = (err2.data && err2.data.message) || err2.statusText || 'Delete failed';
                                                        toastShow({ title: 'Error', lines: [msg], autoHideMs: 3000 });
                                                    });
                                            }
                                        },
                                        { label: 'Cancel', kind: 'secondary', onClick: function () {
                                                product.isTemporarilyDeleted = false;
                                            }
                                        }
                                    ]
                                });

                            } else {
                                product.isTemporarilyDeleted = false;
                                const msg = (err.data && err.data.message) || err.statusText || 'Delete failed';
                                toastShow({ title: 'Error', lines: [msg], autoHideMs: 3000 });
                            }
                        });

                }, 5000);
            }

            // ---------- public click handler: 3 chances to cancel ----------
            $scope.deleteProduct = function (product) {
                // chance #1: confirm
                toastShow({
                    title: 'Delete product?',
                    lines: ['Are you sure you want to delete "' + product.productName + '"?'],
                    actions: [
                        { label: 'Delete', kind: 'danger', onClick: function () {
                                // chance #2: 5s undo
                                scheduleSoftDelete(product);
                            }
                        },
                        { label: 'Cancel', kind: 'secondary', onClick: function () {} }
                    ]
                });
            };

            // // DELETE
        // $scope.deleteProduct = function(product) {
        //     let ifConfirmed = confirm('Are you sure you want to remove this product?');
        //     if (ifConfirmed) {
        //         // Step 1: Mark as temporarily deleted on frontend.
        //         product.isTemporarilyDeleted = true;
        //
        //         // Display an Undo button for say, 5 seconds.
        //         setTimeout(function() {
        //             if (product.isTemporarilyDeleted) {
        //                 // If it's still marked as deleted after 5 seconds, proceed with actual deletion.
        //                 proceedToDelete(product);
        //             }
        //         }, 5000);  // 5 seconds = 5000ms.
        //     }
        // };
        //
        // $scope.undoDelete = function(product) {
        //     product.isTemporarilyDeleted = false;
        //     // Hide the undo button.
        // };
        //
        // function proceedToDelete(product) {
        //     if (!product.isTemporarilyDeleted) return;  // In case the user clicked undo just before the timeout.
        //
        //     $http.delete('api/gateway/products/' + product.productId)
        //         .then(successCallback, errorCallback)
        //
        //     function showNotification(message) {
        //         const notificationElement = document.getElementById('notification');
        //         notificationElement.innerHTML = message;
        //         notificationElement.style.display = 'block';
        //
        //         setTimeout(() => {
        //             notificationElement.style.display = 'none';
        //         }, 5000);  // Hide after 5 seconds
        //     }
        //
        //     function successCallback(response) {
        //         $scope.errors = [];
        //         console.log(response, 'res');
        //
        //         // After deletion, wait for a short moment (e.g., 1 second) before showing the notification
        //         setTimeout(() => {
        //             showNotification(product.productName + " has been deleted successfully!");
        //             // Then, after displaying the notification for 5 seconds, reload the page
        //             setTimeout(() => {
        //                 location.reload();
        //             }, 1000);
        //         }, 1000);  // Wait for 1 second before showing notification
        //     }
        //     function errorCallback(error) {
        //         // If the error message is nested under 'data.errors' in your API response:
        //         alert(error.data.errors);
        //         console.log(error, 'Data is inaccessible.');
        //     }
        // }


        $scope.clearQueries = function (){
            self.lastParams.productSalePrice = '';

            // Clear the input fields
            $scope.productSalePrice = '';
            $scope.searchProduct('');
            $scope.productType = '';
            $scope.averageRating = '';
        }

        $scope.searchProduct = function(productSalePrice, productType, averageRating){
            var queryString = '';
            resetDefaultValues()

            if (productSalePrice && productSalePrice !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "maxPrice=" + productSalePrice;
                self.lastParams.productSalePrice = productSalePrice;
            }

            if (productType && productType !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "productType=" + productType;
                self.lastParams.productType = productType;
            }

            if (averageRating && averageRating !== '') {
                if (queryString !== '') {
                    queryString += "&";
                }
                queryString += "maxRating=" + averageRating;
                self.lastParams.averageRating = averageRating;
            }

            var apiUrl = "api/gateway/products";
            if (queryString !== '') {
                apiUrl += "?" + queryString;
            }
            let response = [];
            $http.get(apiUrl)
                .then(function(resp) {
                    self.productList = parseProductsFromResponse(resp.data);
                    fetchImages();
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

        function fetchProductList(productSalePrice, productType, averageRating) {
            if (productSalePrice) {
                self.lastParams.productSalePrice = productSalePrice;
                self.lastParams.productType = productType;
                self.lastParams.averageRating = averageRating;
                self.searchProduct(productSalePrice, productType, averageRating)
            }
            else {
                self.lastParams.productPrice = null;

                $http.get('api/gateway/products').then(function (resp) {
                    //console.log(resp);
                    self.productList = parseProductsFromResponse(resp.data);
                    if (resp.data === 0) {
                        // Handle if inventory is empty
                        console.log("The products list is empty!");
                    }
                    fetchImages()

                }).catch(function (error) {
                    console.error('An error occurred:', error);
                });
            }
        }
        self.nextPage = function () {S
            if (parseInt(self.currentPage) + 1 < self.totalPages) {
                var currentPageInt = parseInt(self.currentPage) + 1
                self.currentPage = currentPageInt.toString();
                updateActualCurrentPageShown();
                //refresh product list
                fetchProductList(self.lastParams.productSalePrice, self.lastParams.productType, self.lastParams.averageRating);
            }
        }

        self.previousPage = function () {
            if (self.currentPage - 1 >= 0) {
                var currentPageInt = parseInt(self.currentPage) - 1
                self.currentPage = currentPageInt.toString();
                updateActualCurrentPageShown();
                // Refresh the owner's list with the new page size
                fetchProductList(self.lastParams.productSalePrice, self.lastParams.productType, self.lastParams.averageRating);
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

        function fetchImages(){
        //fetch image
            self.productList.forEach(function (row) {
            $http.get('api/gateway/images/' + row.imageId).then(function (imageResp){
            if(imageResp.data === 0){
            console.log("no image found");
            return;
            }
            row.imageData = imageResp.data.imageData;
            row.imageType = imageResp.data.imageType;
            })
                .catch(function (err) {
                console.error("Error fetching image: ", err);
                });
            });
        }
    }]);
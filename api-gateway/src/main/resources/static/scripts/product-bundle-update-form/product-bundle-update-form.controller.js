'use strict';

angular.module('productBundleUpdateForm')
    .controller('ProductBundleUpdateFormController', [
        '$http', '$state', '$stateParams',
        function ($http, $state, $stateParams) {

            var self = this;

            self.loading = true;
            self.error = null;

            self.bundle = {};
            self.resolvedProducts = [];
            self.currentProductIds = [];

            var baseBundlesUrl = '/api/gateway/products/bundles';
            var baseProductsUrl = '/api/gateway/products';
            var baseImagesUrl  = '/api/gateway/images';


            function init() {
                var bundleId = $stateParams.bundleId;

                if (!bundleId) {
                    self.error = 'No bundle ID provided.';
                    self.loading = false;
                    return;
                }

                $http.get(baseBundlesUrl + '/' + encodeURIComponent(bundleId))
                    .then(function (resp) {
                        self.bundle = resp.data || {};

                        self.currentProductIds = Array.isArray(self.bundle.productIds)
                            ? angular.copy(self.bundle.productIds)
                            : [];

                        if (self.bundle.originalTotalPrice != null) {
                            self.bundle.originalTotalPrice =
                                parseFloat(self.bundle.originalTotalPrice);
                        }
                        if (self.bundle.bundlePrice != null) {
                            self.bundle.bundlePrice =
                                parseFloat(self.bundle.bundlePrice);
                        } else {
                            self.bundle.bundlePrice = 0.00;
                        }

                        var productIds = self.bundle.productIds || [];
                        if (!productIds.length) {
                            self.resolvedProducts = [];
                            self.loading = false;
                            return;
                        }

                        return fetchProducts(productIds);

                    })
                    .catch(function (err) {
                        console.error('Error loading bundle:', err);
                        self.error = 'Failed to load bundle.';
                    })
                    .finally(function () {
                        self.loading = false;
                    });
            }

            function fetchProducts(productIds) {

                var promises = productIds.map(function (pid) {
                    return $http.get(baseProductsUrl + '/' + encodeURIComponent(pid))
                        .then(function (pResp) {
                            var product = pResp.data;

                            if (product.productSalePrice != null) {
                                product.productSalePrice =
                                    parseFloat(product.productSalePrice).toFixed(2);
                            }

                            if (product.imageId) {
                                return $http.get(baseImagesUrl + '/' + product.imageId)
                                    .then(function (imgResp) {
                                        product.imageData = imgResp.data.imageData;
                                        product.imageType = imgResp.data.imageType;
                                        return product;
                                    })
                                    .catch(function () {
                                        return product;
                                    });
                            } else {
                                return product;
                            }
                        })
                        .catch(function (err) {
                            console.warn('Failed to load product ' + pid, err);
                            return null;
                        });
                });

                return Promise.all(promises).then(function (resultProducts) {
                    self.resolvedProducts = resultProducts.filter(Boolean);
                    self.loading = false;
                }).catch(function (err) {
                    console.error('Error resolving products:', err);
                    self.loading = false;
                });
            }

            self.submitBundleUpdateForm = function () {

                if (self.loading) {
                    alert('Still loading bundle data, please wait a moment.');
                    return;
                }

                if (!self.bundle.bundleId) {
                    alert('Bundle ID is missing.');
                    return;
                }

                if (!self.currentProductIds || !self.currentProductIds.length) {
                    alert('This bundle appears to have no products. ' +
                        'Updating a bundle with no products is not allowed.');
                    return;
                }

                var body = {
                    bundleName:        self.bundle.bundleName,
                    bundleDescription: self.bundle.bundleDescription,
                    productIds:        self.currentProductIds,
                    bundlePrice:       self.bundle.bundlePrice
                };

                console.log('PUT /api/gateway/products/bundles/' + self.bundle.bundleId + ' payload ->', body);

                $http.put(
                    baseBundlesUrl + '/' + encodeURIComponent(self.bundle.bundleId),
                    body
                ).then(function () {
                    $state.go('productBundleList');

                }).catch(function (response) {
                    var error = response.data || {};
                    error.errors = error.errors || [];

                    alert(
                        (error.error || 'Update failed') + "\r\n" +
                        error.errors.map(function (e) {
                            return e.field + ": " + e.defaultMessage;
                        }).join("\r\n")
                    );

                    console.error('Bundle update failed:', response);
                });
            };

            init();
        }
    ]);

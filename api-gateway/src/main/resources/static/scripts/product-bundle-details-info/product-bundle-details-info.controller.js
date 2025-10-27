'use strict';

angular.module('productBundleDetailsInfo')
    .controller('ProductBundleDetailsInfoController', [
        '$http', '$stateParams',
        function ($http, $stateParams) {

            var self = this;
            self.loading = true;
            self.error = null;
            self.bundle = {};
            self.resolvedProducts = [];

            var baseBundlesUrl = '/api/gateway/products/bundles';
            var baseProductsUrl = '/api/gateway/products';
            var baseImagesUrl = '/api/gateway/images';

            function init() {
                var bundleId = $stateParams.bundleId;

                if (!bundleId) {
                    self.error = 'No bundle ID provided.';
                    self.loading = false;
                    return;
                }

                // 1. get bundle
                $http.get(baseBundlesUrl + '/' + encodeURIComponent(bundleId))
                    .then(function (resp) {
                        self.bundle = resp.data || {};
                        // normalize numbers
                        if (self.bundle.originalTotalPrice != null) {
                            self.bundle.originalTotalPrice =
                                parseFloat(self.bundle.originalTotalPrice).toFixed(2);
                        }
                        if (self.bundle.bundlePrice != null) {
                            self.bundle.bundlePrice =
                                parseFloat(self.bundle.bundlePrice).toFixed(2);
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
                        self.loading = false;
                    });
            }

            // 2. load each product and its image
            function fetchProducts(productIds) {
                var promises = productIds.map(function (pid) {
                    return $http.get(baseProductsUrl + '/' + encodeURIComponent(pid))
                        .then(function (pResp) {
                            var product = pResp.data;

                            // normalize price to match list page formatting
                            if (product.productSalePrice != null) {
                                product.productSalePrice =
                                    parseFloat(product.productSalePrice).toFixed(2);
                            }

                            // if it has an imageId, load image
                            if (product.imageId) {
                                return $http.get(baseImagesUrl + '/' + product.imageId)
                                    .then(function (imgResp) {
                                        product.imageData = imgResp.data.imageData;
                                        product.imageType = imgResp.data.imageType;
                                        return product;
                                    })
                                    .catch(function () {
                                        // no image or failed load
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

                // merge results
                Promise.all(promises).then(function (resultProducts) {
                    self.resolvedProducts = resultProducts.filter(Boolean);
                    self.loading = false;
                }).catch(function (err) {
                    console.error('Error resolving products:', err);
                    self.loading = false;
                });
            }

            init();
        }]);

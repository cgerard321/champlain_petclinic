'use strict';

angular.module('productBundleForm')
    .controller('ProductBundleFormController', ['$http', '$state', function ($http, $state) {
        var self = this;

        self.bundle = {
            bundleName: '',
            bundleDescription: '',
            bundlePrice: 0,
            originalTotalPrice: 0
        };

        self.allProducts = [];

        // Parse stream or array
        function parseProductsFromResponse(rawData) {
            var products = [];
            if (!rawData || typeof rawData !== 'string') return products;

            rawData.split('\n\n').forEach(function (line) {
                line = line.trim();
                if (line.startsWith('data:')) {
                    try {
                        var product = JSON.parse(line.substring(5));
                        product.productSalePrice = parseFloat(product.productSalePrice || 0);
                        products.push(product);
                    } catch (e) {
                        console.warn('Failed to parse product line:', e);
                    }
                }
            });
            return products;
        }

        // Load products
        self.loadProducts = function () {
            $http.get('/api/gateway/products')
                .then(function (response) {
                    if (Array.isArray(response.data)) {
                        self.allProducts = response.data;
                    } else if (typeof response.data === 'string') {
                        self.allProducts = parseProductsFromResponse(response.data);
                    }
                    self.allProducts.forEach(function(p) { p.selected = false;});
                })
                .catch(err => {
                    console.error('Error loading products:', err);
                    alert('Could not load products list.');
                });
        };

        // Called whenever a checkbox changes
        self.updateSelectedProducts = function () {
            var selected = self.allProducts.filter(p => p.selected);
            var total = selected.reduce((sum, p) => sum + (parseFloat(p.productSalePrice) || 0), 0);
            self.bundle.originalTotalPrice = parseFloat(total.toFixed(2));
        };

        // Submit handler
        self.submitBundleForm = function () {
            var selected = self.allProducts.filter(function(p) { return p.selected; });
            if (!self.bundle.bundleName || !self.bundle.bundleDescription || selected.length === 0) {
                alert('Please fill out all required fields and select at least one product.');
                return;
            }

            var payload = {
                bundleName: self.bundle.bundleName,
                bundleDescription: self.bundle.bundleDescription,
                productIds: selected.map(function(p) { return p.productId; }),
                originalTotalPrice: self.bundle.originalTotalPrice,
                bundlePrice: self.bundle.bundlePrice
            };

            $http.post('/api/gateway/products/bundles', payload)
                .then(function() {
                    alert('Bundle created successfully!');
                    $state.go('productBundleList');
                })
                .catch(function(error) {
                    console.error('Bundle creation failed:', error);
                    var err = error.data || {};
                    alert((err.error || 'Error creating bundle') + '\n' +
                        (err.errors ? err.errors.map(function(e) { return e.field + ': ' + e.defaultMessage; }).join('\n') : ''));
                });
        };

        // init
        self.loadProducts();
    }]);

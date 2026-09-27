'use strict';

angular.module('customerForm')
    .controller('CustomerFormController', ["$http", '$state', '$stateParams', '$scope', function ($http, $state, $stateParams, $scope) {
        var self = this;
        var customerId = $stateParams.customerId || "";
        var method = $stateParams.method;

        // Initialize
        self.customer = {};
        self.checked = false;
        self.showModal = false;

        if (customerId) {
            $http.get("api/gateway/customers/" + customerId).then(function (resp) {
                self.customer = resp.data;
            });
            if (method !== 'edit') {
                self.checked = true;
            }
        }

        // Open confirmation modal
        self.submitCustomerForm = function () {
            self.showModal = true;
        };

        // Cancel modal
        self.cancelModal = function () {
            self.showModal = false;
        };

        // Confirm modal: submit form
        self.confirmModal = function () {
            self.showModal = false;

            var req;
            if (self.customer.customerId) {
                if (method === 'edit') {
                    req = $http.put("api/gateway/customers/" + self.customer.customerId, self.customer);
                } else {
                    req = $http.delete("api/gateway/customers/" + self.customer.customerId);
                }
            } else {
                req = $http.post("api/gateway/customers", self.customer);
            }

            req.then(function () {
                $state.go('customers');
            }).catch(function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);

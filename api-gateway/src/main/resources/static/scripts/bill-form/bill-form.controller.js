'use strict';

angular.module('billForm')
    .controller('BillFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        self.submitBillForm = function () {
            var req;
                req = $http.post("api/gateway/bills", self.bill);

            req.then(function () {
                $state.go('bills');
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);

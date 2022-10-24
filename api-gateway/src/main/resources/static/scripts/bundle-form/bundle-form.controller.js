'use strict';

angular.module('bundleForm')
    .controller('BundleFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        self.submitBundleForm = function () {
            var req;
            req = $http.post("api/gateway/bundles", self.bundle);

            req.then(function () {
                $state.go('bundles');
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);

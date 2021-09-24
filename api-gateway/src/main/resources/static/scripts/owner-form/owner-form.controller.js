'use strict';

angular.module('ownerForm')
    .controller('OwnerFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        var ownerId = $stateParams.ownerId || 0;

        if (!ownerId) {
            self.owner = {};
        } else {
            $http.get("api/gateway/customer/owners/" + ownerId).then(function (resp) {
                self.owner = resp.data;
            });
        }

        self.submitOwnerForm = function () {
            var id = self.owner.id;
            var req;
            if (id) {
                req = $http.put("api/gateway/customer/owners/" + id, self.owner);
            } else {
                req = $http.post("api/gateway/customer/owners", self.owner);
            }

            req.then(function () {
                $state.go('owners');
            }, function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
            });
        };
    }]);

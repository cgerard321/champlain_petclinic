'use strict';

angular.module('loginForm')
    .controller('LoginFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        var ownerId = $stateParams.ownerId || 0;

        if (!ownerId) {
            self.login = {};
        } else {
            $http.get("api/gateway/login/" + ownerId).then(function (resp) {
                self.login = resp.data;
            });
        }

        self.submitLoginForm = function () {
            var id = self.owner.id;
            var req;
            if (id) {
                req = $http.put("api/gateway/login/" + id, self.login);
            } else {
                req = $http.post("api/gateway/login", self.login);
            }

            req.then(function () {
                $state.go('login');
            }, function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);
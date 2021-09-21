'use strict';

angular.module('loginForm')
    .controller('LoginFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        var loginId = $stateParams.loginId || 0;

        if (!loginId) {
            self.login = {};
        } else {
            $http.get("api/gateway/login/" + loginId).then(function (resp) {
                self.login = resp.data;
            });
        }

        self.submitLoginForm = function () {
            var id = self.login.id;
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
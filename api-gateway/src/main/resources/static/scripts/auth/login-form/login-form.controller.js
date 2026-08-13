'use strict';

angular.module('loginForm')
    .controller('LoginFormController', [
        '$http', '$location', '$scope', 'authProvider',
        function ($http, $location, $scope, authProvider) {

            var vm = this;
            vm.showPassword = false;
            vm.loading = false;

            $scope.clearErrorMessages = function (i) {
                if (Array.isArray($scope.errorMessages)) {
                    if (typeof i === 'number') $scope.errorMessages.splice(i, 1);
                    else $scope.errorMessages = [];
                } else {
                    $scope.errorMessages = [];
                }
            };

            vm.togglePassword = function () { vm.showPassword = !vm.showPassword; };

            vm.login = function () {
                vm.loading = true;
                $scope.errorMessages = [];

                return $http.post('/api/gateway/users/login', {
                    emailOrUsername: $scope.login && $scope.login.email,
                    password: $scope.login && $scope.login.password,
                })
                    .then(function (res) {
                        authProvider.setUser({
                            username: res.data.username,
                            email: res.data.email,
                            userId: res.data.userId,
                            roles: res.data.roles,
                        });
                        $location.path('/welcome');
                    })
                    .catch(function (err) {
                        var msg = 'Login failed. Please try again.';
                        if (err && err.data) {
                            if (typeof err.data === 'string') msg = err.data;
                            else if (err.data.message) msg = err.data.message;
                            else if (Array.isArray(err.data.errors)) msg = err.data.errors.join('\n');
                        }
                        $scope.errorMessages = String(msg).split('\n');
                    })
                    .finally(function () { vm.loading = false; });
            };

            vm.keypress = function (e) {
                var key = (e && e.originalEvent && e.originalEvent.key) || (e && e.key);
                if (key === 'Enter') vm.login();
            };

            vm.goForgot = function () { $location.path('/forgot_password'); };
        }
    ]);

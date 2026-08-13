'use strict';

angular.module('forgotPwdForm')
    .controller('forgotPwdFormController', [
        '$http', '$scope', '$location',
        function ($http, $scope, $location) {

            var vm = this;
            vm.loading = false;

            $scope.clearErrorMessages = function (i) {
                if (Array.isArray($scope.errorMessages)) {
                    if (typeof i === 'number') $scope.errorMessages.splice(i, 1);
                    else $scope.errorMessages = [];
                } else {
                    $scope.errorMessages = [];
                }
            };

            vm.forgotPwdPost = function () {
                vm.loading = true;
                $scope.errorMessages = [];

                return $http.post('/api/gateway/users/forgot_password', {
                    email: $scope.forgotPwdPost && $scope.forgotPwdPost.email
                })
                    .then(function () {
                        alert('We sent you a reset link.');
                        $location.path('/login');
                    })
                    .catch(function (err) {
                        var msg = (err && err.data && (err.data.message || err.data.email)) || 'Request failed. Please try again.';
                        $scope.errorMessages = String(msg).split('\n');
                    })
                    .finally(function () { vm.loading = false; });
            };

            vm.goLogin = function () { $location.path('/login'); };

            vm.keypress = function (e) {
                var key = (e && e.originalEvent && e.originalEvent.key) || (e && e.key);
                if (key === 'Enter') vm.forgotPwdPost();
            };
        }
    ]);
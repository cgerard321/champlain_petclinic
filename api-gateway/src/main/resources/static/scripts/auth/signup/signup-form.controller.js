'use strict';

angular.module('signupForm')
    .controller('SignupFormController', [
        '$http', '$scope', '$location', 'authProvider',
        function ($http, $scope, $location, authProvider) {

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

            vm.pwStrength = { score: 0, text: '', class: '' };

            vm.evalStrength = function (pwd) {
                if (!pwd) { vm.pwStrength = { score: 0, text: '', class: '' }; return; }

                var score = 0;
                if (pwd.length >= 8) score++;
                if (/[A-Z]/.test(pwd)) score++;
                if (/[a-z]/.test(pwd)) score++;
                if (/\d/.test(pwd)) score++;
                if (/[^A-Za-z0-9]/.test(pwd)) score++;

                var bucket = score <= 2 ? 1 : score <= 4 ? 2 : 3;
                var label = bucket === 1 ? 'Weak' : bucket === 2 ? 'Medium' : 'Strong';

                vm.pwStrength = { score: bucket, text: label, class: 'strength-' + bucket };
            };

            $scope.$watch('signup.password', function (v) { vm.evalStrength(v); });

            vm.togglePassword = function () { vm.showPassword = !vm.showPassword; };

            vm.add = function () {
                vm.loading = true;
                $scope.errorMessages = [];

                var payload = {
                    username: $scope.signup.username,
                    password: $scope.signup.password,
                    email: $scope.signup.email,
                    owner: {
                        firstName: $scope.signup.firstName,
                        lastName: $scope.signup.lastName,
                        address: $scope.signup.address,
                        city: $scope.signup.city,
                        province: $scope.signup.province,
                        telephone: $scope.signup.telephone
                    }
                };

                return $http.post('/api/gateway/users', payload)
                    .then(function () { $location.path('/login'); })
                    .catch(function (err) {
                        var msg = (err && err.data)
                            ? (err.data.password || err.data.message || 'Signup failed. Please try again.')
                            : 'Signup failed. Please try again.';
                        $scope.errorMessages = String(msg).split('\n');
                    })
                    .finally(function () { vm.loading = false; });
            };

            vm.keypress = function (e) {
                var key = (e && e.originalEvent && e.originalEvent.key) || (e && e.key);
                if (key === 'Enter') vm.add();
            };
        }
    ]);

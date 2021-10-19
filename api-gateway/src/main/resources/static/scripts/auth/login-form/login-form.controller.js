'use strict';

angular.module('loginForm')
    .controller('LoginFormController', ["$http", '$state', '$stateParams', "$scope", function ($http, $state, $stateParams, $scope) {

        this.login = () => $http.post("/api/gateway/users/login", {
            email: $scope.login.email,
            password: $scope.login.password,
        })
            .then(n => {
                console.log(n)
            })
            .catch(n => {
                $scope.errorMessages = n.data.message.split`\n`;
                console.log(n);
            })

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);
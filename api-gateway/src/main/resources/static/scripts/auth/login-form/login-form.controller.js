'use strict';

angular.module('loginForm')
    .controller('LoginFormController', ["$http", '$location', "$scope", "authProvider", function ($http, $location, $scope, authProvider) {

        this.login = () => $http.post("/api/gateway/users/login", {
            email: $scope.login.email,
            password: $scope.login.password,
        })
            .then(n => {
                console.log(n)
                authProvider.setUser({
                    username: n.data.username,
                    email: n.data.email,
                    userId: n.data.userId,
                    roles: n.data.roles
                });
                $location.path("/welcome")
            })
            .catch(n => {
                console.log(n)
                $scope.errorMessages = n.data.message.split`\n`;
            })

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);
'use strict';

angular.module('loginForm')
    .controller('LoginFormController', ["$http", '$state', '$window', "$scope", function ($http, $state, $window, $scope) {

        this.login = () => $http.post("/api/gateway/users/login", {
            email: $scope.login.email,
            password: $scope.login.password,
        })
            .then(n => {
                const token = n.headers("Authorization");
                const { data: { username, email } } = n;

                $window.localStorage.setItem("token", token)
                $window.localStorage.setItem("username", username)
                $window.localStorage.setItem("email", email)
            })
            .catch(n => {
                $scope.errorMessages = n.data.message.split`\n`;
                console.log(n);
            })

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);
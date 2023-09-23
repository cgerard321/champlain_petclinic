'use strict';

angular.module('forgotPwdForm')
    .controller('forgotPwdFormController', ["$http", '$location', "$scope", function ($http, $location, $scope) {

        this.forgotPwdPost = () => $http.post("/api/gateway/users/forgot_password", {
            email: $scope.forgotPwdPost.email,
        })
            .then(n => {
               alert("Message sent to " + $scope.forgotPwdPost().email)
            })
            .catch(n => {
                console.log(n)
                $scope.errorMessages = n.data.message.split`\n`;
            })

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);


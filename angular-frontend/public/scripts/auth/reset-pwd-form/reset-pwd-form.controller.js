'use strict';

angular.module('resetPwdForm')
    .controller('resetPwdFormController', ["$http",'$stateParams', '$location', "$scope", function ($http,$stateParams,$location, $scope) {


        this.resetPwdForm = () => $http.post("/api/gateway/users/reset_password", {
            token: $stateParams.token,
            password: $scope.resetPwdForm.password,
        })
            .then(n => {
                alert("Password was reset !")
                $location.path("/login")

            })
            .catch(n => {
                console.log(n)
                try {
                    $scope.errorMessages = n.data.password.split`\n`;
                }
                catch (e) {
                    $scope.errorMessages = n.data.message.split`\n`;
                }
            })

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);


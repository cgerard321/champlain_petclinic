'use strict';

angular.module('verification')
    .controller('VerificationController', ['$http', '$scope', "$location", "$stateParams", function ($http, $scope, $location, $stateParams) {

        this.test = () => $http.get('/api/gateway/verification/' + $stateParams.token)
            .then(() => $location.path("/login"))
            .catch(n => {
                console.log(n);
                $scope.errorMessages = n.data.message.split`\n`;
                console.log("I am in agony")
            });
    }]);
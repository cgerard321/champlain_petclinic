'use strict';

angular.module('verification')
    .controller('VerificationController', ['$http', '$scope', "$location", "$stateParams", function ($http, $scope, $location, $stateParams) {

        this.test = () => $http.get('/api/gateway/users/verification/' + $stateParams.token)
            .then(() => $location.path("/login"))
            .catch(n => {
                $scope.errorMessage = n.data.message;
                console.log(n);
                console.log("I am in agony")
            });
    }]);
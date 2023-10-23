'use strict';

angular.module('userDetails')
    .controller('UserDetailsController', ['$http', '$stateParams', '$location', function ($http, $stateParams, $location) {

        let self = this;
        self.userId = $stateParams.userId;
        self.user = {};

        $http.get('api/gateway/users/' + self.userId)
            .then(function (response) {
                self.user = response.data;
            })
            .catch(function (error) {
                $scope.errorMessages = n.data.message.split`\n`;
            });

        self.goToAdminPanel = function() {
            $location.path("/adminPanel");
        }
    }]);

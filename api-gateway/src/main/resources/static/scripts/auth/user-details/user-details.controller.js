'use strict';

angular.module('userDetails')
    .controller('UserDetailsController', ['$http', '$stateParams', function ($http, $stateParams, $scope) {

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
    }]);


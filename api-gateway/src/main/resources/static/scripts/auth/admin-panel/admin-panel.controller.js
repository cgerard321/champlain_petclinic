'use strict';
angular.module('adminPanel')
    .controller('AdminPanelController', ['$http', '$scope', function ($http, $scope) {

        var self = this;

        $http.get('api/gateway/users').then(function (resp) {
            self.users = resp.data;
        });

        $scope.removeUser = function (userid) {
            $http.delete('api/gateway/users/' + userid).then(function () {
                $http.get('api/gateway/users').then(function (resp) {
                    self.users = resp.data;
                });
            });
        };
    }
    ]);







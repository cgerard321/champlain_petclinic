'use strict';
angular.module('adminPanel')
    .controller('AdminPanelController', ['$http', '$scope', "authProvider", function ($http, $scope, authProvider) {

        var self = this;



        $http.get('api/gateway/users', {
            headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
            .then(function (resp) {
            self.users = resp.data;
        });


        $scope.removeUser = function (userid) {
            $http.delete('api/gateway/users/' + userid, {
                headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
                .then(function () {
                $http.get('api/gateway/users', {
                    headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
                    .then(function (resp) {
                    self.users = resp.data;
                });
            });
        };
    }
    ]);







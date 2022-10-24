'use strict';

angular.module('rolesDetails')
    .controller('RolesDetailsController', ['$http', '$scope', "authProvider", function ($http, $scope, authProvider) {

        var self = this;

        $http.get('api/gateway/admin/roles', {
            headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
            .then(function (resp) {
                self.roles = resp.data;
            });


        this.delete = function (roleId) {
            $http.delete('api/gateway/admin/roles/' + roleId, {
                headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
                .then(function () {
                    $http.get('api/gateway/admin/roles', {
                        headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
                        .then(function (resp) {
                            self.roles = resp.data;
                        });
                });
        };

        // this.delete = id =>
        //     $http.delete(`api/gateway/admin/roles/${id}`, {
        //         headers: {'Authorization': "Bearer " + authProvider.getUser().token}}) &&
        //     this.roles.splice(this.roles.findIndex(n => n.id == id), 1);

        this.add = function() {
            var newRole = {
                name: this.name,
                parent: this.parent,
            };
            $http.post('api/gateway/admin/roles/', {
                headers: {'Authorization': "Bearer " + authProvider.getUser().token}, newRole})
                .then(function () {
                    $http.get('api/gateway/admin/roles', {
                        headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
                        .then(function (resp) {
                            self.roles = resp.data;
                        });
                });
        };
    }]);

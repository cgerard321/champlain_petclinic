'use strict';

angular.module('rolesDetails')
    .controller('RolesDetailsController', ['$http', '$scope', function ($http, $scope) {
        $http.get('api/gateway/admin/roles')
            .then(res => (this.roles = console.log(res) || res.data.content))
            .catch(console.log);

        this.delete = id =>
            $http.delete(`api/gateway/admin/roles/${id}`) &&
            this.roles.splice(this.roles.findIndex(n => n.id == id), 1);

        this.add = () => $http.post('api/gateway/admin/roles/', {
            name: $scope.newRole.name,
            parent: $scope.newRole.parent,
        })
            .then(res => this.roles.push(res.data) && ($scope.newRole.name = ''))
            .catch(console.log);

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);

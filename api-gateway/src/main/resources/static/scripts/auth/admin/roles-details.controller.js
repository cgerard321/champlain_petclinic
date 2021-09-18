'use strict';

angular.module('rolesDetails')
    .controller('RolesDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        $http.get('api/gateway/admin/roles')
            .then(res => (this.roles = console.log(res) || res.data.content))
            .catch(console.log);

        this.delete = id => $http.delete(`api/gateway/admin/roles/${id}`);
    }]);

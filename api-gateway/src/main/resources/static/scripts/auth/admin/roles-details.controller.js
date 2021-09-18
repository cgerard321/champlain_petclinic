'use strict';

angular.module('rolesDetails')
    .controller('RolesDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        $http.get('api/gateway/admin/roles')
            .then(res => (this.roles = res.data))
            .catch(console.log);
    }]);

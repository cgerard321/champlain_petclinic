'use strict';

angular.module('rolesDetails')
    .controller('RolesDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        $http.get('api/gateway/roles')
            .then(res => (this.roles = res.data))
            .catch(console.log);
    }]);

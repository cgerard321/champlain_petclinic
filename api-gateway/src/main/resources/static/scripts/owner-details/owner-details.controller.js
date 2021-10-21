'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;

        $http.get('api/gateway/customer/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
        });

        /*$http.delete('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
        });*/
    }]);

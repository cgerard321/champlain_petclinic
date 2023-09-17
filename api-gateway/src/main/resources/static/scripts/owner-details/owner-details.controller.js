'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;


        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            console.log(resp.data)
            self.owner = resp.data;
        });

    }]);


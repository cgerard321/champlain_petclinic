'use strict';

angular.module('petOwnerDetails')
    .controller('PetOwnerDetailsController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;


        $http.get('api/gateway/owners/' + $stateParams.ownerId + '/pets/' + 5).then(function (resp) {
            console.log("Owner id is " + $stateParams.ownerId)
            console.log(resp.data)
            self.owner = resp.data;
        });

    }]);


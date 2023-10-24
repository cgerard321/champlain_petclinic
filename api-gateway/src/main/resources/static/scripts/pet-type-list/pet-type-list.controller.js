'use strict';

angular.module('petTypeList')
    .controller('PetTypeListController', ['$http', '$stateParams', '$scope', '$state', function ($http, $stateParams, $scope, $state) {
        var self = this;
        self.petTypes = {};
        // Initialize as an empty array

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.petTypes = resp.data;
            console.log(self.petTypes);

        });
    }]);



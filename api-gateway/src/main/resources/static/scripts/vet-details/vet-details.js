'use strict';

angular.module('vetDetails', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('vetDetails', {
                parent: 'app',
                url: '/vets/details/:vetId',
                template: '<vet-details></vet-details>'
            })
            .state('disableVet', {
                parent: 'app',
                url: '/vets/details/:vetId/disableVet',
                template: '<vet-details></vet-details>'
            })
            .state('enableVet', {
                parent: 'app',
                url: '/vets/details/:vetId/enableVet',
                template: '<vet-details></vet-details>'
            })
            .state('deleteVet', {
                parent: 'app',
                url: '/vets/details/:vetId/deleteVet',
                template: '<vet-details></vet-details>'
            })
    }]);
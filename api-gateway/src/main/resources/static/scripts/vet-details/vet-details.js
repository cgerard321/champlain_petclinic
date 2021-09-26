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
                url: '/vets/:vetId/disable',
                template: '<vet-details></vet-details>'
            })
    }]);
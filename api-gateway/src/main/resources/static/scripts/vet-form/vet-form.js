'use strict';

angular.module('vetForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('vetNew', {
                parent: 'app',
                url: '/vets/new',
                template: '<vet-form></vet-form>'
            })
            .state('vetEdit', {
                parent: 'app',
                url: '/vets/:vetId/edit',
                template: '<vet-form></vet-form>'
            })
    }]);

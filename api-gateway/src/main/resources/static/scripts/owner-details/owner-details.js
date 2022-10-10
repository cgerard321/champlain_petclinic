'use strict';

angular.module('ownerDetails', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('ownerDetails', {
                parent: 'app',
                url: '/owners/details/:ownerId',
                params: {ownerId: null},
                template: '<owner-details></owner-details>'
            })
        $stateProvider
            .state('photoDetails', {
                parent: 'app',
                url: '/owners/photo/:ownerId',
                params: {ownerId: null},
                template: '<photo-details></photo-details>'
            })
    }]);
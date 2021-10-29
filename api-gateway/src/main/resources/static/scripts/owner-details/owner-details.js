'use strict';

angular.module('ownerDetails', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('owner.details', {
                parent: 'app',
                url: '/owners/details/:ownerId',
                template: '<owner-details></owner-details>'
            })

    }]);
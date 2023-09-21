'use strict';

angular.module('visits', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('visitsNew', {
                parent: 'app',
                url: '/owners/:ownerId/pets/:petId/visits',
                template: '<visits></visits>'
            })
    }]);


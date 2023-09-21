'use strict';

angular.module('visits', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('visitsNew', {
                parent: 'app',
                url: '/visits/new',
                template: '<visits></visits>'
            })
<<<<<<< HEAD
    }]);
=======
    }]);

//url: '/owners/:ownerId/pets/:petId/visits',
>>>>>>> 83dda6a9 (Double Check)

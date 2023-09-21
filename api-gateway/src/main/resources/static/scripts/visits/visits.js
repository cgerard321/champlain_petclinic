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

<<<<<<< HEAD
//url: '/owners/:ownerId/pets/:petId/visits',
//                url: '/visits/new',
=======
//url: '/owners/:ownerId/pets/:petId/visits',
>>>>>>> 67834aa0 (feat/(VIST-CPC-762) User can now navigate to create new visit page (#415))
>>>>>>> 75d3a380 (feat/(VIST-CPC-762) User can now navigate to create new visit page (#415))

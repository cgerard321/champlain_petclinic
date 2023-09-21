'use strict';

angular.module('visits', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('visitsNew', {
                parent: 'app',
                url: '/visits/new',
                template: '<visits></visits>'
            })
    }]);
<<<<<<< HEAD

=======
>>>>>>> 791dfa3d (Fixed rouuting issue with create visit button)
//url: '/owners/:ownerId/pets/:petId/visits',
'use strict';

angular.module('visits', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('visitsNew', {
                parent: 'app',
                url: '/visits/new',
                template: '<visits></visits>'
<<<<<<< HEAD
            })
<<<<<<< HEAD
    }]);
=======
<<<<<<< HEAD
    }]);
<<<<<<< HEAD
<<<<<<< HEAD

<<<<<<< HEAD
<<<<<<< HEAD
//url: '/owners/:ownerId/pets/:petId/visits',
//                url: '/visits/new',
=======
//url: '/owners/:ownerId/pets/:petId/visits',
>>>>>>> 67834aa0 (feat/(VIST-CPC-762) User can now navigate to create new visit page (#415))
=======
=======
>>>>>>> 791dfa3d (Fixed rouuting issue with create visit button)
//url: '/owners/:ownerId/pets/:petId/visits',
>>>>>>> 0d5c194a (Fixed rouuting issue with create visit button)
=======
    }]);
>>>>>>> 9b25b300 (Double Check)
=======
=======
    }]);

//url: '/owners/:ownerId/pets/:petId/visits',
>>>>>>> 83dda6a9 (Double Check)
>>>>>>> 1a7bbf9f (Double Check)
=======
            })}
    ]);
>>>>>>> 26c4ae85 (Working on Post)
>>>>>>> e7d8f063 (Working on Post)

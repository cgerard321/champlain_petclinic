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

>>>>>>> d158d8be (Working on parse date)

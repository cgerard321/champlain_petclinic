'use strict';

// Configure an existing module from the component script
angular.module('billHistory', ['ui.router'])
    // Call the config method of the module to alter an argument called state provider as a method
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            // Access the state property to use a template called bills
            .state('bills', {
                parent: 'app',
                url: '/bills',
                template: '<bill-history></bill-history>'
            })
    }]);
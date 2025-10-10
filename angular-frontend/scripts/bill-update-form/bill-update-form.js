'use strict';


angular.module('billUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('billEdit', {
                parent: 'app',
                url: '/bills/:billId/:method',
                template: '<bill-update-form></bill-update-form>'
            })

    }]);

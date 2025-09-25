'use strict';

angular.module('receptionistForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('receptionistForm', {
                parent: 'app',
                url: 'users/receptionist',
                template: '<receptionist-form></receptionist-form>'
            })
    }]);
'use strict';

angular.module('inventoriesUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider

            .state('updateInventory', {
                    parent: 'app',
                    url: '/inventories/:inventoryId/edit',
                    template: '<inventories-update-form></inventories-update-form>'
                }
            )

    }]);
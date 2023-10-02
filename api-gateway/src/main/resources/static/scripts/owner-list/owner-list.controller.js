'use strict';

angular.module('ownerList')
    .controller('OwnerListController', ['$http', '$stateParams', '$scope', '$state', function ($http, $stateParams, $scope, $state) {
        var vm = this;
        vm.currentPage = $stateParams.page || 0; // Initialize the current page
        vm.pageSize = $stateParams.size || 5; // Number of items per page
        vm.currentPageOnSite = parseInt(vm.currentPage) + 1;

        function loadTotalItem() {
            return $http.get('api/gateway/owners-count')
                .then(function (resp) {
                    console.log(resp);
                    return resp.data;
                });
        }

        // Calculate total pages based on total items and page size
        loadTotalItem().then(function (totalItems) {
            vm.totalItems = totalItems;
            vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
        });

        // Function to load data based on page
        function loadData() {
            // Update the URL with the current 'page' and 'size' values
            $state.transitionTo('owners', { page: vm.currentPage, size: vm.pageSize }, { notify: false });
            // Rest of your data loading logic
            $http.get('api/gateway/owners-pagination?page=' + vm.currentPage + '&size=' + vm.pageSize)
                .then(function (resp) {
                    vm.owners = resp.data;
                    console.log(resp);
                });

        }

        // Initial data load
        loadData();

        vm.goNextPage = function () {
            if (parseInt(vm.currentPage) + 1 < vm.totalPages) {

                var currentPageInt = parseInt(vm.currentPage) + 1
                vm.currentPage = currentPageInt.toString();
                updateCurrentPageOnSite();
                // Refresh the owner's list with the new page size
                loadData();
            }
        }

        vm.goPreviousPage = function () {
            if (vm.currentPage - 1 >= 0) {
                var currentPageInt = parseInt(vm.currentPage) - 1
                vm.currentPage = currentPageInt.toString();
                updateCurrentPageOnSite();
                // Refresh the owner's list with the new page size
                loadData();
            }
        }

        function updateCurrentPageOnSite() {
            vm.currentPageOnSite = parseInt(vm.currentPage) + 1;
            console.log(vm.currentPage);
        }
    }]);


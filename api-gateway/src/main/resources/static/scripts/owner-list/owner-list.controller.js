'use strict';

angular.module('ownerList')
    .controller('OwnerListController', ['$http', '$stateParams', '$scope', '$state', function ($http, $stateParams, $scope, $state) {
        var vm = this;
        
        vm.currentPage = $stateParams.page || 0;
        vm.pageSize = $stateParams.size || 5;
        
        vm.currentPageOnSite = parseInt(vm.currentPage) + 1;
        
        vm.ownerId = null;
        vm.firstName = null;
        vm.lastName = null;
        vm.phoneNumber = null;
        vm.city = null;
        vm.selectedSize = null;
        
        vm.searchActive = false;
        
        vm.baseURL = "api/gateway/owners/owners-pagination";
        vm.baseURLforTotalNumberOfOwnersByFiltering = "api/gateway/owners/owners-filtered-count";

        vm.notification = {
            show: false,
            message: '',
            type: 'info'
        };

        vm.showNotification = function(message, type) {
            vm.notification.show = true;
            vm.notification.message = message;
            vm.notification.type = type;
            
            setTimeout(function() {
                vm.hideNotification();
            }, 5000);
        };

        vm.hideNotification = function() {
            vm.notification.show = false;
            vm.notification.message = '';
        };

        loadDefaultData();

        function loadTotalItemForDefaultData() {

            return $http.get('api/gateway/owners/owners-count')
                .then(function (resp) {
                    console.log(resp);
                    return resp.data;
                });
        }

        function loadTotalItemForSearchData(searchURL) {
            return $http.get(searchURL)
                .then(function (resp) {
                    console.log(resp);
                    return resp.data;
                });
        }

        function loadDefaultData() {
            if(!vm.searchActive){
                loadTotalItemForDefaultData().then(function (totalItems) {
                    vm.totalItems = totalItems;
                    vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
                    $http.get('api/gateway/owners/owners-pagination?page=' + vm.currentPage + '&size=' + vm.pageSize)
                        .then(function (resp) {
                            vm.owners = resp.data;
                            console.log(resp);
                        });

                    updateCurrentPageOnSite();
                });
            }
        }

        vm.searchOwnersByPaginationAndFilters = function (currentPage = 0, prevOrNextPressed = false) {
            vm.selectedSize = document.getElementById("sizeInput").value;

            if(!prevOrNextPressed) {
                vm.ownerId = document.getElementById("ownerIdInput").value;
                vm.firstName = document.getElementById("firstNameInput").value;
                vm.lastName = document.getElementById("lastNameInput").value;
                vm.phoneNumber = document.getElementById("phoneNumberInput").value;
                vm.city = document.getElementById("cityInput").value;

                if (checkIfAllInputFieldsAreEmptyOrNull(vm.ownerId, vm.firstName, vm.lastName, vm.phoneNumber, vm.city, vm.selectedSize)) {
                    vm.showNotification("Oops! It seems like you forgot to enter any filter criteria. Please provide some filter input to continue.", 'warning');
                    return;
                }
            }

            vm.searchActive = true;

            var searchURL = vm.baseURL + "?page=" + currentPage.toString();
            var loadTotalNumberOfDataURL  = vm.baseURLforTotalNumberOfOwnersByFiltering + "?";

            if (vm.selectedSize) {
                searchURL += "&size=" + vm.selectedSize;
                vm.pageSize = vm.selectedSize
            } else {
                searchURL += "&size=" + vm.pageSize;
            }

            if (vm.ownerId) {
                searchURL += "&ownerId=" + vm.ownerId;
                loadTotalNumberOfDataURL += "&ownerId=" + vm.ownerId;
            }

            if (vm.firstName) {
                searchURL += "&firstName=" + vm.firstName;
                loadTotalNumberOfDataURL += "&firstName=" + vm.firstName;
            }

            if (vm.lastName) {
                searchURL += "&lastName=" + vm.lastName;
                loadTotalNumberOfDataURL += "&lastName=" + vm.lastName;
            }

            if (vm.phoneNumber) {
                searchURL += "&phoneNumber=" + vm.phoneNumber;
                loadTotalNumberOfDataURL += "&phoneNumber=" + vm.phoneNumber;
            }

            if (vm.city) {
                searchURL += "&city=" + vm.city;
                loadTotalNumberOfDataURL += "&city=" + vm.city;
            }

            console.log(searchURL);

            loadTotalItemForSearchData(loadTotalNumberOfDataURL).then(function (totalItems) {
                vm.totalItems = totalItems;
                vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
            });

            $http.get(searchURL)
                .then(function (resp) {
                    vm.owners = resp.data;
                    console.log(resp);
                });

            updateCurrentPageOnSite();
        }

        function checkIfAllInputFieldsAreEmptyOrNull(ownerId, firstName, lastName, phoneNumber, city, selectedSize) {
            return (
                (ownerId === null || ownerId === "") &&
                (firstName === null || firstName === "") &&
                (lastName === null || lastName === "") &&
                (phoneNumber === null || phoneNumber === "") &&
                (city === null || city === "") &&
                (selectedSize === null || selectedSize === "")
            );
        }

        vm.clearInputAndResetDefaultData = function() {

            var ownerId = document.getElementById("ownerIdInput");
            var firstNameInput = document.getElementById("firstNameInput");
            var lastNameInput = document.getElementById("lastNameInput");
            var phoneNumberInput = document.getElementById("phoneNumberInput");
            var cityInput = document.getElementById("cityInput");
            var sizeInput = document.getElementById("sizeInput");

            firstNameInput.value = "";
            lastNameInput.value = "";
            ownerId.value = "";
            phoneNumberInput.value = "";
            cityInput.value = "";
            sizeInput.selectedIndex = 0;

            vm.currentPage = 0;
            vm.pageSize = 5;

            vm.ownerId = null;
            vm.firstName = null;
            vm.lastName = null;
            vm.phoneNumber = null;
            vm.city = null;
            vm.selectedSize = null;

            vm.searchActive = false;

            loadDefaultData()

            vm.showNotification("All filters have been cleared successfully.", 'success')
        }

        vm.goNextPage = function () {
            if (parseInt(vm.currentPage) + 1 < vm.totalPages) {

                var currentPageInt = parseInt(vm.currentPage) + 1
                vm.currentPage = currentPageInt.toString();
                updateCurrentPageOnSite();

                if(vm.searchActive){
                    vm.searchOwnersByPaginationAndFilters(currentPageInt,true)
                } else {
                    loadDefaultData();
                }

            }
        }

        vm.goPreviousPage = function () {
            if (vm.currentPage - 1 >= 0) {
                var currentPageInt = parseInt(vm.currentPage) - 1
                vm.currentPage = currentPageInt.toString();
                updateCurrentPageOnSite();

                if(vm.searchActive){
                    vm.searchOwnersByPaginationAndFilters(currentPageInt,true)
                } else {
                    loadDefaultData();
                }
            }
        }

        function updateCurrentPageOnSite() {
            vm.currentPageOnSite = parseInt(vm.currentPage) + 1;
            console.log(vm.currentPage);
        }

        function StringBuilder() {
            this.strings = [];

            this.append = function (str) {
                this.strings.push(str);
            };

            this.toString = function () {
                return this.strings.join("");
            };

            this.clear = function () {
                this.strings = [];
            };
        }

    }]);
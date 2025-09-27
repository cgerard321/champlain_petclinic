'use strict';

angular.module('petTypeList')
    .controller('PetTypeListController', ['$http', '$stateParams', '$scope', '$state', function ($http, $stateParams, $scope, $state) {
        var vm = this;
        /*------------------------------------------------------------*/
        vm.currentPage = $stateParams.page || 0;
        vm.pageSize = $stateParams.size || 5;
        /*------------------------------------------------------------*/
        vm.currentPageOnSite = parseInt(vm.currentPage) + 1;
        /*------------------------------------------------------------*/
        vm.petTypeId = null;
        vm.name = null;
        vm.description = null;
        vm.selectedSize = null;
        /*------------------------------------------------------------*/
        vm.searchActive = false;
        vm.allPetTypes = []; // Store all pet types for filtering
        /*------------------------------------------------------------*/

        // Initial data load
        loadDefaultData();

        function loadDefaultData() {
            $http.get('api/gateway/owners/petTypes').then(function (resp) {
                vm.allPetTypes = resp.data;
                vm.petTypes = resp.data;
                console.log('Pet types loaded:', resp.data);
                
                // Calculate pagination
                vm.totalItems = vm.allPetTypes.length;
                vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
                
                // Apply initial pagination
                applyPagination();
                updateCurrentPageOnSite();
            });
        }

        function applyPagination() {
            var startIndex = vm.currentPage * parseInt(vm.pageSize);
            var endIndex = startIndex + parseInt(vm.pageSize);
            vm.petTypes = vm.allPetTypes.slice(startIndex, endIndex);
        }

        vm.searchPetTypesByPaginationAndFilters = function (currentPage = 0, prevOrNextPressed = false) {
            // Collect search parameters
            vm.selectedSize = document.getElementById("sizeInput").value;

            if(!prevOrNextPressed) {
                vm.petTypeId = document.getElementById("petTypeIdInput").value;
                vm.name = document.getElementById("nameInput").value;
                vm.description = document.getElementById("descriptionInput").value;

                // Check if all input fields are empty
                if (checkIfAllInputFieldsAreEmptyOrNull(vm.petTypeId, vm.name, vm.description, vm.selectedSize)) {
                    alert("Oops! It seems like you forgot to enter any filter criteria. Please provide some filter input to continue.");
                    return;
                }
            }

            vm.searchActive = true;

            // Apply client-side filtering
            var filteredPetTypes = vm.allPetTypes;

            // Apply filters
            if (vm.petTypeId) {
                filteredPetTypes = filteredPetTypes.filter(function(petType) {
                    return petType.petTypeId && petType.petTypeId.toString().includes(vm.petTypeId);
                });
            }

            if (vm.name) {
                filteredPetTypes = filteredPetTypes.filter(function(petType) {
                    return petType.name && petType.name.toLowerCase().includes(vm.name.toLowerCase());
                });
            }

            if (vm.description) {
                filteredPetTypes = filteredPetTypes.filter(function(petType) {
                    return petType.petTypeDescription && petType.petTypeDescription.toLowerCase().includes(vm.description.toLowerCase());
                });
            }

            // Update page size if changed
            if (vm.selectedSize) {
                vm.pageSize = vm.selectedSize;
            }

            // Reset to first page if not navigating
            if (!prevOrNextPressed) {
                vm.currentPage = 0;
            }

            // Apply pagination
            vm.totalItems = filteredPetTypes.length;
            vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
            
            var startIndex = vm.currentPage * parseInt(vm.pageSize);
            var endIndex = startIndex + parseInt(vm.pageSize);
            vm.petTypes = filteredPetTypes.slice(startIndex, endIndex);
            
            console.log('Filtered pet types:', vm.petTypes);
            updateCurrentPageOnSite();
        }

        function checkIfAllInputFieldsAreEmptyOrNull(petTypeId, name, description, selectedSize) {
            return (
                (petTypeId === null || petTypeId === "") &&
                (name === null || name === "") &&
                (description === null || description === "") &&
                (selectedSize === null || selectedSize === "")
            );
        }

        vm.clearInputAndResetDefaultData = function() {
            var petTypeIdInput = document.getElementById("petTypeIdInput");
            var nameInput = document.getElementById("nameInput");
            var descriptionInput = document.getElementById("descriptionInput");
            var sizeInput = document.getElementById("sizeInput");

            if (petTypeIdInput) petTypeIdInput.value = "";
            if (nameInput) nameInput.value = "";
            if (descriptionInput) descriptionInput.value = "";
            if (sizeInput) sizeInput.selectedIndex = 0;

            vm.currentPage = 0;
            vm.pageSize = 5;

            vm.petTypeId = null;
            vm.name = null;
            vm.description = null;
            vm.selectedSize = null;

            vm.searchActive = false;

            // Reset to show all pet types
            vm.petTypes = vm.allPetTypes;
            vm.totalItems = vm.allPetTypes.length;
            vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
            applyPagination();
            updateCurrentPageOnSite();

            alert("All filters have been cleared successfully.");
        }

        vm.goNextPage = function () {
            if (parseInt(vm.currentPage) + 1 < vm.totalPages) {
                var currentPageInt = parseInt(vm.currentPage) + 1
                vm.currentPage = currentPageInt.toString();
                updateCurrentPageOnSite();

                if(vm.searchActive){
                    vm.searchPetTypesByPaginationAndFilters(currentPageInt, true)
                } else {
                    applyPagination();
                }
            }
        }

        vm.goPreviousPage = function () {
            if (vm.currentPage - 1 >= 0) {
                var currentPageInt = parseInt(vm.currentPage) - 1
                vm.currentPage = currentPageInt.toString();
                updateCurrentPageOnSite();

                if(vm.searchActive){
                    vm.searchPetTypesByPaginationAndFilters(currentPageInt, true)
                } else {
                    applyPagination();
                }
            }
        }

        function updateCurrentPageOnSite() {
            vm.currentPageOnSite = parseInt(vm.currentPage) + 1;
            console.log('Current page:', vm.currentPage);
        }

        vm.deletePetType = function(petTypeId) {
            let isConfirmed = confirm('Are you sure you want to delete this pet type?');
            if (isConfirmed) {
                $http.delete('api/gateway/owners/petTypes/' + petTypeId)
                    .then(function(response) {
                        console.log('Pet type deleted successfully');
                        alert('Pet type deleted successfully!');
                
                        // Refresh the pet types list
                        loadDefaultData();
                    }, function(error) {
                        console.error('Error deleting pet type:', error);
                        alert('Failed to delete pet type: ' + (error.data.message || 'Unknown error'));
                    });
            }
        }
    }]);
    
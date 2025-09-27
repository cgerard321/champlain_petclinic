'use strict';

angular.module('billHistory')
    .controller('BillHistoryController', ['$http','$stateParams', '$scope', '$state',
        function ($http,$stateParams, $scope, $state) {
            var vm = this;

            vm.toolbar = { open: null };
            vm.togglePanel = function(which){
                vm.toolbar.open = (vm.toolbar.open === which) ? null : which;
                if (vm.toolbar.open === 'filter') {
                    vm.tempStatus     = vm.activeStatus;
                    vm.tempVisitType  = vm.activeVisitType;
                    vm.tempYear       = vm.filterYear;
                }
            };

            (function buildYears(){
                vm.availableYears = [];
                var now = new Date().getFullYear();
                for (var y = now; y >= now - 14; y--) vm.availableYears.push(y);
            })();

            vm.activeStatus = '';
            vm.activeVisitType = '';
            vm.filterYear = '';

            vm.tempStatus = '';
            vm.tempVisitType = '';
            vm.tempYear = '';

            vm.applyFilter = function(){
                vm.activeStatus    = (vm.tempStatus || '').toLowerCase();
                vm.activeVisitType = (vm.tempVisitType || '').toLowerCase();
                vm.filterYear      = vm.tempYear || '';
                vm.toolbar.open = null;
            };
            vm.resetFilter = function(){
                vm.tempStatus = vm.tempVisitType = '';
                vm.tempYear = '';
                vm.activeStatus = vm.activeVisitType = '';
                vm.filterYear = '';
            }

            vm.getRows = function(){
                switch (vm.activeStatus) {
                    case 'paid':    return vm.paidBills;
                    case 'unpaid':  return vm.unpaidBills;
                    case 'overdue': return vm.overdueBills;
                    default:        return vm.billHistory;
                }
            };

            vm.visitTypePredicate = function(bill){
                if (!vm.activeVisitType) return true;
                var v = (bill && bill.visitType) ? String(bill.visitType).toLowerCase() : '';
                return v === vm.activeVisitType;
            };

            vm.billHistory = [];
            vm.paidBills = [];
            vm.unpaidBills = [];
            vm.overdueBills = [];

            vm.currentPage = $stateParams.page || 0;
            vm.pageSize = $stateParams.size || 10;
            vm.currentPageOnSite = parseInt(vm.currentPage) + 1;
            vm.page = vm.currentPageOnSite;
            vm.totalPages = 1;

            vm.baseURL = "api/gateway/bills/bills-pagination";
            vm.baseURLforTotalNumberOfBillsByFiltering = "api/gateway/bills/bills-filtered-count";
            vm.searchActive = false;

            loadDefaultData();

            function loadTotalItemForDefaultData() {
                return $http.get('api/gateway/bills/bills-count')
                    .then(function (resp) { return resp.data; });
            }
            function loadDefaultData() {
                if (!vm.searchActive){
                    loadTotalItemForDefaultData().then(function (totalItems) {
                        vm.totalItems = totalItems;
                        vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
                        $http.get(vm.baseURL + '?page=' + vm.currentPage + '&size=' + vm.pageSize)
                            .then(function (resp) { vm.billHistory = resp.data; });
                        updateCurrentPageOnSite();
                    });
                }
            }
            vm.goNextPage = function () {
                if (parseInt(vm.currentPage) + 1 < vm.totalPages) {
                    vm.currentPage = (parseInt(vm.currentPage) + 1).toString();
                    updateCurrentPageOnSite(); loadDefaultData();
                }
            };
            vm.goPreviousPage = function () {
                if (vm.currentPage - 1 >= 0) {
                    vm.currentPage = (parseInt(vm.currentPage) - 1).toString();
                    updateCurrentPageOnSite(); loadDefaultData();
                }
            };
            function updateCurrentPageOnSite() {
                vm.currentPageOnSite = parseInt(vm.currentPage) + 1;
                vm.page = vm.currentPageOnSite;
            }

            var eventSource = new EventSource("api/gateway/bills");
            eventSource.addEventListener('message', function (event) {
                $scope.$apply(function () { vm.billHistory.push(JSON.parse(event.data)); });
            });
            eventSource.onerror = function(){ if (eventSource.readyState === 0) eventSource.close(); };

            var eventSourcePaid = new EventSource("api/gateway/bills/paid");
            eventSourcePaid.addEventListener('message', function (event) {
                $scope.$apply(function () { vm.paidBills.push(JSON.parse(event.data)); });
            });
            eventSourcePaid.onerror = function(){ if (eventSourcePaid.readyState === 0) eventSourcePaid.close(); };

            var eventSourceUnpaid = new EventSource("api/gateway/bills/unpaid");
            eventSourceUnpaid.addEventListener('message', function (event) {
                $scope.$apply(function () { vm.unpaidBills.push(JSON.parse(event.data)); });
            });
            eventSourceUnpaid.onerror = function(){ if (eventSourceUnpaid.readyState === 0) eventSourceUnpaid.close(); };

            var eventSourceOverdue = new EventSource("api/gateway/bills/overdue");
            eventSourceOverdue.addEventListener('message', function (event) {
                $scope.$apply(function () { vm.overdueBills.push(JSON.parse(event.data)); });
            });
            eventSourceOverdue.onerror = function(){ if (eventSourceOverdue.readyState === 0) eventSourceOverdue.close(); };

            vm.owners = [
                { ownerId: '1', firstName: 'George', lastName: 'Franklin' },
                { ownerId: '2', firstName: 'Betty', lastName: 'Davis' },
                { ownerId: '3', firstName: 'Eduardo', lastName: 'Rodriguez' },
                { ownerId: '4', firstName: 'Harold', lastName: 'Davis' },
                { ownerId: '5', firstName: 'Peter', lastName: 'McTavish' },
                { ownerId: '6', firstName: 'Jean', lastName: 'Coleman' },
                { ownerId: '7', firstName: 'Jeff', lastName: 'Black' },
                { ownerId: '8', firstName: 'Maria', lastName: 'Escobito' },
                { ownerId: '9', firstName: 'David', lastName: 'Schroeder' },
                { ownerId: '10', firstName: 'Carlos', lastName: 'Esteban' }
            ];
            vm.ownersUUID = [
                { ownerId: 'f470653d-05c5-4c45-b7a0-7d70f003d2ac', firstName: 'George', lastName: 'Franklin' },
                { ownerId: 'e6c7398e-8ac4-4e10-9ee0-03ef33f0361a', firstName: 'Betty', lastName: 'Davis' },
                { ownerId: '3f59dca2-903e-495c-90c3-7f4d01f3a2aa', firstName: 'Eduardo', lastName: 'Rodriguez' },
                { ownerId: 'a6e0e5b0-5f60-45f0-8ac7-becd8b330486', firstName: 'Harold', lastName: 'Davis' },
                { ownerId: 'c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2', firstName: 'Peter', lastName: 'McTavish' },
                { ownerId: 'b3d09eab-4085-4b2d-a121-78a0a2f9e501', firstName: 'Jean', lastName: 'Coleman' },
                { ownerId: '5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd', firstName: 'Jeff', lastName: 'Black' },
                { ownerId: '48f9945a-4ee0-4b0b-9b44-3da829a0f0f7', firstName: 'Maria', lastName: 'Escobito' },
                { ownerId: '9f6accd1-e943-4322-932e-199d93824317', firstName: 'David', lastName: 'Schroeder' },
                { ownerId: '7c0d42c2-0c2d-41ce-bd9c-6ca67478956f', firstName: 'Carlos', lastName: 'Esteban' }
            ];

            $http.get('api/gateway/vets').then(function (resp) { vm.vetList = resp.data; });
            $http.get('api/gateway/owners').then(function (owners) { vm.ownersInfoArray = owners.data; });

            $scope.getOwnerUUIDByCustomerId = function(customerId) {
                let foundOwner;
                vm.owners.forEach(function(o){ if (o.ownerId === String(customerId)) foundOwner = o; });
                vm.ownersUUID.forEach(function(o){ if (o.ownerId === String(customerId)) foundOwner = o; });
                if (foundOwner && vm.ownersInfoArray) {
                    const match = vm.ownersInfoArray.find(function(o){
                        return o.firstName === foundOwner.firstName && o.lastName === foundOwner.lastName;
                    });
                    if (match) return match.ownerId;
                }
                return customerId || 'Unknown Owner';
            };

            $scope.getVetDetails = function(vetId) {
                if (!vm.vetList) return 'Unknown Vet';
                const viaBillId = vm.vetList.find(function(v){ return v.vetBillId === vetId; });
                const viaVetId  = vm.vetList.find(function(v){ return v.vetId === vetId; });
                const v = viaBillId || viaVetId;
                return v ? (v.firstName + ' ' + v.lastName) : 'Unknown Vet';
            };

            vm.customerNameMap = {}; vm.customerNameMap2 = {};
            vm.owners.forEach(function (c) { vm.customerNameMap[c.ownerId] = c.firstName + ' ' + c.lastName; });
            vm.ownersUUID.forEach(function (c) { vm.customerNameMap2[c.ownerId] = c.firstName + ' ' + c.lastName; });

            $scope.getCustomerDetails = function(customerId) {
                return vm.customerNameMap[customerId] || vm.customerNameMap2[customerId] || 'Unknown Customer';
            };

            $scope.deleteAllBills = function () {
                var ok = confirm('Are you sure you want to delete all the bills in the bill history');
                if (!ok) return;
                $http.delete('api/gateway/bills')
                    .then(function () {
                        alert("bill history was deleted successfully");
                        return $http.get('api/gateway/bills');
                    })
                    .then(function (resp) { vm.billHistory = resp.data; })
                    .catch(function (err){ console.log(err); });
            };

            $scope.deleteBill = function (billId) {
                var ok = confirm('You are about to delete billId ' + billId + '. Is it what you want to do ? ');
                if (!ok) return;
                $http.delete('api/gateway/bills/' + billId)
                    .then(function () {
                        alert(billId + " bill was deleted successfully");
                        return $http.get('api/gateway/bills');
                    })
                    .then(function (resp) { vm.billHistory = resp.data; })
                    .catch(function (err){ console.log(err); });
            };
        }
    ]);

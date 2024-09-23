"use strict";

angular.module("billHistory").controller("BillHistoryController", [
  "$http",
  "$stateParams",
  "$scope",
  "$state",
  function ($http, $stateParams, $scope, $state) {
    var vm = this;
    vm.billHistory = [];
    vm.paidBills = [];
    vm.unpaidBills = [];
    vm.overdueBills = [];

    // Pagination properties
    vm.currentPage = $stateParams.page || 0;
    vm.pageSize = $stateParams.size || 10; // Number of items per page
    vm.currentPageOnSite = parseInt(vm.currentPage) + 1;

    vm.billId = null;
    vm.customerId = null;
    vm.ownerFirstName = null;
    vm.ownerLastName = null;
    vm.visitType = null;
    vm.vetId = null;
    vm.vetFirstName = null;
    vm.vetLastName = null;

    vm.selectedSize = null;
    vm.searchActive = false;

    vm.baseURL = "api/gateway/bills/bills-pagination";
    vm.baseURLforTotalNumberOfBillsByFiltering =
      "api/gateway/bills/bills-filtered-count";

    loadDefaultData();

    // Pageable pageable,
    // String billId,
    // String customerId,
    // String ownerFirstName,
    // String ownerLastName,
    // String visitType,
    // String vetId,
    // String vetFirstName,
    // String vetLastName

    function loadTotalItemForDefaultData() {
      return $http.get("api/gateway/bills/bills-count").then(function (resp) {
        console.log(resp);
        return resp.data;
      });
    }

    function loadTotalItemForSearchData(searchURL) {
      return $http.get(searchURL).then(function (resp) {
        console.log(resp);
        return resp.data;
      });
    }

    function loadDefaultData() {
      // $state.transitionTo('bills', { page: vm.currentPage, size: vm.pageSize}, { notify: false });

      if (!vm.searchActive) {
        loadTotalItemForDefaultData().then(function (totalItems) {
          vm.totalItems = totalItems;
          vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
          $http
            .get(
              "api/gateway/bills/bills-pagination?page=" +
                vm.currentPage +
                "&size=" +
                vm.pageSize,
            )
            .then(function (resp) {
              vm.billHistory = resp.data;
              console.log(resp);
            });

          updateCurrentPageOnSite();
        });
      }
    }

    vm.searchBillsByPaginationAndFilters = function (
      currentPage = 0,
      prevOrNextPressed = false,
    ) {
      // Collect search parameters
      vm.selectedSize = document.getElementById("sizeInput").value;

      if (!prevOrNextPressed) {
        vm.billId = document.getElementById("billIdInput").value;
        vm.customerId = document.getElementById("customerIdInput").value;
        vm.ownerFirstName = document.getElementById(
          "ownerFirstNameInput",
        ).value;
        vm.ownerLastName = document.getElementById("ownerLastNameInput").value;
        vm.visitType = document.getElementById("visitTypeInput").value;
        vm.vetId = document.getElementById("vetIdInput").value;
        vm.vetFirstName = document.getElementById("vetFirstNameInput").value;
        vm.vetLastName = document.getElementById("vetLastNameInput").value;

        // Check if all input fields are empty
        // if (checkIfAllInputFieldsAreEmptyOrNull(vm.billId, vm.customerId, vm.ownerFirstName, vm.ownerLastName,
        //     vm.visitType, vm.vetId, vm.vetFirstName, vm.vetLastName, vm.selectedSize)) {
        //     alert("Oops! It seems like you forgot to enter any filter criteria. Please provide some filter input to continue.");
        //     return;
        // }
      }

      vm.searchActive = true;

      // Construct the search URL
      var searchURL = vm.baseURL + "?page=" + currentPage.toString();
      var loadTotalNumberOfDataURL =
        vm.baseURLforTotalNumberOfBillsByFiltering + "?";

      if (vm.selectedSize) {
        searchURL += "&size=" + vm.selectedSize;
        vm.pageSize = vm.selectedSize;
      } else {
        searchURL += "&size=" + vm.pageSize;
      }

      if (vm.billId) {
        searchURL += "&billId=" + vm.billId;
        loadTotalNumberOfDataURL += "&billId=" + vm.billId;
      }

      if (vm.customerId) {
        searchURL += "&customerId=" + vm.customerId;
        loadTotalNumberOfDataURL += "&customerId=" + vm.customerId;
      }

      if (vm.ownerFirstName) {
        searchURL += "&ownerFirstName=" + vm.ownerFirstName;
        loadTotalNumberOfDataURL += "&ownerFirstName=" + vm.ownerFirstName;
      }

      if (vm.ownerLastName) {
        searchURL += "&ownerLastName=" + vm.ownerLastName;
        loadTotalNumberOfDataURL += "&ownerLastName=" + vm.ownerLastName;
      }

      if (vm.visitType) {
        searchURL += "&visitType=" + vm.visitType;
        loadTotalNumberOfDataURL += "&visitType=" + vm.visitType;
      }

      if (vm.vetId) {
        searchURL += "&vetId=" + vm.vetId;
        loadTotalNumberOfDataURL += "&vetId=" + vm.vetId;
      }

      if (vm.vetFirstName) {
        searchURL += "&vetFirstName=" + vm.vetFirstName;
        loadTotalNumberOfDataURL += "&vetFirstName=" + vm.vetFirstName;
      }

      if (vm.vetLastName) {
        searchURL += "&vetLastName=" + vm.vetLastName;
        loadTotalNumberOfDataURL += "&vetLastName=" + vm.vetLastName;
      }

      console.log(searchURL);

      loadTotalItemForSearchData(loadTotalNumberOfDataURL).then(
        function (totalItems) {
          vm.totalItems = totalItems;
          vm.totalPages = Math.ceil(vm.totalItems / parseInt(vm.pageSize));
        },
      );

      // Rest of your data loading logic
      $http.get(searchURL).then(function (resp) {
        vm.billHistory = resp.data;
        console.log(resp);
      });

      updateCurrentPageOnSite();
    };

    vm.goNextPage = function () {
      if (parseInt(vm.currentPage) + 1 < vm.totalPages) {
        var currentPageInt = parseInt(vm.currentPage) + 1;
        vm.currentPage = currentPageInt.toString();
        updateCurrentPageOnSite();

        if (vm.searchActive) {
          vm.searchBillsByPaginationAndFilters(currentPageInt, true);
        } else {
          loadDefaultData();
        }
      }
    };

    vm.goPreviousPage = function () {
      if (vm.currentPage - 1 >= 0) {
        var currentPageInt = parseInt(vm.currentPage) - 1;
        vm.currentPage = currentPageInt.toString();
        updateCurrentPageOnSite();

        if (vm.searchActive) {
          vm.searchBillsByPaginationAndFilters(currentPageInt, true);
        } else {
          loadDefaultData();
        }
      }
    };

    function updateCurrentPageOnSite() {
      vm.currentPageOnSite = parseInt(vm.currentPage) + 1;
      console.log(vm.currentPage);
    }

    vm.owners = [
      { ownerId: "1", firstName: "George", lastName: "Franklin" },
      { ownerId: "2", firstName: "Betty", lastName: "Davis" },
      { ownerId: "3", firstName: "Eduardo", lastName: "Rodriguez" },
      { ownerId: "4", firstName: "Harold", lastName: "Davis" },
      { ownerId: "5", firstName: "Peter", lastName: "McTavish" },
      { ownerId: "6", firstName: "Jean", lastName: "Coleman" },
      { ownerId: "7", firstName: "Jeff", lastName: "Black" },
      { ownerId: "8", firstName: "Maria", lastName: "Escobito" },
      { ownerId: "9", firstName: "David", lastName: "Schroeder" },
      { ownerId: "10", firstName: "Carlos", lastName: "Esteban" },
    ];

    vm.ownersUUID = [
      {
        ownerId: "f470653d-05c5-4c45-b7a0-7d70f003d2ac",
        firstName: "George",
        lastName: "Franklin",
      },
      {
        ownerId: "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a",
        firstName: "Betty",
        lastName: "Davis",
      },
      {
        ownerId: "3f59dca2-903e-495c-90c3-7f4d01f3a2aa",
        firstName: "Eduardo",
        lastName: "Rodriguez",
      },
      {
        ownerId: "a6e0e5b0-5f60-45f0-8ac7-becd8b330486",
        firstName: "Harold",
        lastName: "Davis",
      },
      {
        ownerId: "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2",
        firstName: "Peter",
        lastName: "McTavish",
      },
      {
        ownerId: "b3d09eab-4085-4b2d-a121-78a0a2f9e501",
        firstName: "Jean",
        lastName: "Coleman",
      },
      {
        ownerId: "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd",
        firstName: "Jeff",
        lastName: "Black",
      },
      {
        ownerId: "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7",
        firstName: "Maria",
        lastName: "Escobito",
      },
      {
        ownerId: "9f6accd1-e943-4322-932e-199d93824317",
        firstName: "David",
        lastName: "Schroeder",
      },
      {
        ownerId: "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f",
        firstName: "Carlos",
        lastName: "Esteban",
      },
    ];

    $http.get("api/gateway/vets").then(function (resp) {
      vm.vetList = resp.data;
      arr = resp.data;
      // console.log(resp)
    });

    $http.get("api/gateway/owners").then(function (owners) {
      vm.ownersInfoArray = owners.data;
      // console.log(vm.ownersInfoArray)
      vm.ownersInfoArray.forEach(function (owner) {
        console.log(owner.ownerId);
      });
    });
    // vm.getOwnerUUIDByName = function(customerId) {
    //     const owner = vm.ownersInfoArray.find(function(owner) {
    //         return owner.firstName === firstName && owner.lastName === lastName;
    //     });
    //
    //     if (owner) {
    //         return owner.ownerId;
    //     }
    //
    //     return 'unknown-uuid';
    // };

    $scope.getOwnerUUIDByCustomerId = function (customerId) {
      let foundOwner;
      // Iterate through vm.owners to find a matching customerId
      vm.owners.forEach(function (owner) {
        if (owner.ownerId === customerId.toString()) {
          foundOwner = owner;
        }
      });
      vm.ownersUUID.forEach(function (owner) {
        if (owner.ownerId === customerId.toString()) {
          foundOwner = owner;
        }
      });
      if (foundOwner) {
        // Get the first and last name from the foundOwner
        const firstName = foundOwner.firstName;
        const lastName = foundOwner.lastName;

        const ownerInfo = vm.ownersInfoArray.find(function (owner) {
          return owner.firstName === firstName && owner.lastName === lastName;
        });
        if (ownerInfo) {
          return ownerInfo.ownerId;
        }
      }

      return "Unknown Owner";
    };

    vm.getOwnerInfoByCustomerId = function (customerId) {
      const owner = vm.ownerIdToInfoMap[customerId];
      return owner || {};
    };

    vm.getOwnerFullName = function (customerId) {
      const owner = vm.ownerIdToInfoMap[customerId];
      if (owner) {
        return owner.firstName + " " + owner.lastName;
      }
      return "Unknown Owner";
    };

    vm.customerNameMap = {};
    vm.customerNameMap2 = {};

    vm.owners.forEach(function (customer) {
      // The customer's ownerId is used as the key, and their full name as the value
      vm.customerNameMap[customer.ownerId] =
        customer.firstName + " " + customer.lastName;
    });

    vm.ownersUUID.forEach(function (customer) {
      vm.customerNameMap2[customer.ownerId] =
        customer.firstName + " " + customer.lastName;
    });

    let eventSource = new EventSource("api/gateway/bills");
    eventSource.addEventListener("message", function (event) {
      $scope.$apply(function () {
        vm.billHistory.push(JSON.parse(event.data));
      });
    });
    eventSource.onerror = (error) => {
      if (eventSource.readyState === 0) {
        eventSource.close();
        console.log("Event source was closed by server successfully. " + error);
      } else {
        console.log("EventSource error: " + error);
      }
    };

    let eventSourcePaid = new EventSource("api/gateway/bills/paid");
    eventSourcePaid.addEventListener("message", function (event) {
      $scope.$apply(function () {
        vm.paidBills.push(JSON.parse(event.data));
      });
    });

    eventSourcePaid.onerror = (error) => {
      if (eventSourcePaid.readyState === 0) {
        eventSourcePaid.close();
        console.log("Event source was closed by server successfully. " + error);
      } else {
        console.log("EventSource error: " + error);
      }
    };

    let eventSourceUnpaid = new EventSource("api/gateway/bills/unpaid");
    eventSourceUnpaid.addEventListener("message", function (event) {
      $scope.$apply(function () {
        vm.unpaidBills.push(JSON.parse(event.data));
      });
    });

    eventSourceUnpaid.onerror = (error) => {
      if (eventSourceUnpaid.readyState === 0) {
        eventSourceUnpaid.close();
        console.log("Event source was closed by server successfully. " + error);
      } else {
        console.log("EventSource error: " + error);
      }
    };

    let eventSourceOverdue = new EventSource("api/gateway/bills/overdue");
    eventSourceOverdue.addEventListener("message", function (event) {
      $scope.$apply(function () {
        vm.overdueBills.push(JSON.parse(event.data));
      });
    });

    eventSourceOverdue.onerror = (error) => {
      if (eventSourceOverdue.readyState === 0) {
        eventSourceOverdue.close();
        console.log("Event source was closed by server successfully. " + error);
      } else {
        console.log("EventSource error: " + error);
      }
    };

    // Assuming that vm.owners is an array of owner objects
    vm.getOwnerById = function (ownerId) {
      // Find and return the owner with the given ownerId
      for (var i = 0; i < vm.owners.length; i++) {
        if (vm.owners[i].ownerId === ownerId) {
          return vm.owners[i];
        }
      }
      return null; // Handle if owner not found
    };

    $scope.getVetDetails = function (vetId) {
      const vet = vm.vetList.find(function (vet) {
        return vet.vetBillId === vetId;
      });
      const vet2 = vm.vetList.find(function (vet) {
        return vet.vetId === vetId;
      });
      if (vet) {
        return vet.firstName + " " + vet.lastName;
      } else if (vet2) {
        return vet2.firstName + " " + vet2.lastName;
      } else {
        return "Unknown Vet";
      }
    };

    $scope.getCustomerDetails = function (customerId) {
      const customerName = vm.customerNameMap[customerId];
      const customerName2 = vm.customerNameMap2[customerId];
      if (customerName) {
        return customerName;
      } else if (customerName2) {
        return customerName2;
      } else {
        return "Unknown Customer";
      }
    };

    $scope.deleteAllBills = function () {
      let varIsConf = confirm(
        "Are you sure you want to delete all the bills in the bill history",
      );
      if (varIsConf) {
        $http.delete("api/gateway/bills").then(successCallback, errorCallback);

        function successCallback(response) {
          $scope.errors = [];
          alert("bill history was deleted successfully");
          console.log(response, "res");
          //refresh list
          $http.get("api/gateway/bills").then(function (resp) {
            vm.billHistory = resp.data;
            arr = resp.data;
          });
        }
        function errorCallback(error) {
          alert(data.errors);
          console.log(error, "Could not receive data");
        }
      } else {
        return;
      }
    };

    $scope.deleteBill = function (billId) {
      let varIsConf = confirm(
        "You are about to delete billId " +
          billId +
          ". Is it what you want to do ? ",
      );
      if (varIsConf) {
        $http
          .delete("api/gateway/bills/" + billId)
          .then(successCallback, errorCallback);

        function successCallback(response) {
          $scope.errors = [];
          alert(billId + " bill was deleted successfully");
          console.log(response, "res");
          //refresh list
          location.reload();
          $http.get("api/gateway/bills").then(function (resp) {
            vm.billHistory = resp.data;
            arr = resp.data;
          });
        }

        function errorCallback(error) {
          alert(data.errors);
          console.log(error, "Could not receive data");
        }
      }
    };
  },
]);

"use strict";
let arr;
angular.module("vetList").controller("VetListController", [
  "$http",
  "$scope",
  function ($http, $scope) {
    var self = this;

    // Modal helper function using index.html global backdrop
    self.showConfirmationModal = function (title, body, confirmCallback) {
      const backdrop = document.getElementById("confirmationBackdrop");
      const modalTitle = document.getElementById("confirmationModalTitle");
      const modalBody = document.getElementById("confirmationModalBody");
      const confirmBtn = document.getElementById("confirmationModalConfirmBtn");
      const cancelBtn = document.getElementById("confirmationModalCancelBtn");

      if (!backdrop) return;

      modalTitle.textContent = title;
      modalBody.textContent = body;

      if (!confirmCallback) {
        confirmBtn.style.display = "none";
      } else {
        confirmBtn.style.display = "inline-block";
      }

      const hideModal = () => {
        backdrop.classList.remove("modalOn");
        backdrop.classList.add("modalOff");
        backdrop.onclick = null;
        confirmBtn.onclick = null;
        cancelBtn.onclick = null;
      };

      backdrop.onclick = (e) => {
        if (e.target === backdrop) {
          hideModal();
        }
      };

      cancelBtn.onclick = hideModal;

      confirmBtn.onclick = () => {
        hideModal();
        if (confirmCallback) {
          confirmCallback();
        }
      };

      backdrop.classList.remove("modalOff");
      backdrop.classList.add("modalOn");
    };

    this.show = ($event, vetID) => {
      let child = document.getElementsByClassName("m" + vetID)[0];
      let left = $event.pageX;
      let top = $event.clientY;
      if (document.documentElement.clientWidth > 960) {
        child.style.left = left + 221 + "px";
      }
      if (document.documentElement.clientWidth < 420) {
        child.style.left = 170 + "px";
      } else if (document.documentElement.clientWidth < 510) {
        child.style.left = left + 334.5 / 2.5 + "px";
      } else {
        child.style.left = left + 200 + "px";
      }
      child.style.top = top + "px";
      child.classList.remove("modalOff");
      child.classList.add("modalOn");
    };

    this.hide = ($event, vetID) => {
      let child = document.getElementsByClassName("m" + vetID)[0];
      child.classList.remove("modalOn");
      child.classList.add("modalOff");
    };

    this.selectedFilter = "Top Vets";

    $scope.vetList = [];

    $http.get("api/gateway/vets").then(function (resp) {
      self.vetList = resp.data;
      arr = resp.data;

      $scope.vetList = resp.data;

      angular.forEach($scope.vetList, function (vet) {
        getCountOfRatings(vet);
        getAverageRating(vet);
        getTopThreeVetsWithHighestRating(vet);
      });
    });

    function getAverageRating(vet) {
      $http
        .get("api/gateway/vets/" + vet.vetId + "/ratings/average")
        .then(function (resp) {
          vet.showRating = true;
          let val = parseFloat(resp.data);
          vet.rating = isNaN(val) ? 0 : parseFloat(val.toFixed(1));
        });
    }

    function getTopThreeVetsWithHighestRating(vet) {
      $http.get("api/gateway/vets/topVets").then(function (resp) {
        vet.showRating = true;
        let val = parseFloat(resp.data);
        vet.rating = isNaN(val) ? 0 : parseFloat(val.toFixed(1));
      });
    }

    function getCountOfRatings(vet) {
      $http
        .get("api/gateway/vets/" + vet.vetId + "/ratings/count")
        .then(function (resp) {
          vet.count = resp.data;
        });
    }

    $scope.deleteVet = function (vetId) {
      const deleteAction = function () {
        $http
          .delete("api/gateway/vets/" + vetId)
          .then(successCallback, errorCallback);

        function successCallback(response) {
          $scope.errors = [];

          $http.get("api/gateway/vets").then(function (resp) {
            self.vetList = resp.data;
            arr = resp.data;
            $scope.vetList = resp.data;

            angular.forEach($scope.vetList, function (vet) {
              getCountOfRatings(vet);
              getAverageRating(vet);
              getTopThreeVetsWithHighestRating(vet);
            });

            setTimeout(() => {
              self.showConfirmationModal(
                "Success",
                vetId + " Deleted Successfully!",
                null,
              );
            }, 150);
          });
        }

        function errorCallback(error) {
          let errorMsg = "Could not delete vet.";
          if (error.status === 403) {
            errorMsg =
              "Access Denied (403 Forbidden): You do not have permission to delete veterinarians.";
          } else if (error.data && error.data.errors) {
            errorMsg = error.data.errors;
          }

          setTimeout(() => {
            self.showConfirmationModal("Error", errorMsg, null);
          }, 150);
          console.log(error, "Error deleting vet.");
        }
      };

      self.showConfirmationModal(
        "Confirm Deletion",
        "Want to delete vet with vetId: " + vetId + ". Are you sure?",
        deleteAction,
      );
    };

    $scope.refreshList = self.vetList;

    $scope.ReloadData = function () {
      let url = "api/gateway/vets";
      let optionSelection = document.getElementById("filterOption").value;
      if (optionSelection === "Active") {
        url += "/active";
      } else if (optionSelection === "Inactive") {
        url += "/inactive";
      } else if (optionSelection === "Top Vets") {
        url += "/topVets";
      }
      self.selectedFilter = optionSelection;

      $http.get(url).then(function (resp) {
        self.vetList = resp.data;
        arr = resp.data;
        $scope.vetList = resp.data;
        angular.forEach(self.vetList, function (vet) {
          getAverageRating(vet);
          getCountOfRatings(vet);
          getTopThreeVetsWithHighestRating(vet);
        });
      });
    };
  },
]);

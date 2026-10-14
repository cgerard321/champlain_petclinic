"use strict";

angular.module("vetDetails").controller("VetDetailsController", [
  "$http",
  "$stateParams",
  "$scope",
  function ($http, $stateParams, $scope) {
    var self = this;

    self.showConfirmationModal = function (title, body, confirmCallback) {
      const backdrop = document.getElementById("confirmationBackdrop");
      const modalTitle = document.getElementById("confirmationModalTitle");
      const modalBody = document.getElementById("confirmationModalBody");
      const confirmBtn = document.getElementById("confirmationModalConfirmBtn");
      const cancelBtn = document.getElementById("confirmationModalCancelBtn");

      if (!backdrop) return;

      modalTitle.textContent = title;
      modalBody.textContent = body;

      const hideModal = () => {
        backdrop.classList.remove("modalOn");
        backdrop.classList.add("modalOff");
        backdrop.onclick = null;
      };

      backdrop.onclick = (e) => {
        if (e.target === backdrop) {
          hideModal();
        }
      };

      cancelBtn.onclick = hideModal;

      confirmBtn.onclick = () => {
        if (confirmCallback) {
          confirmCallback();
        }
        hideModal();
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

    self.checkedCheckboxesUpdate = {};
    self.handleCheckboxClick = function (value, ratingId) {
      self.checkedCheckboxesUpdate[ratingId] = value;
      const checkboxes = document.querySelectorAll(
          'input[type="checkbox"][name="predefinedDescriptionUpdate' +
          ratingId +
          '"]'
      );
      checkboxes.forEach((checkbox) => {
        if (checkbox.value !== value) {
          checkbox.checked = false;
        }
      });
    };

    const checkboxesForAddRating = document.querySelectorAll(
        '.col-sm-12 input[type="checkbox"]'
    );
    let lastCheckedCheckbox = null;
    checkboxesForAddRating.forEach((checkbox) => {
      checkbox.addEventListener("click", function () {
        if (this !== lastCheckedCheckbox) {
          if (lastCheckedCheckbox) {
            lastCheckedCheckbox.checked = false;
          }
          lastCheckedCheckbox = this;
        }
        const value = lastCheckedCheckbox.checked
            ? lastCheckedCheckbox.value
            : null;
        console.log("Last checked value:", value);
      });
    });

    self.togglePredefinedDescription = function (rating, value) {
      if (rating["predefinedDescription" + value] === value) {
        rating["predefinedDescription" + value] = null;
      } else {
        rating["predefinedDescription" + value] = value;
      }
    };

    self.workdayToWorkHours = new Map();

    $http.get("api/gateway/vets/" + $stateParams.vetId).then(function (resp) {
      self.vet = resp.data;
      const workHoursObject = JSON.parse(self.vet.workHoursJson);

      for (const key in workHoursObject) {
        if (workHoursObject.hasOwnProperty(key)) {
          self.workdayToWorkHours.set(key, workHoursObject[key]);
        }
      }
    });

    self.selectWorkday = function (selectedWorkday) {
      self.workHours = [];

      if (self.workdayToWorkHours.has(selectedWorkday)) {
        self.workHours = self.workdayToWorkHours.get(selectedWorkday);
      } else {
        console.log("Workday not found in the mapping.");
      }

      var table = document.getElementById("workHoursTable");
      var tdElements = table.getElementsByTagName("td");

      if (table.style.display === "none") {
        table.style.display = "block";
      } else {
        table.style.display = "none";
      }

      for (var i = 0; i < tdElements.length; i++) {
        var td = tdElements[i];
        var tdId = td.id;

        if (self.workHours.includes(tdId)) {
          td.style.border = "2px solid green";
        } else {
          td.style.border = "2px solid black";
        }
      }
    };

    $http
        .get("api/gateway/visits/vets/" + $stateParams.vetId)
        .then(function (resp) {
          self.visitsList = resp.data;
        });

    $http
        .get("api/gateway/vets/" + $stateParams.vetId + "/ratings")
        .then(function (resp) {
          self.ratings = resp.data;
        });

    $http
        .get("api/gateway/vets/" + $stateParams.vetId + "/educations")
        .then(function (resp) {
          self.educations = resp.data;
        });

    $scope.deleteVetEducation = function (educationId) {
      const deleteAction = function () {
        $http
            .delete(
                "api/gateway/vets/" +
                $stateParams.vetId +
                "/educations/" +
                educationId
            )
            .then(
                function successCallback(response) {
                  self.showConfirmationModal(
                      "Success",
                      "Education " + educationId + " was deleted successfully!",
                      null
                  );
                  $http
                      .get("api/gateway/vets/" + $stateParams.vetId + "/educations")
                      .then(function (resp) {
                        self.educations = resp.data;
                      });
                },
                function errorCallback(error) {
                  self.showConfirmationModal(
                      "Error",
                      (error.data && error.data.errors) || "Could not delete education.",
                      null
                  );
                }
            );
      };

      self.showConfirmationModal(
          "Confirm Deletion",
          "Are you sure you want to delete education " + educationId + "?",
          deleteAction
      );
    };

    self.deleteSpecialty = function (specialty) {
      const deleteAction = function () {
        $http
            .delete(
                "api/v2/gateway/vets/" +
                $stateParams.vetId +
                "/specialties/" +
                specialty.specialtyId
            )
            .then(
                function successCallback(response) {
                  $http
                      .get("api/gateway/vets/" + $stateParams.vetId)
                      .then(function (resp) {
                        self.vet = resp.data;
                      });
                },
                function errorCallback(error) {
                  self.showConfirmationModal(
                      "Error",
                      (error.data && error.data.errors) || "Could not delete specialty.",
                      null
                  );
                }
            );
      };

      self.showConfirmationModal(
          "Confirm Deletion",
          'Are you sure you want to delete specialty "' + specialty.name + '"?',
          deleteAction
      );
    };

    self.updateEducation = function (educationId) {
      const btn = document.getElementById("updateEducationBtn" + educationId);
      const updateContainer = document.getElementById(
          "educationUpdate" + educationId
      );

      let updatedDegree = document.getElementById(
          "updateDegree" + educationId
      ).value;
      let updatedSchoolName = document.getElementById(
          "updateSchoolName" + educationId
      ).value;
      let updatedFieldOfStudy = document.getElementById(
          "updateFieldOfStudy" + educationId
      ).value;
      let updatedStartDate = document.getElementById(
          "updateStartDate" + educationId
      ).value;
      let updatedEndDate = document.getElementById(
          "updateEndDate" + educationId
      ).value;

      let updatedEducation = {
        educationId: educationId,
        degree: updatedDegree,
        schoolName: updatedSchoolName,
        vetId: $stateParams.vetId,
        fieldOfStudy: updatedFieldOfStudy,
        startDate: updatedStartDate,
        endDate: updatedEndDate,
      };

      if (updateContainer.style.display === "none") {
        updateContainer.style.display = "block";
        btn.textContent = "Save";
      } else if (btn.textContent === "Save") {
        if (
            !updatedDegree ||
            !updatedSchoolName ||
            !updatedFieldOfStudy ||
            !updatedStartDate ||
            !updatedEndDate
        ) {
          self.showConfirmationModal(
              "Validation Error",
              "Please fill in all education fields.",
              null
          );
          return;
        }

        const confirmUpdate = function () {
          $http
              .put(
                  "api/gateway/vets/" +
                  $stateParams.vetId +
                  "/educations/" +
                  educationId,
                  updatedEducation
              )
              .then(function (resp) {
                self.updatedEducation = resp.data;
                self.showConfirmationModal(
                    "Success",
                    "Your education was successfully updated!",
                    null
                );

                $http
                    .get("api/gateway/vets/" + $stateParams.vetId + "/educations")
                    .then(function (resp) {
                      self.educations = resp.data;
                    });
              })
              .catch(function (error) {
                self.showConfirmationModal(
                    "Error",
                    (error.data && error.data.errors) || "Could not update education.",
                    null
                );
              });

          updateContainer.style.display = "none";
          btn.textContent = "Update";
        };

        self.showConfirmationModal(
            "Confirm Update",
            "Are you sure you want to save these changes?",
            confirmUpdate
        );
      }
    };

    $http
        .get("api/gateway/vets/" + $stateParams.vetId + "/ratings/percentages")
        .then(function (resp) {
          const ratingsData = resp.data;
          const ratingsContainer = document.getElementById("ratings-list");
          let html = "";
          for (const key in ratingsData) {
            if (ratingsData.hasOwnProperty(key)) {
              const percentage = ratingsData[key] * 100;
              html += key + " stars - " + percentage.toFixed(0) + "%<br>";
            }
          }
          ratingsContainer.innerHTML = html.slice(0, -4);
        });

    $scope.getRecentRatingBasedOnDate = function () {
      const wrongYearPattern = /^\d{4}$/;

      let yearQuery = document.getElementById("queryDate").value;
      let year = new Date().getFullYear();

      if (!wrongYearPattern.test(yearQuery)) {
        self.showConfirmationModal(
            "Validation Error",
            "Invalid year format. Please enter a valid year.",
            null
        );
        return;
      } else if (self.query === undefined || self.query === "") {
        let newYear = year - 2;
        $http
            .get(
                "api/gateway/vets/" +
                $stateParams.vetId +
                "/ratings/date?year=" +
                newYear
            )
            .then(function (resp) {
              self.ratings = resp.data;
            });
      } else {
        $http
            .get(
                "api/gateway/vets/" +
                $stateParams.vetId +
                "/ratings/date?year=" +
                yearQuery
            )
            .then(function (resp) {
              self.ratings = resp.data;
            });
      }
    };

    $scope.deleteVetRating = function (ratingId) {
      const deleteAction = function () {
        $http
            .delete(
                "api/gateway/vets/" + $stateParams.vetId + "/ratings/" + ratingId
            )
            .then(
                function successCallback(response) {
                  self.showConfirmationModal(
                      "Success",
                      ratingId + " Deleted Successfully!",
                      null
                  );

                  $http
                      .get("api/gateway/vets/" + $stateParams.vetId + "/ratings")
                      .then(function (resp) {
                        self.ratings = resp.data;
                      });
                },
                function errorCallback(error) {
                  self.showConfirmationModal(
                      "Error",
                      (error.data && error.data.errors) || "Could not delete rating.",
                      null
                  );
                }
            );
      };

      self.showConfirmationModal(
          "Confirm Deletion",
          "Are you sure you want to delete rating " + ratingId + "?",
          deleteAction
      );
    };

    self.updateRating = function (ratingId) {
      const btn = document.getElementById("updateRatingBtn" + ratingId);
      const updateContainer = document.getElementById(
          "ratingUpdate" + ratingId
      );
      const selectedValue = parseInt(
          document.getElementById("ratingOptions" + ratingId).value
      );

      if (selectedValue < 1 || selectedValue > 5) {
        self.showConfirmationModal(
            "Validation Error",
            "rateScore should be between 1 and 5: " + selectedValue,
            null
        );
        return;
      }

      let updatedDescription = document.getElementById(
          "updateDescription" + ratingId
      ).value;
      if (updatedDescription.trim() === "") {
        updatedDescription = null;
      }

      const predefinedDesc = document.querySelector(
          'input[name="predefinedDescriptionUpdate' + ratingId + '"]:checked'
      )
          ? document.querySelector(
              'input[name="predefinedDescriptionUpdate' + ratingId + '"]:checked'
          ).value
          : null;

      let updatedRating = {
        ratingId: ratingId,
        rateScore: selectedValue,
        vetId: $stateParams.vetId,
        rateDescription: updatedDescription,
        rateDate: Date.now().toString(),
        predefinedDescription: predefinedDesc,
      };

      if (updateContainer.style.display === "none") {
        updateContainer.style.display = "block";
        btn.textContent = "Save";
      } else if (btn.textContent === "Save") {
        const confirmUpdate = function () {
          $http
              .put(
                  "api/gateway/vets/" + $stateParams.vetId + "/ratings/" + ratingId,
                  updatedRating
              )
              .then(function (resp) {
                self.updatedRating = resp.data;
                self.showConfirmationModal(
                    "Success",
                    "Your review was successfully updated!",
                    null
                );

                $http
                    .get("api/gateway/vets/" + $stateParams.vetId + "/ratings")
                    .then(function (resp) {
                      self.ratings = resp.data;
                    });
                percentageOfRatings();
                $http
                    .get("api/gateway/vets/" + $stateParams.vetId + "/badge")
                    .then(function (resp) {
                      self.badge = resp.data;
                    });
              })
              .catch(function (error) {
                self.showConfirmationModal(
                    "Error",
                    (error.data && error.data.errors) || "Could not update rating.",
                    null
                );
              });

          updateContainer.style.display = "none";
          btn.textContent = "Update";
          document
              .querySelectorAll(
                  'input[name="predefinedDescriptionUpdate' + ratingId + '"]'
              )
              .forEach(function (radio) {
                radio.checked = false;
              });
        };

        self.showConfirmationModal(
            "Confirm Update",
            "Are you sure you want to save this rating?",
            confirmUpdate
        );
      }
    };

    $http
        .get("api/gateway/vets/" + $stateParams.vetId + "/badge")
        .then(function (resp) {
          self.badge = resp.data;
        });

    self.init = function () {
      $http
          .get("api/gateway/vets/" + $stateParams.vetId + "/photo")
          .then(function (resp) {
            self.vetPhoto = resp.data;
          });
    };

    $http
        .get("api/gateway/vets/" + $stateParams.vetId + "/default-photo")
        .then(function (resp) {
          self.vetPhoto = resp.data;
          if (self.vetPhoto.filename === "vet_default.jpg") {
            self.vetPhoto.photo = self.vetPhoto.resourceBase64;
          } else {
            throw new Error();
          }
        })
        .catch(function () {
          $http
              .get("api/gateway/vets/" + $stateParams.vetId + "/photo")
              .then(function (resp) {
                self.vetPhoto = resp.data;
              });
        });

    self.submitRatingForm = function () {
      var rating = {
        vetId: $stateParams.vetId,
        rateScore: parseFloat(document.getElementById("ratingScore").value),
        rateDate: new Date().toLocaleDateString(),
        rateDescription: document.getElementById("ratingDescription").value,
        predefinedDescription: document.querySelector(
            'input[name="predefinedDescriptionPOOR"]:checked'
        )
            ? document.querySelector(
                'input[name="predefinedDescriptionPOOR"]:checked'
            ).value
            : document.querySelector(
                'input[name="predefinedDescriptionGOOD"]:checked'
            )
                ? document.querySelector(
                    'input[name="predefinedDescriptionGOOD"]:checked'
                ).value
                : document.querySelector(
                    'input[name="predefinedDescriptionEXCELLENT"]:checked'
                )
                    ? document.querySelector(
                        'input[name="predefinedDescriptionEXCELLENT"]:checked'
                    ).value
                    : null,
        ratingDate: document.querySelector('input[name="Year"]:checked')
            ? document.querySelector('input[name="Year"]:checked').value
            : null,
      };

      if (!rating.rateScore) {
        self.showConfirmationModal(
            "Validation Error",
            "Please select a rating score.",
            null
        );
        return;
      } else {
        if (rating.rateDescription.trim() === "") {
          rating.rateDescription = null;
        }
        $http
            .post("api/gateway/vets/" + $stateParams.vetId + "/ratings", rating)
            .then(function (resp) {
              self.rating = resp.data;
              self.showConfirmationModal(
                  "Success",
                  "Your review was successfully added!",
                  null
              );

              $http
                  .get("api/gateway/vets/" + $stateParams.vetId + "/ratings")
                  .then(function (resp) {
                    self.ratings = resp.data;
                  });

              percentageOfRatings();

              document.getElementById("ratingScore").value = "";
              document.getElementById("ratingDescription").value = "";

              $http
                  .get("api/gateway/vets/" + $stateParams.vetId + "/badge")
                  .then(function (resp) {
                    self.badge = resp.data;
                  });
            })
            .catch(function (error) {
              let errorMessage =
                  "An error occurred while adding the rating. Please try again.";
              if (error.data && error.data.errors) {
                errorMessage = error.data.errors;
              }
              self.showConfirmationModal("Error", errorMessage, null);
            });
        document.getElementById("ratingForm").reset();
      }
    };

    self.submitEducationForm = function (education) {
      education.vetId = $stateParams.vetId;
      education.degree = document.getElementById("degree").value;
      education.schoolName = document.getElementById("schoolName").value;
      education.fieldOfStudy = document.getElementById("fieldOfStudy").value;
      education.startDate = document.getElementById("startDate").value;
      education.endDate = document.getElementById("endDate").value;

      $http
          .post(
              "api/gateway/vets/" + $stateParams.vetId + "/educations",
              education
          )
          .then(
              function (resp) {
                self.education = resp.data;
                self.addEducationFormVisible = false;
                self.showConfirmationModal(
                    "Success",
                    "Your education was successfully added!",
                    null
                );

                $http
                    .get("api/gateway/vets/" + $stateParams.vetId + "/educations")
                    .then(function (resp) {
                      self.educations = resp.data;
                    });
                self.newEducation = {};
              },
              function (error) {
                let errorMessage = "An error occurred while adding the education.";
                if (error.data && error.data.message) {
                  errorMessage = error.data.message;
                }
                self.showConfirmationModal(
                    "Error",
                    "Error adding education: " + errorMessage,
                    null
                );
              }
          );
    };

    function percentageOfRatings() {
      $http
          .get("api/gateway/vets/" + $stateParams.vetId + "/ratings/percentages")
          .then(function (resp) {
            const ratingsData = resp.data;
            const ratingsContainer = document.getElementById("ratings-list");
            let html = "";
            const ratingsArray = [];
            for (const key in ratingsData) {
              if (ratingsData.hasOwnProperty(key)) {
                const percentage = ratingsData[key] * 100;
                ratingsArray.push({ rating: parseFloat(key), percentage });
              }
            }
            for (const ratingObj of ratingsArray) {
              html +=
                  ratingObj.rating +
                  " stars - " +
                  ratingObj.percentage.toFixed(0) +
                  "%<br>";
            }
            ratingsContainer.innerHTML = html.slice(0, -4);
          });
    }

    self.newEducation = {};
    self.addEducationFormVisible = false;

    self.addEducation = function () {
      self.addEducationFormVisible = true;
    };

    self.cancelEducationForm = function () {
      self.newEducation = {};
      self.addEducationFormVisible = false;
    };

    self.addSpecialtyFormVisible = false;
    self.newSpecialty = {};

    self.addSpecialty = function () {
      self.addSpecialtyFormVisible = true;
    };

    self.submitSpecialtyForm = function () {
      if (!self.newSpecialty.name || self.newSpecialty.name.trim() === "") {
        self.showConfirmationModal(
            "Validation Error",
            "Please enter a specialty name.",
            null
        );
        return;
      }

      const specialty = {
        specialtyId: "",
        name: self.newSpecialty.name.trim(),
      };

      const confirmAdd = function () {
        $http
            .post(
                "api/v2/gateway/vets/" + $stateParams.vetId + "/specialties",
                specialty
            )
            .then(
                function (resp) {
                  self.addSpecialtyFormVisible = false;
                  self.newSpecialty = {};

                  $http
                      .get("api/gateway/vets/" + $stateParams.vetId)
                      .then(function (resp) {
                        self.vet = resp.data;
                      });
                },
                function (error) {
                  let errorMessage = "Could not add specialty.";
                  if (error.data && error.data.message) {
                    errorMessage = error.data.message;
                  }
                  self.showConfirmationModal("Error", errorMessage, null);
                }
            );
      };

      self.showConfirmationModal(
          "Confirm Add",
          'Are you sure you want to add specialty "' + specialty.name + '"?',
          confirmAdd
      );
    };

    self.cancelSpecialtyForm = function () {
      self.newSpecialty = {};
      self.addSpecialtyFormVisible = false;
    };
  },
]);
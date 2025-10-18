'use strict';

angular.module('vetDetails').controller('VetDetailsController', [
  '$http',
  '$stateParams',
  '$scope',
  function ($http, $stateParams, $scope) {
    var self = this;
    //var vetId = $stateParams.vetId || 0;

    self.showConfirmationModal = function (title, body, confirmCallback) {
      const backdrop = document.getElementById('confirmationBackdrop');
      const modalTitle = document.getElementById('confirmationModalTitle');
      const modalBody = document.getElementById('confirmationModalBody');
      const confirmBtn = document.getElementById('confirmationModalConfirmBtn');
      const cancelBtn = document.getElementById('confirmationModalCancelBtn');

      modalTitle.textContent = title;
      modalBody.textContent = body;

      const hideModal = () => {
        backdrop.classList.remove('modalOn');
        backdrop.classList.add('modalOff');
      };

      cancelBtn.onclick = hideModal;

      confirmBtn.onclick = () => {
        if (confirmCallback) {
          confirmCallback();
        }
        hideModal();
      };

      backdrop.classList.remove('modalOff');
      backdrop.classList.add('modalOn');
    };

    this.show = ($event, vetID) => {
      let child = document.getElementsByClassName('m' + vetID)[0];
      let left = $event.pageX;
      let top = $event.clientY;
      if (document.documentElement.clientWidth > 960) {
        child.style.left = left + 221 + 'px';
      }
      if (document.documentElement.clientWidth < 420) {
        child.style.left = 170 + 'px';
      } else if (document.documentElement.clientWidth < 510) {
        child.style.left = left + 334.5 / 2.5 + 'px';
      } else {
        child.style.left = left + 200 + 'px';
      }
      child.style.top = top + 'px';
      child.classList.remove('modalOff');
      child.classList.add('modalOn');
    };
    this.hide = ($event, vetID) => {
      let child = document.getElementsByClassName('m' + vetID)[0];
      child.classList.remove('modalOn');
      child.classList.add('modalOff');
    };

    self.checkedCheckboxesUpdate = {};
    self.handleCheckboxClick = function (value, ratingId) {
      // Update the checked state for the specific ratingId.
      self.checkedCheckboxesUpdate[ratingId] = value;
      // Uncheck other checkboxes for the same ratingId.
      const checkboxes = document.querySelectorAll(
        'input[type="checkbox"][name="predefinedDescriptionUpdate' +
          ratingId +
          '"]'
      );
      checkboxes.forEach(checkbox => {
        if (checkbox.value !== value) {
          checkbox.checked = false;
        }
      });
    };

    const checkboxesForAddRating = document.querySelectorAll(
      '.col-sm-12 input[type="checkbox"]'
    );
    let lastCheckedCheckbox = null;
    checkboxesForAddRating.forEach(checkbox => {
      checkbox.addEventListener('click', function () {
        if (this !== lastCheckedCheckbox) {
          if (lastCheckedCheckbox) {
            lastCheckedCheckbox.checked = false;
          }
          lastCheckedCheckbox = this;
        }
        // const value = lastCheckedCheckbox.checked
        //   ? lastCheckedCheckbox.value
        //   : null;
        // console.log removed('Last checked value:', value);
      });
    });
    self.togglePredefinedDescription = function (rating, value) {
      // console.log removed('Toggling predefinedDescription: ', value);
      if (rating['predefinedDescription' + value] === value) {
        rating['predefinedDescription' + value] = null;
      } else {
        rating['predefinedDescription' + value] = value;
      }
    };

    self.workdayToWorkHours = new Map();

    /* added /{{vet.vetID}} in the url */
    $http.get('api/gateway/vets/' + $stateParams.vetId).then(function (resp) {
      self.vet = resp.data;
      const workHoursObject = JSON.parse(self.vet.workHoursJson);

      // Convert the JSON object into a Map
      for (const key in workHoursObject) {
        if (Object.prototype.hasOwnProperty.call(workHoursObject, key)) {
          self.workdayToWorkHours.set(key, workHoursObject[key]);
        }
      }

      // console.log removed(self.workdayToWorkHours);
    });

    // map of work hours
    self.selectWorkday = function (selectedWorkday) {
      self.workHours = [];

      // Check if the selected workday exists in the mapping
      if (self.workdayToWorkHours.has(selectedWorkday)) {
        self.workHours = self.workdayToWorkHours.get(selectedWorkday);
      } else {
        // Handle the case when the workday is not found in the mapping
        // console.log removed('Workday not found in the mapping.');
      }

      // Get the table element by its ID
      var table = document.getElementById('workHoursTable');
      // Get all the <td> elements in the table
      var tdElements = table.getElementsByTagName('td');
      // console.log removed(tdElements);

      if (table.style.display === 'none') {
        table.style.display = 'block';
      } else {
        table.style.display = 'none';
      }

      // Loop through the <td> elements to check if their IDs correspond with the elements in workHours
      for (var i = 0; i < tdElements.length; i++) {
        var td = tdElements[i];
        var tdId = td.id;

        // Check if the tdId is in the self.workHours array
        if (self.workHours.includes(tdId)) {
          // This <td> corresponds to a work hour for the selected day
          // You can apply styling or any other actions you need here
          td.style.border = '2px solid green'; // For example, add a green border
        } else {
          // This <td> is not in the workHours array
          // You can apply a different style for non-working hours
          td.style.border = '2px solid black'; // For example, add a black border
        }
      }
    };

    $http
      .get('api/gateway/visits/vets/' + $stateParams.vetId)
      .then(function (resp) {
        self.visitsList = resp.data;
      });

    $http
      .get('api/gateway/vets/' + $stateParams.vetId + '/ratings')
      .then(function (resp) {
        // console.log removed(resp.data);
        self.ratings = resp.data;
      });

    $http
      .get('api/gateway/vets/' + $stateParams.vetId + '/educations')
      .then(function (resp) {
        // console.log removed(resp.data);
        self.educations = resp.data;
      });

    $scope.deleteVetEducation = function (educationId) {
      const deleteAction = function () {
        $http
          .delete(
            'api/gateway/vets/' +
              $stateParams.vetId +
              '/educations/' +
              educationId
          )
          .then(
            function successCallback() {
              self.showConfirmationModal(
                'Success',
                'Education ' + educationId + ' was deleted successfully!',
                null
              );
              $http
                .get('api/gateway/vets/' + $stateParams.vetId + '/educations')
                .then(function (resp) {
                  self.educations = resp.data;
                });
            },
            function errorCallback(error) {
              self.showConfirmationModal(
                'Error',
                error.data.errors || 'Could not delete education.',
                null
              );
            }
          );
      };

      self.showConfirmationModal(
        'Confirm Deletion',
        'Are you sure you want to delete education ' + educationId + '?',
        deleteAction
      );
    };

    //update education
    self.updateEducation = function (educationId) {
      const btn = document.getElementById('updateEducationBtn' + educationId);
      const updateContainer = document.getElementById(
        'educationUpdate' + educationId
      );

      let updatedDegree = document.getElementById(
        'updateDegree' + educationId
      ).value;
      let updatedSchoolName = document.getElementById(
        'updateSchoolName' + educationId
      ).value;
      let updatedFieldOfStudy = document.getElementById(
        'updateFieldOfStudy' + educationId
      ).value;
      let updatedStartDate = document.getElementById(
        'updateStartDate' + educationId
      ).value;
      let updatedEndDate = document.getElementById(
        'updateEndDate' + educationId
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

      if (updateContainer.style.display === 'none') {
        updateContainer.style.display = 'block';
        btn.textContent = 'Save';
      } else if (btn.textContent === 'Save') {
        if (
          !updatedDegree ||
          !updatedSchoolName ||
          !updatedFieldOfStudy ||
          !updatedStartDate ||
          !updatedEndDate
        ) {
          self.showConfirmationModal(
            'Validation Error',
            'Please fill in all education fields.',
            null
          );
          return;
        }

        const confirmUpdate = function () {
          $http
            .put(
              'api/gateway/vets/' +
                $stateParams.vetId +
                '/educations/' +
                educationId,
              updatedEducation
            )
            .then(function (resp) {
              self.updatedEducation = resp.data;
              self.showConfirmationModal(
                'Success',
                'Your education was successfully updated!',
                null
              );

              $http
                .get('api/gateway/vets/' + $stateParams.vetId + '/educations')
                .then(function (resp) {
                  self.educations = resp.data;
                });
            })
            .catch(function (error) {
              self.showConfirmationModal(
                'Error',
                error.data.errors || 'Could not update education.',
                null
              );
            });

          updateContainer.style.display = 'none';
          btn.textContent = 'Update';
        };

        self.showConfirmationModal(
          'Confirm Update',
          'Are you sure you want to save these changes?',
          confirmUpdate
        );
      }
    };

    $http
      .get('api/gateway/vets/' + $stateParams.vetId + '/ratings/percentages')
      .then(function (resp) {
        const ratingsData = resp.data;
        const ratingsContainer = document.getElementById('ratings-list');
        let html = '';
        for (const key in ratingsData) {
          if (Object.prototype.hasOwnProperty.call(ratingsData, key)) {
            const percentage = ratingsData[key] * 100; // Convert fraction to percentage
            html += key + ' stars - ' + percentage.toFixed(0) + '%';
            html += '<br>';
          }
        }
        ratingsContainer.innerHTML = html.slice(0, -2);
      });

    $scope.getRecentRatingBasedOnDate = function () {
      // console.log removed('In function based on date');
      const wrongYearPattern = /^\d{4}$/;

      let yearQuery = document.getElementById('queryDate').value;
      let year = new Date().getFullYear();

      if (!wrongYearPattern.test(yearQuery)) {
        // Throw an exception with a custom error message
        alert('Invalid year format. Please enter a valid year.');
        return;
      } else if (self.query === undefined || self.query === '') {
        let newYear = year - 2;
        $http
          .get(
            'api/gateway/vets/' +
              $stateParams.vetId +
              '/ratings/date?year=' +
              newYear
          )
          .then(function (resp) {
            // console.log removed(resp.data);
            self.ratings = resp.data;
            arr = resp.data;
          });
      } else {
        $http
          .get(
            'api/gateway/vets/' +
              $stateParams.vetId +
              '/ratings/date?year=' +
              yearQuery
          )
          .then(function (resp) {
            // console.log removed(resp.data);
            self.ratings = resp.data;
            arr = resp.data;
          });
      }
    };

    $scope.deleteVetRating = function (ratingId) {
      //added $scope in this class
      const deleteAction = function () {
        $http
          .delete(
            'api/gateway/vets/' + $stateParams.vetId + '/ratings/' + ratingId
          )
          .then(
            function successCallback() {
              self.showConfirmationModal(
                'Success',
                ratingId + ' Deleted Successfully!',
                null
              );

              $http
                .get('api/gateway/vets/' + $stateParams.vetId + '/ratings')
                .then(function (resp) {
                  self.ratings = resp.data;
                });
            },
            function errorCallback(error) {
              self.showConfirmationModal(
                'Error',
                error.data.errors || 'Could not delete rating.',
                null
              );
            }
          );
      };

      self.showConfirmationModal(
        'Confirm Deletion',
        'Are you sure you want to delete rating ' + ratingId + '?',
        deleteAction
      );
    };

    self.updateRating = function (ratingId) {
      const btn = document.getElementById('updateRatingBtn' + ratingId);
      const updateContainer = document.getElementById(
        'ratingUpdate' + ratingId
      );
      const selectedValue = parseInt(
        document.getElementById('ratingOptions' + ratingId).value
      );

      if (selectedValue < 1 || selectedValue > 5) {
        alert('rateScore should be between 1 and 5' + selectedValue);
        return;
      }

      let updatedDescription = document.getElementById(
        'updateDescription' + ratingId
      ).value;
      if (updatedDescription.trim() === '') {
        updatedDescription = null;
      }

      const predefinedDesc = document.querySelector(
        'input[name="predefinedDescriptionUpdate' + ratingId + '"]:checked'
      )
        ? document.querySelector(
            'input[name="predefinedDescriptionUpdate' + ratingId + '"]:checked'
          ).value
        : null;

      //CHECK
      // const ratingDate = document.querySelector(
      //   'input[name="Year' + ratingId + '"]:checked'
      // )
      //   ? document.querySelector('input[name="Year' + ratingId + '"]:checked')
      //       .value
      //   : null;

      let updatedRating = {
        ratingId: ratingId,
        rateScore: selectedValue,
        vetId: $stateParams.vetId,
        rateDescription: updatedDescription,
        rateDate: Date.now().toString(),
        predefinedDescription: predefinedDesc,
      };

      if (updateContainer.style.display === 'none') {
        // Show the update form
        updateContainer.style.display = 'block';
        btn.textContent = 'Save';
      } else if (btn.textContent === 'Save') {
        // Save the updated rating
        const confirmUpdate = function () {
          $http
            .put(
              'api/gateway/vets/' + $stateParams.vetId + '/ratings/' + ratingId,
              updatedRating
            )
            .then(function (resp) {
              self.updatedRating = resp.data;
              self.showConfirmationModal(
                'Success',
                'Your review was successfully updated!',
                null
              );

              $http
                .get('api/gateway/vets/' + $stateParams.vetId + '/ratings')
                .then(function (resp) {
                  self.ratings = resp.data;
                });
              percentageOfRatings();
              $http
                .get('api/gateway/vets/' + $stateParams.vetId + '/badge')
                .then(function (resp) {
                  self.badge = resp.data;
                });
            })
            .catch(function (error) {
              self.showConfirmationModal(
                'Error',
                error.data.errors || 'Could not update rating.',
                null
              );
            });

          updateContainer.style.display = 'none';
          btn.textContent = 'Update';
          document
            .querySelectorAll(
              'input[name="predefinedDescriptionUpdate' + ratingId + '"]'
            )
            .forEach(function (radio) {
              radio.checked = false;
            });
        };

        self.showConfirmationModal(
          'Confirm Update',
          'Are you sure you want to save this rating?',
          confirmUpdate
        );
      }
    };

    //badge
    $http
      .get('api/gateway/vets/' + $stateParams.vetId + '/badge')
      .then(function (resp) {
        self.badge = resp.data;
        // console.log removed(resp.data);
      });
    self.init = function () {
      $http
        .get('api/gateway/vets/' + $stateParams.vetId + '/badge')
        .then(function (resp) {
          self.badge = resp.data;
          // console.log removed(resp.data);
        });
    };

    //photo
    $http
      .get('api/gateway/vets/' + $stateParams.vetId + '/default-photo')
      .then(function (resp) {
        self.vetPhoto = resp.data;
        if (self.vetPhoto.filename == 'vet_default.jpg')
          self.vetPhoto.photo = self.vetPhoto.resourceBase64;
        else throw new Error();
        // console.log removed(resp.data);
      })
      .catch(function () {
        // console.log removed(error);
        $http
          .get('api/gateway/vets/' + $stateParams.vetId + '/photo')
          .then(function (resp) {
            self.vetPhoto = resp.data;
            // console.log removed(self.vetPhoto.photo);
            // console.log removed(resp.data);
          });
      });
    /* //photo
        $http.get('api/gateway/vets/' + $stateParams.vetId + '/photo').then(function (resp) {
            self.vetPhoto = resp.data;
        });
*/

    self.init = function () {
      $http
        .get('api/gateway/vets/' + $stateParams.vetId + '/photo')
        .then(function (resp) {
          self.vetPhoto = resp.data;
        });
    };

    self.submitRatingForm = function () {
      var rating = {
        vetId: $stateParams.vetId,
        rateScore: parseFloat(document.getElementById('ratingScore').value),
        rateDate: new Date().toLocaleDateString(),
        rateDescription: document.getElementById('ratingDescription').value,
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
        //ADDITION
        ratingDate: document.querySelector('input[name="Year"]:checked')
          ? document.querySelector('input[name="Year"]:checked').value
          : null,
      };
      if (!rating.rateScore) {
        alert('Please select a rating score');
        return;
      } else {
        // If ratingDescription is an empty string, set it to null
        if (rating.rateDescription.trim() === '') {
          rating.rateDescription = null;
        }
        $http
          .post('api/gateway/vets/' + $stateParams.vetId + '/ratings', rating)
          .then(function (resp) {
            self.rating = resp.data;
            alert('Your review was successfully added!');

            // Refresh list
            $http
              .get('api/gateway/vets/' + $stateParams.vetId + '/ratings')
              .then(function (resp) {
                // console.log removed(resp.data);
                self.ratings = resp.data;
                arr = resp.data;
              });

            // Refresh percentages
            percentageOfRatings();

            document.getElementById('ratingScore').value = '';
            document.getElementById('ratingDescription').value = '';

            // Refresh list
            $http
              .get('api/gateway/vets/' + $stateParams.vetId + '/ratings')
              .then(function (resp) {
                self.ratings = resp.data;
                arr = resp.data;
              });

            //refresh badge
            $http
              .get('api/gateway/vets/' + $stateParams.vetId + '/badge')
              .then(function (resp) {
                self.badge = resp.data;
                // console.log removed(resp.data);
              });
          })
          .catch(function (error) {
            let errorMessage =
              'An error occurred while adding the rating. Please try again.';
            if (error.data && error.data.errors) {
              errorMessage = error.data.errors;
            }
            alert(errorMessage);
            // Note: resp is not available in catch block
          });
        document.getElementById('ratingForm').reset();
      }
    };

    self.submitEducationForm = function (education) {
      education.vetId = $stateParams.vetId;
      education.degree = document.getElementById('degree').value;
      education.schoolName = document.getElementById('schoolName').value;
      education.fieldOfStudy = document.getElementById('fieldOfStudy').value;
      education.startDate = document.getElementById('startDate').value;
      education.endDate = document.getElementById('endDate').value;

      // Send a POST request to add the new education
      $http
        .post(
          'api/gateway/vets/' + $stateParams.vetId + '/educations',
          education
        )
        .then(
          function (resp) {
            self.education = resp.data;
            self.addEducationFormVisible = false;
            self.showConfirmationModal(
              'Success',
              'Your education was successfully added!',
              null
            );

            $http
              .get('api/gateway/vets/' + $stateParams.vetId + '/educations')
              .then(function (resp) {
                self.educations = resp.data;
              });
            self.newEducation = {}; // Reset the model instead of the non-existent form
          },
          function (error) {
            let errorMessage = 'An error occurred while adding the education.';
            if (error.data && error.data.message) {
              errorMessage = error.data.message;
            }
            self.showConfirmationModal(
              'Error',
              'Error adding education: ' + errorMessage,
              null
            );
          }
        );
    };

    // function uuidv4() {
    //   return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
    //     (
    //       c ^
    //       (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))
    //     ).toString(16)
    //   );
    // }

    function percentageOfRatings() {
      $http
        .get('api/gateway/vets/' + $stateParams.vetId + '/ratings/percentages')
        .then(function (resp) {
          const ratingsData = resp.data;
          const ratingsContainer = document.getElementById('ratings-list');
          let html = '';
          const ratingsArray = [];
          for (const key in ratingsData) {
            if (Object.prototype.hasOwnProperty.call(ratingsData, key)) {
              const percentage = ratingsData[key] * 100;
              ratingsArray.push({ rating: parseFloat(key), percentage });
            }
          }
          for (const ratingObj of ratingsArray) {
            // console.log removed('RATING' + ratingObj.percentage);
            html +=
              ratingObj.rating +
              ' stars - ' +
              ratingObj.percentage.toFixed(0) +
              '%';
            html += '<br>';
          }
          ratingsContainer.innerHTML = html.slice(0, -2);
        });
    }

    self.newEducation = {}; // Initialize newEducation

    self.addEducationFormVisible = false; // Hide the education form initially

    // Function to toggle the visibility of the education form
    self.addEducation = function () {
      self.addEducationFormVisible = true;
    };

    // Function to cancel adding education and hide the form
    self.cancelEducationForm = function () {
      self.newEducation = {}; // Clear form fields
      self.addEducationFormVisible = false; // Hide the education form
    };
  },
]);

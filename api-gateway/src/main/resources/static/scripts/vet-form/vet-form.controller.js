"use strict";

angular.module("vetForm").controller("VetFormController", [
  "$http",
  "$state",
  "$stateParams",
  function ($http, $state, $stateParams) {
    var self = this;
    document.getElementById("loaderDiv").style.display = "none";

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

    var vetId = $stateParams.vetId || 0;
    if (!vetId || vetId === 0) {
      document.getElementById("title").innerHTML = "New Vet Sign Up";
      self.vet = {};
    } else {
      $http.get("api/gateway/vets/" + $stateParams.vetId).then(function (resp) {
        self.vet = resp.data;
        console.log(self.vet);
        document.getElementById("title").innerHTML = "Edit Vet";
        document.getElementById("firstName").value = self.vet.firstName;
        document.getElementById("lastName").value = self.vet.lastName;
        document.getElementById("email-info").style.display = "none";
        document.getElementById("vetResume").value = self.vet.resume;
        document.getElementById("user-info").style.display = "none";

        const specialties = self.vet.specialties;
        specialties.forEach((specs) => {
          document.getElementById(specs.name).checked = true;
        });

        const workdays = self.vet.workday;
        workdays.forEach((work) => {
          console.log(work);
          document.getElementById(work).checked = true;
        });

        let isAct = document.getElementsByClassName("isActiveRadio");
        if (self.vet.active) {
          isAct[0].checked = true;
        } else {
          isAct[1].checked = true;
        }
        let phoneNumber = self.vet.phoneNumber;
        let code = phoneNumber.substring(phoneNumber.length - 4);
        document.getElementById("phoneNumber").value = code;
      });
    }

    let uploadPhoto = function (vetId) {
      const fileInput = document.querySelector('input[id="photoVet"]');
      let vetPhoto = "";

      const file = fileInput.files[0];
      const reader = new FileReader();
      var image = {};
      reader.onloadend = () => {
        vetPhoto = reader.result.replace("data:", "").replace(/^.+,/, "");
        self.PreviewImage = vetPhoto;
        image = {
          name: file.name,
          type: "jpeg",
          photo: vetPhoto,
        };
        if (image.photo == null) {
          self.vet.photoDefault = true;
        }

        $http
          .post(`api/gateway/vets/${vetId}/photos/${image.name}`, image)
          .then(function (response) {
            console.log("VET ID: " + vetId);
            console.log("RESPONSE: " + JSON.stringify(response.data));
          })
          .catch(function (error) {
            console.error(error);
          });
      };
      reader.readAsDataURL(file);
    };

    let updatePhoto = function (vetId) {
      const fileInput = document.querySelector('input[id="photoVet"]');
      let vetPhoto = "";

      const file = fileInput.files[0];

      console.log(fileInput);
      const reader = new FileReader();
      var image = {};
      reader.onloadend = () => {
        vetPhoto = reader.result.replace("data:", "").replace(/^.+,/, "");
        self.PreviewImage = vetPhoto;
        image = {
          name: file.name,
          type: "jpeg",
          photo: vetPhoto,
        };
        console.log(image);

        $http
          .put(`api/gateway/vets/${vetId}/photos/${image.name}`, image)
          .then(function (response) {
            console.log("VET ID: " + vetId);
            console.log("RESPONSE: " + JSON.stringify(response.data));
          })
          .catch(function (error) {
            console.error(error);
          });
      };
      reader.readAsDataURL(file);
    };

    self.submitVetForm = function (vet = self.vet) {
      console.log("vet please: " + vet);
      document.getElementById("loaderDiv").style.display = "block";

      let specialtyList = document.getElementsByClassName("specialty");
      let selectedSpecialtiesList = [];
      let specialtiesStr = "[";
      for (let i = 0; i < specialtyList.length; i++) {
        if (specialtyList[i].checked) {
          selectedSpecialtiesList.push(specialtyList[i]);
        }
      }
      for (let i = 0; i < selectedSpecialtiesList.length; i++) {
        if (selectedSpecialtiesList[i].checked) {
          specialtiesStr += selectedSpecialtiesList[i].value;
          if (i !== selectedSpecialtiesList.length - 1) specialtiesStr += ", ";
        }
      }
      specialtiesStr += "]";
      if (specialtiesStr === "[]") {
        document.getElementById("loaderDiv").style.display = "none";
        self.showConfirmationModal(
          "Validation Error",
          "vet should have at least one specialty: " + specialtiesStr,
          null,
        );
        return;
      }

      var id = self.vet.vetId;
      const specialties = JSON.parse(specialtiesStr);
      vet.specialties = specialties;

      let workdayList = document.getElementsByClassName("workday");
      let selectedWorkdayList = [];
      for (let i = 0; i < workdayList.length; i++) {
        if (workdayList[i].checked) {
          selectedWorkdayList.push(workdayList[i].value);
        }
      }
      let workdaysArray = selectedWorkdayList;

      if (workdaysArray.length === 0) {
        document.getElementById("loaderDiv").style.display = "none";
        self.showConfirmationModal(
          "Validation Error",
          "vet should have at least one workday: " + workdaysArray,
          null,
        );
        return;
      }
      vet.workday = workdaysArray;

      vet.firstName = document.getElementById("firstName").value;
      var namePattern = /^[a-zA-Z -]+/;
      if (!namePattern.test(vet.firstName)) {
        document.getElementById("loaderDiv").style.display = "none";
        self.showConfirmationModal(
          "Validation Error",
          "first name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " +
            vet.firstName,
          null,
        );
        return;
      }
      if (vet.firstName.length > 30 || vet.firstName.length < 2) {
        document.getElementById("loaderDiv").style.display = "none";
        self.showConfirmationModal(
          "Validation Error",
          "first name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " +
            vet.firstName,
          null,
        );
        return;
      }

      vet.lastName = document.getElementById("lastName").value;
      if (!namePattern.test(vet.lastName)) {
        document.getElementById("loaderDiv").style.display = "none";
        self.showConfirmationModal(
          "Validation Error",
          "last name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " +
            vet.lastName,
          null,
        );
        return;
      }
      if (vet.lastName.length > 30 || vet.lastName.length < 2) {
        document.getElementById("loaderDiv").style.display = "none";
        self.showConfirmationModal(
          "Validation Error",
          "last name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " +
            vet.lastName,
          null,
        );
        return;
      }

      let basePhoneNumber = "(514)-634-8276 #";
      let inputPhoneNumber = document.getElementById("phoneNumber").value;
      if (inputPhoneNumber.length !== 4) {
        document.getElementById("loaderDiv").style.display = "none";
        self.showConfirmationModal(
          "Validation Error",
          "phoneNumber length not equal to 4: " + vet.phoneNumber,
          null,
        );
        return;
      }
      vet.phoneNumber = basePhoneNumber + inputPhoneNumber;

      vet.resume = document.getElementById("vetResume").value;
      if (vet.resume.length < 10) {
        document.getElementById("loaderDiv").style.display = "none";
        self.showConfirmationModal(
          "Validation Error",
          "resume should be minimum 10 characters: " + vet.resume,
          null,
        );
        return;
      }

      let isAct = document.getElementsByClassName("isActiveRadio");
      vet.active = isAct[0].checked;

      let photoInput = document.getElementById("photoVet");
      if (photoInput.files.length > 0) {
        vet.photoDefault = false;
      } else {
        vet.photoDefault = true;
      }
      var req;
      if (id) {
        req = $http.put("api/gateway/vets/" + vetId, vet);

        req.then(
          function (response) {
            var result = response.data;
            console.log("Response data:", result);
            console.log("Response vet id ", result.vetId);
            updatePhoto(result.vetId);
            console.log(result.vetId);
            $state.go("vets");
          },
          function (response) {
            document.getElementById("loaderDiv").style.display = "none";
            let error = "Invalid vet fields";
            $state.go("vets");
            self.showConfirmationModal("Error", error, null);
            console.error(error);
          },
        );
      } else {
        req = $http.post("api/gateway/users/vets", {
          username: vet.username,
          password: vet.password,
          email: vet.email,
          vet: vet,
        });
        console.log(self.vet);
        console.log(self.vet.photoDefault);

        req.then(
          function (response) {
            var result = response.data;
            console.log("Response data:", result);
            console.log("Response vet id ", result.vetId);
            uploadPhoto(result.vetId);
            $state.go("vets");
          },
          function (response) {
            document.getElementById("loaderDiv").style.display = "none";
            let error = "Invalid vet profile picture";
            $state.go("vets");
            self.showConfirmationModal("Error", error, null);
            console.error(error);
          },
        );
      }
    };
  },
]);

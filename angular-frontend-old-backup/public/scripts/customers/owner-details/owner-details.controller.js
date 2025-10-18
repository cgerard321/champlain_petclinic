angular
  .module('ownerDetails')
  .controller('OwnerDetailsController', OwnerDetailsController);

OwnerDetailsController.$inject = [
  '$http',
  '$state',
  '$stateParams',
  '$scope',
  '$timeout',
  '$q',
];

function OwnerDetailsController(
  $http,
  $state,
  $stateParams,
  $scope,
  $timeout,
  $q
) {
  var vm = this; // Use 'vm' (short for ViewModel) instead of 'self'

  // Initialize properties
  vm.owner = {};
  vm.pet = {};
  vm.pets = [];

  // Function to get pet type name based on petTypeId
  vm.getPetTypeName = function (petTypeId) {
    switch (petTypeId) {
      case '1':
        return 'Cat';
      case '2':
        return 'Dog';
      case '3':
        return 'Lizard';
      case '4':
        return 'Snake';
      case '5':
        return 'Bird';
      case '6':
        return 'Hamster';
      default:
        return 'Unknown';
    }
  };

  vm.getBirthday = function (birthday) {
    if (birthday) {
      var date = new Date(birthday);
      var timezoneOffset = date.getTimezoneOffset() * 60000;
      date = new Date(date.getTime() - timezoneOffset);
      var year = date.getFullYear();
      var month = (date.getMonth() + 1).toString().padStart(2, '0'); // Months are zero-based, so we add 1
      var day = date.getDate().toString().padStart(2, '0');
      return year + ' / ' + month + ' / ' + day;
    } else {
      return '';
    }
  };

  // Fetch owner data
  $http
    .get('api/gateway/owners/' + $stateParams.ownerId)
    .then(function (resp) {
      vm.owner = resp.data;
      // console.log removed(vm.owner);

      vm.owner.pets.forEach(function (pet) {
        pet.isActive = pet.isActive === 'true';
      });
    })
    .catch(function (error) {
      console.error('Error fetching owner data:', error);
    });

  // Fetch associated pets and their details
  $http
    .get(`api/gateway/owners/${$stateParams.ownerId}/pets`)
    .then(function (response) {
      // Split the response by newline characters to get individual pet objects
      var petResponses = response.data.split('\n');
      // Parse each pet response as JSON, remove the "data:" prefix, and trim any leading/trailing whitespace
      var petObjects = petResponses.map(function (petResponse) {
        // Remove the "data:" prefix and trim any leading/trailing whitespace
        var trimmedResponse = petResponse.replace(/^data:/, '').trim();
        // console.log removed('Trimmed results: ', trimmedResponse);

        // Check if the trimmed response is empty
        if (!trimmedResponse) {
          return null; // Skip empty responses
        } else {
          vm.pets = trimmedResponse;
        }

        try {
          return JSON.parse(trimmedResponse);
        } catch (error) {
          console.error('Error parsing pet response:', error);
          return null;
        }
      });

      // Filter out any parsing errors (null values)
      petObjects = petObjects.filter(function (pet) {
        return pet !== null;
      });

      // Assuming that each pet has a 'petId' property, you can create an array of promises to fetch detailed pet data
      var petPromises = petObjects.map(function (pet) {
        return $http.get(`api/gateway/pets/${pet.petId}`);
      });

      return $q.all(petPromises);
    })
    .then(function (responses) {
      vm.pets = responses.map(function (response) {
        return response.data;
      });
      // console.log removed('Pet Array:', vm.pets);
    })
    .catch(function (error) {
      console.error('Error fetching pet data:', error);
    });

  // Toggle pet's active status
  vm.toggleActiveStatus = function (petId) {
    return $http
      .get('api/gateway/pets/' + petId + '?_=' + new Date().getTime(), {
        headers: { 'Cache-Control': 'no-cache' },
      })
      .then(function (resp) {
        // console.log removed('Pet id is ' + petId);
        // console.log removed(resp.data);
        vm.pet = resp.data;
        // console.log removed('Pet id is ' + vm.pet.petId);
        // console.log removed(vm.pet);
        // console.log removed('=====================================');
        // console.log removed(resp.data);
        // console.log removed('Active status before is:' + vm.pet.isActive);
        vm.pet.isActive = vm.pet.isActive === 'true' ? 'false' : 'true';
        // console.log removed('Active status after is:' + vm.pet.isActive);

        return $http.patch(
          'api/gateway/pet/' + petId,
          {
            isActive: vm.pet.isActive,
          },
          { headers: { 'Cache-Control': 'no-cache' } }
        );
      })
      .then(function (resp) {
        // console.log removed('Pet active status updated successfully');
        vm.pet = resp.data;
        // Schedule a function to be executed during the next digest cycle
        $scope.$evalAsync();
      })
      .catch(function (error) {
        console.error('Error updating pet active status:', error);
        // Handle the error appropriately
      });
  };

  // Watch the pet.isActive property
  $scope.$watch('pet.isActive', function (newVal, oldVal) {
    if (newVal !== oldVal) {
      // The pet.isActive property has changed, update the UI
      $scope.$apply();
    }
  });

  vm.deletePet = function (petId) {
    var config = {
      headers: {
        'Content-Type': 'application/json',
      },
    };

    $http
      .delete('api/gateway/pets/' + petId, config)
      .then(function () {
        // console.log removed('Pet deleted successfully');

        /*  $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
                      self.owner = resp.data;
                  });
                 */

        vm.owner.pets = vm.owner.pets.filter(function (pet) {
          return pet.petId !== petId;
        });

        $scope.$applyAsync();
        // Handle the success appropriately
      })
      .catch(function (error) {
        console.error('Error deleting pet:', error);
        // Handle the error appropriately
      });
  };
}

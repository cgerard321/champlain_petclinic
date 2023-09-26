angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$state', '$stateParams', '$scope', '$timeout', function ($http, $state, $stateParams, $scope, $timeout) {
        var self = this;
        self.owner = {};
        self.pet = {};

        $http.get('api/gateway/owners/' + $stateParams.ownerId, { headers: { 'Cache-Control': 'no-cache' } }).then(function (resp) {
            console.log("Owner id is " + $stateParams.ownerId)
            console.log(resp.data)
            self.owner = resp.data;

        });


        self.toggleActiveStatus = function (petId) {
            $http.get('api/gateway/pets/' + petId + '?_=' + new Date().getTime(), { headers: { 'Cache-Control': 'no-cache' } }).then(function (resp) {
                console.log("Pet id is " + petId)
                console.log(resp.data)
                self.pet = resp.data;
                console.log("Pet id is " + self.pet.petId)
                console.log(self.pet)
                console.log("=====================================")
                console.log(resp.data)
                console.log("Active status before is:" + self.pet.isActive)
                self.pet.isActive = self.pet.isActive === "true" ? "false" : "true";
                console.log("Active status after is:" + self.pet.isActive)

                $http.patch('api/gateway/pet/' + petId, {
                    isActive: self.pet.isActive
                }, { headers: { 'Cache-Control': 'no-cache' } }).then(function (resp) {
                    console.log("Pet active status updated successfully");
                    self.pet = resp.data;
                    $timeout(); // Manually trigger the $digest cycle to update the UI
                }).catch(function (error) {
                    console.error("Error updating pet active status:", error);
                    // Handle the error appropriately
                });
            });
        };
    }]);

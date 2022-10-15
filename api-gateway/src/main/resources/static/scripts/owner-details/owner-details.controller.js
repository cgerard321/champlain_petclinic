'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;

        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
        });

        $http.get('api/gateway/owners/photo/' + $stateParams.ownerId).then(function (resp) {
            self.ownerPhoto = resp.data;
        });

        const fileInput = document.querySelector('input[id="photoOwner"]');
        let ownerPhoto = "";
        fileInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            const reader = new FileReader();
            reader.onloadend = () => {
                ownerPhoto = reader.result
                    .replace('data:', '')
                    .replace(/^.+,/, '');
                self.PreviewImage = ownerPhoto;
                var image = {
                    name: uuidv4(),
                    type: "jpeg",
                    photo: ownerPhoto
                };

                var test = $http.post('api/gateway/owners/photo/' + $stateParams.ownerId, image);
                console.log(test);

            };
            reader.readAsDataURL(file);
        });

        self.init = function (id){
            $http.get('api/gateway/owners/' + $stateParams.ownerId + '/pet/photo/' + id).then(function (resp) {
                self.petPhoto = resp.data;
            });
        }

        function uuidv4() {
            return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
                (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
            );
        };

    }]);


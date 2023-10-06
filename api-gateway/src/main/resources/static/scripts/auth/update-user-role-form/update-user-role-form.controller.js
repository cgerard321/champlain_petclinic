$scope.updateUserRole = function (userid) {
    $http.patch('api/gateway/users/' + userid, {
        headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
        .then(function () {
            $http.get('api/gateway/users', {
                headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
                .then(function (resp) {
                    self.users = resp.data;
                });
        });
};
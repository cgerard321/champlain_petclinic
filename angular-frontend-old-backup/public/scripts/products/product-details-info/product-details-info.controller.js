angular
  .module('productDetailsInfo')
  .controller('ProductDetailsInfoController', [
    '$http',
    '$state',
    '$stateParams',
    function ($http, $state, $stateParams) {
      var self = this;
      self.product = {}; // Initialize self.product
      var productId = $stateParams.productId;

      $http
        .get('/api/gateway/products/' + productId)
        .then(function (resp) {
          // Handle the response data for the specific product
          var product = resp.data;
          //// console.log removed("Product found:", product);
          self.product = product; // Update the product data in your controller

          //fetch image
          $http
            .get('api/gateway/images/' + product.imageId)
            .then(function (imageResp) {
              if (imageResp.data === '') {
                // console.log removed('no image found');
                return;
              }
              self.product.imageData = imageResp.data.imageData;
              self.product.imageType = imageResp.data.imageType;
            })
            .catch(function (err) {
              console.error('Error fetching image: ', err);
            });
        })
        .catch(function (error) {
          // Handle errors if the product is not found or other issues
          console.error('Error fetching product:', error);
        });
    },
  ]);

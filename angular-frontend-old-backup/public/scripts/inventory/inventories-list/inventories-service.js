// inventory.service.js

angular
  .module('inventoriesService', [])
  .service('InventoryService', function () {
    var inventoryId;

    // Setter method to set the inventoryId
    this.setInventoryId = function (passedId) {
      inventoryId = passedId;
    };

    // Getter method to retrieve the inventoryId
    this.getInventoryId = function () {
      return inventoryId;
    };
  });

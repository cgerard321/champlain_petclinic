'use strict';

// Configure a module for the bill history page
angular.module('billHistory') // Give the module a name
    .component('billHistory', { // Use the component method to add various scripts and links to a specified module
        templateUrl: 'scripts/bill/bill-history.template.html', // Add in a page template to append to (bill-history.template.html)
        controller: 'BillHistoryController' // Add a controller to the module which will tie a HTTP protocol to the page for specific methods
    });

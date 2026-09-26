'use strict';


angular.module('productForm')
    .component('productForm', {
        templateUrl: 'scripts/product-form/product-form.template.html',
        controller: 'ProductFormController',
        bindings: {
            modalClose: '&close',
            modalDismiss: '&dismiss'
        }
    });

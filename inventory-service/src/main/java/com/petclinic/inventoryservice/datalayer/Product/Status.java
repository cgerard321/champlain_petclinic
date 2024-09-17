package com.petclinic.inventoryservice.datalayer.Product;

public enum Status {

    RE_ORDER,
    OUT_OF_STOCK,
    AVAILABLE

    //Need to put this for re-order, disposable, expired
    //should be based on the quantity, if the quantity is less than 10, then the message should be re-order
    // and so on
    // needs also to copy teh status inside the inventory api gateway
    // the teacher said that I don't need to put the status as a value inside the database
    //and do the front end after that
    // do the test inside the inventory service. Like this I can see if the status is working as it should

    // -----------------

    // tests are done, need to finish the test today and focus on front end after
    //change some enums, when it is at 0 == to out of stock

}

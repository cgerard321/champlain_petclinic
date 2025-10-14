package com.petclinic.customersservice.domainclientlayer;

interface FilesServiceRethrower {
    Throwable rethrow(Throwable t, String context);
}

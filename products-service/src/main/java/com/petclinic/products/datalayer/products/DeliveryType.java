package com.petclinic.products.datalayer.products;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum DeliveryType {
    DELIVERY,
    PICKUP,
    DELIVERY_AND_PICKUP,
    NO_DELIVERY_OPTION
}


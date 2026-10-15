package com.petclinic.cartsservice.dataaccesslayer;

public enum ShippingMethod {
    PICKUP(0.00),
    STANDARD(9.99),
    EXPRESS(19.99);

    private final double cost;

    ShippingMethod(double cost) {
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }
}

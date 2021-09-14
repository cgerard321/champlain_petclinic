#!/usr/bin/env bash

spring init \
--boot-version=2.3.2.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=billing-service \
--package-name=com.petclinic.billing \
--groupId=com.petclinic.billing \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
billing-service


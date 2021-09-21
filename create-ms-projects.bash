#!/usr/bin/env bash

spring init \
--boot-version=2.3.2.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=customers-service \
--package-name=com.petclinic.customers \
--groupId=com.petclinic.customers \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
customers-service

spring init \
--boot-version=2.3.2.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=vets-service \
--package-name=com.petclinic.vets \
--groupId=com.petclinic.vets \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
vets-service

spring init \
--boot-version=2.3.2.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=visits-service \
--package-name=com.petclinic.visits \
--groupId=com.petclinic.visits \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
visits-service

spring init \
--boot-version=2.3.2.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=api-gateway \
--package-name=com.petclinic.api-gateway \
--groupId=com.petclinic.api-gateway \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
api-gateway

spring init \
--boot-version=2.3.2.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=auth-service \
--package-name=com.petclinic.auth \
--groupId=com.petclinic.auth \
--dependencies=actuator,webflux,security \
--version=1.0.0-SNAPSHOT \
auth-service


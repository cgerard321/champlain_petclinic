# Running the project

Back to [Main page](../README.md)

<!-- TOC -->
* [Running the project](#running-the-project)
  * [.env](#env)
  * [Docker - Run docker-compose with ALL SERVICES and ALL PORTALS](#docker---run-docker-compose-with-all-services-and-all-portals)
  * [Docker - Run docker-compose and EXCLUDE the Customer Portal](#docker---run-docker-compose-and-exclude-the-customer-portal)
  * [Docker - Run docker-compose and EXCLUDE the Employee Portal](#docker---run-docker-compose-and-exclude-the-employee-portal)
  * [Docker - Run docker-compose and EXCLUDE both Portals](#docker---run-docker-compose-and-exclude-both-portals)
  * [Bring up Frontend](#bring-up-frontend)
<!-- TOC -->

Once you have cloned the repo (see the setup instructions below), you need to do the following:

## .env
In the project's top-level folder, you will need to create a file called .env. It will contain many properties used
by a variety of services.

My students: I'll provide this to you.

Note: you must also add a different `.env` file to the `petclinic-frontend` folder. See [Environment Variables Setup for Frontend Application](environment.md#environment-variables-setup-for-frontend-application).


## Docker - Run docker-compose with ALL SERVICES and ALL PORTALS
Must be used prior to issuing a PR.
```
docker-compose up --build
```

## Docker - Run docker-compose and EXCLUDE the Customer Portal
For building all backend services AND the Employee Portal. Use this when actively
working on the **Customer** Portal.
```
docker-compose -f docker-compose.yml -f docker-compose.exclude-petclinic.yml up --build
```

## Docker - Run docker-compose and EXCLUDE the Employee Portal
For building all backend services AND the Customer Portal. Use this when actively
working on the **Employee** Portal.
```
docker-compose -f docker-compose.yml -f docker-compose.exclude-employee.yml up --build
```

## Docker - Run docker-compose and EXCLUDE both Portals
For building all backend services only. Use this when actively
working on both the **Customer AND Employee** Portals.
```
docker-compose -f docker-compose.yml -f docker-compose.exclude-petclinic.yml -f docker-compose.exclude-employee.yml up --build
```

To learn more about how docker profiles are used in general, I strongly encourage a short read on how docker profiles function [here](https://docs.docker.com/compose/how-tos/profiles/).
## Bring up Frontend
React frontend :
```
localhost:3000/
```
Until it is fixed, to login on React frontend, use need to enter the following URI:
```
localhost:3000/users/login
```
employee-frontend :
```
localhost:4200/
```

Old Angular frontend:
```
localhost:8080/
```

Running employee-frontend locally (development mode with auto-refresh):
```
cd employee-frontend
npm install
npm run dev
```

Running React frontend locally (development mode with auto-refresh):
```
cd petclinic-frontend
npm install
npm run dev
```

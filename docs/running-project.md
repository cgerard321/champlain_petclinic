# Running the project

Back to [Main page](../README.md)

<!-- TOC -->
* [Running the project](#running-the-project)
  * [mailer.env](#mailerenv)
  * [Docker Profile (for running with docker-compose with ALL SERVICES)](#docker-profile-for-running-with-docker-compose-with-all-services)
  * [Docker Profile (for running with docker-compose with all services but NO FRONTEND)](#docker-profile-for-running-with-docker-compose-with-all-services-but-no-frontend)
  * [Bring up Frontend](#bring-up-frontend)
<!-- TOC -->

Once you have cloned the repo (see the setup instructions below), you need to do the following:

## mailer.env
In the project's top-level folder, you will need to create a file called mailer.env. It will contain SMTP_PASS property set to the hashed password of the mailing service you will be using.
My students: I'll provide this to you.


## Docker Profile (for running with docker-compose with ALL SERVICES)
Must be used prior to issuing a PR and in Sprint Reviews.
```
docker-compose --profile fe build
docker-compose --profile fe up -d
docker-compose logs -f

or

docker-compose --profile fe up --build
```

## Docker Profile (for running with docker-compose with all services but NO FRONTEND)
This can be used during development to avoid having to rebuild everything whenever you make a change to the frontend.
```
docker-compose build
docker-compose up -d
docker-compose logs -f

or

docker-compose up --build
```
To learn more about how docker profiles are used in general, I strongly encourage a short read on how docker profiles function [here](https://docs.docker.com/compose/how-tos/profiles/).
## Bring up Frontend
React frontend:
```
localhost:3000/
```
Until it is fixed, to login on React frontend, use need to enter the following URI:
```
localhost:3000/users/login
```
Angular frontend:
```
localhost:8080/
```
In terminal:

Check database contents (did the script run)
```
winpty docker-compose exec mysql3 mysql -uuser -p customers-db -e "select * from owners"
winpty docker-compose exec mysql3 mysql -uuser -p customers-db -e "select * from pets"
winpty docker-compose exec mysql3 mysql -uuser -p customers-db -e "select * from types"
```
When all docker containers are up, test with curl:
```
curl localhost:8080/api/gateway/customer/owners | jq
curl localhost:8080/api/gateway/vet/vets | jq
```

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
docker-compose build
docker-compose up -d
docker-compose logs -f

or

docker-compose up --build
```

## Docker Profile (for running with docker-compose with all services but NO FRONTEND)
This can be used during development to avoid having to rebuild everything whenever you make a change to the frontend.
```
docker-compose -f docker-compose_no_FE.yml build
docker-compose -f docker-compose_no_FE.yml up -d
docker-compose logs -f

or

docker-compose -f docker-compose_no_FE.yml up --build
```
## Bring up Frontend

### React Frontend (Standalone Development)
```bash
cd petclinic-frontend
npm install
npm run dev
```
Access at: `localhost:3000/`
Until it is fixed, to login on React frontend, use need to enter the following URI:
```
localhost:3000/users/login
```

### Angular Frontend (Standalone Development)
```bash
cd angular-frontend
npm install
npm run dev
```
Access at: `localhost:8082/`

### Running Frontends with Backend
1. Start backend services (without frontends):
```bash
docker-compose -f docker-compose_no_FE.yml up --build
```

2. Start frontend in separate terminal:
```bash
# For React
cd petclinic-frontend && npm run dev

# For Angular  
cd angular-frontend && npm run dev
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

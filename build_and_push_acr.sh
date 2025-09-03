#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

ACR_URL="champlainpetclinic.azurecr.io"
TAG="latest" # You can change this tag if needed, e.g., to a version number

echo "Logging into ACR: $ACR_URL (ensure you've already run 'az acr login --name champlainpetclinic')"

# --- Service: petclinicFrontend ---
SERVICE_NAME="petclinic-frontend"
SERVICE_DIR="./petclinic-frontend"
DOCKERFILE_PATH="${SERVICE_DIR}/Dockerfile"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} -f ${DOCKERFILE_PATH} ${SERVICE_DIR} --build-arg BUILD_MODE=development
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: visits-service-new ---
SERVICE_NAME="visits-service-new"
SERVICE_DIR="./visits-service-new"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: inventory-service ---
SERVICE_NAME="inventory-service"
SERVICE_DIR="./inventory-service"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: vet-service ---
SERVICE_NAME="vet-service"
SERVICE_DIR="./vet-service"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: customers-service-reactive ---
SERVICE_NAME="customers-service-reactive"
SERVICE_DIR="./customers-service-reactive"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: api-gateway ---
SERVICE_NAME="api-gateway"
SERVICE_DIR="./api-gateway"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: auth-service ---
SERVICE_NAME="auth-service"
SERVICE_DIR="./auth-service"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: billing-service ---
SERVICE_NAME="billing-service"
SERVICE_DIR="./billing-service"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: mailer-service ---
SERVICE_NAME="mailer-service"
SERVICE_DIR="./mailer-service"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: emailing-service ---
SERVICE_NAME="emailing-service"
SERVICE_CONTEXT_DIR="./emailing-service"
DOCKERFILE_PATH="${SERVICE_CONTEXT_DIR}/emailing-service/Dockerfile" # Path to Dockerfile relative to root
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} -f ${DOCKERFILE_PATH} ${SERVICE_CONTEXT_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: products-service ---
SERVICE_NAME="products-service"
SERVICE_DIR="./products-service"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

# --- Service: cart-service ---
SERVICE_NAME="cart-service"
SERVICE_DIR="./cart-service"
echo "Building ${SERVICE_NAME}..."
docker build -t ${SERVICE_NAME}:${TAG} ${SERVICE_DIR}
echo "Tagging ${SERVICE_NAME} for ACR..."
docker tag ${SERVICE_NAME}:${TAG} ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "Pushing ${SERVICE_NAME} to ACR..."
docker push ${ACR_URL}/${SERVICE_NAME}:${TAG}
echo "${SERVICE_NAME} pushed successfully."
echo "------------------------------------"

echo "All specified services built, tagged, and pushed to ${ACR_URL}"
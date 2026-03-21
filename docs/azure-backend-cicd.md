# Azure Backend CI

This repo now builds each backend service in GitHub Actions and pushes the Docker image to Azure Container Registry (ACR).
The workflows avoid `azure/login` and `az containerapp` commands so they can run from a limited Azure student account.

## What the workflows do

- Build the changed Spring service with Maven.
- Optionally run SonarCloud on push if `SONAR_TOKEN` exists.
- Log in to ACR directly with `docker login`.
- Build and push a Docker image to Azure Container Registry.

## Required GitHub repository secrets

- `ACR_LOGIN_SERVER`
  - Example: `artisanregistry.azurecr.io`
- `ACR_USERNAME`
  - The admin or service account username for your registry.
- `ACR_PASSWORD`
  - The matching password for the registry account.
- `SONAR_TOKEN`
  - Optional. Only needed if you want SonarCloud scans to keep running.

## Services covered

- `user-service`
- `listing-service`
- `order-service`
- `review-service`
- `api-gateway`

Each service now builds and pushes on the `fix/security` branch when files in that service change.

## Runtime configuration

The workflows no longer update Azure Container Apps for you. Configure these values directly in Azure Portal, Azure CLI, or any deployment tool that has the required RBAC:

- `SPRING_DATA_MONGODB_URI` on `user-service`, `listing-service`, `order-service`, and `review-service`
- `USER_SERVICE_URL` on `order-service`, `review-service`, and `api-gateway`
- `LISTING_SERVICE_URL` on `order-service` and `api-gateway`
- `ORDER_SERVICE_URL` on `api-gateway`
- `REVIEW_SERVICE_URL` on `api-gateway`

## Important

- These workflows only publish images. They do not create infrastructure or roll out a new Container App revision.
- Make sure each Azure service is already configured to pull images from your ACR if you still want runtime deployments.
- If you later get broader Azure permissions, you can add a separate deployment workflow for Container Apps.

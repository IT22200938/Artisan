# Azure Backend CI/CD

This repo now deploys each backend service to Azure Container Apps from GitHub Actions.

## What the workflows do

- Build the changed Spring service with Maven.
- Optionally run SonarCloud on push if `SONAR_TOKEN` exists.
- Log in to Azure with `azure/login`.
- Build and push a Docker image to Azure Container Registry.
- Update the matching Azure Container App to the new image.
- Refresh service env vars so the gateway and inter-service clients point at the current Container App FQDNs.
- Store MongoDB connection strings as Container App secrets instead of hardcoding them in workflow files.

## Required GitHub repository secrets

- `AZURE_CREDENTIALS`
  - Azure service principal JSON for GitHub Actions.
- `SPRING_DATA_MONGODB_URI`
  - Shared MongoDB Atlas connection string used by the Spring services.
- `SONAR_TOKEN`
  - Optional. Only needed if you want SonarCloud scans to keep running.

## Required GitHub repository variables

- `AZURE_RESOURCE_GROUP`
  - Example: `artisan-rg`
- `AZURE_ACR_NAME`
  - Example: `artisanregistry`

## Services covered

- `user-service`
- `listing-service`
- `order-service`
- `review-service`
- `api-gateway`

Each service has its own workflow file under `.github/workflows/` and automatically deploys on push to `main` or `master` when files in that service change.

## How service URLs are handled

The reusable workflow queries Azure for the live ingress FQDN of:

- `user-service`
- `listing-service`
- `order-service`
- `review-service`

It then injects the corresponding `https://...azurecontainerapps.io` URLs into the services that need them.

## Important

- These workflows update existing Container Apps. They do not create the Azure infrastructure for you.
- Make sure each Container App already exists and is configured to pull images from your ACR.
- Rotate any ACR passwords or MongoDB credentials that were shared in chat before you rely on this pipeline.

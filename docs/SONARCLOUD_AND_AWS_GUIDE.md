# SonarCloud & AWS Deployment Guide

Step-by-step guide for SonarCloud (DevSecOps), AWS ECS deployment, and security configuration.

---

# Part 1: SonarCloud (DevSecOps)

## 1.1 Create SonarCloud Account & Link GitHub

1. Go to [sonarcloud.io](https://sonarcloud.io/)
2. Click **Log in** → **Log in with GitHub**
3. Authorize SonarCloud to access your GitHub account
4. When prompted, choose **Only select repositories** and add your `Artisan` repo

## 1.2 Create Projects

**Option A: One project per service (recommended for 4 students)**

1. In SonarCloud dashboard, click **+** → **Analyze new project**
2. Select your GitHub org (e.g. `IT22200938`) and the **Artisan** repository
3. Click **Set Up** next to the repo
4. Choose **Create project manually**
5. **Project key:** `IT22200938_artisan-user-service` (use your org + service name)
6. **Display name:** `Artisan User Service`
7. **Main branch:** `main`
8. Click **Set Up**
9. Under **Other** → select **Maven**
10. Copy the **Token** shown (you won't see it again)

Repeat for:
- `IT22200938_artisan-listing-service`
- `IT22200938_artisan-order-service`
- `IT22200938_artisan-review-service`

**Option B: One project for the whole repo**

1. Create a single project: `IT22200938_artisan`
2. Use one token for all services
3. CI/CD will need to run SonarCloud once per build (monorepo analysis)

## 1.3 Add SONAR_TOKEN to GitHub Secrets

1. In your GitHub repo: **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. **Name:** `SONAR_TOKEN`
4. **Value:** Paste the token from SonarCloud
5. Click **Add secret**

If using one token for all services, this is enough. If you use separate projects per service, you can use the same token (it has access to all projects in your org) or create multiple secrets like `SONAR_TOKEN_USER`, etc. – for simplicity, one `SONAR_TOKEN` is fine.

## 1.4 Update CI/CD for SonarCloud

The workflows already have a SonarCloud step. Ensure the `sonar.projectKey` matches your SonarCloud project key. Check `.github/workflows/user-service-ci.yml` – it uses:

```
-Dsonar.projectKey=${{ github.repository_owner }}_artisan-user-service
```

If your GitHub username/org is `IT22200938`, the project key should be `IT22200938_artisan-user-service`. Adjust if needed.

## 1.5 Add sonar-project.properties (Alternative)

For more control, add `user-service/sonar-project.properties`:

```properties
sonar.projectKey=IT22200938_artisan-user-service
sonar.organization=IT22200938
sonar.sources=src/main/java
sonar.java.binaries=target/classes
sonar.exclusions=**/dto/**,**/model/**
```

This overrides command-line args. Create similar files for other services if needed.

## 1.6 Run a Scan

1. Push a change to `main` – the CI pipeline will run
2. The SonarCloud job runs after build
3. Go to SonarCloud → your project → **Bugs**, **Vulnerabilities**, **Code Smells**
4. Fix **Critical** and **High** issues first
5. Re-push to trigger a new scan

## 1.7 Common Fixes

| Issue | Fix |
|-------|-----|
| "Make this field final" | Add `final` to the field |
| "Use try-with-resources" | Wrap `InputStream`/`Connection` in try-with-resources |
| "Remove this unused import" | Delete unused imports |
| "Add a null check" | Add `Objects.requireNonNull()` or null check |
| "Use secure random" | Use `SecureRandom` instead of `Random` for security-sensitive code |

---

# Part 2: AWS Cloud Deployment

## 2.1 Prerequisites

- AWS account (free tier)
- AWS CLI installed: `aws --version`
- Configure AWS CLI: `aws configure` (Access Key, Secret Key, region e.g. `us-east-1`)

## 2.2 Create ECR Repositories

Run these in AWS CLI or use the Console:

```bash
# Set your AWS region
export AWS_REGION=us-east-1

# Create ECR repos (one per service)
aws ecr create-repository --repository-name artisan-user-service --region $AWS_REGION
aws ecr create-repository --repository-name artisan-listing-service --region $AWS_REGION
aws ecr create-repository --repository-name artisan-order-service --region $AWS_REGION
aws ecr create-repository --repository-name artisan-review-service --region $AWS_REGION
```

Or via Console: **ECR** → **Create repository** → Name: `artisan-user-service` (repeat for each).

Note the **URI** for each repo (e.g. `123456789.dkr.ecr.us-east-1.amazonaws.com/artisan-user-service`).

## 2.3 Add GitHub Secrets for ECR Push

1. GitHub repo → **Settings** → **Secrets and variables** → **Actions**
2. Add these secrets:

| Secret | Value |
|--------|-------|
| `AWS_ACCESS_KEY_ID` | Your AWS access key |
| `AWS_SECRET_ACCESS_KEY` | Your AWS secret key |
| `AWS_REGION` | e.g. `us-east-1` |

**IAM user for CI:** Create an IAM user with:
- `AmazonEC2ContainerRegistryFullAccess` (or minimal: ECR push)
- Programmatic access (Access Key + Secret Key)

## 2.4 Fix CI/CD Deploy Job

The deploy job in each workflow needs the JAR in the right place for Docker build. The current workflow downloads the artifact – the artifact path includes `user-service/target/`. After download, the file lands in the workspace. The Docker build runs in `user-service/` and expects `target/user-service-*.jar`. The download-artifact places files preserving the path – so we get `user-service/target/user-service-*.jar`. The `working-directory: user-service` means we're in user-service, but the artifact is in the parent. We need the jar inside user-service/target. Let me check the workflow – the upload path is `user-service/target/user-service-*.jar`. When downloaded, it creates `user-service/target/user-service-*.jar` in the workspace. So when we `working-directory: user-service` and run docker build, the context is user-service. The Dockerfile does `COPY target/user-service-*.jar` – so it expects the file at `user-service/target/user-service-*.jar`. The download puts it at `<workspace>/user-service/target/` which is correct. Good.

One issue: the deploy job might run before the login-ecr step if AWS secrets are not set. The workflow has `if: ${{ secrets.AWS_ACCESS_KEY_ID != '' }}` on the deploy steps – but when secrets are empty, the login-ecr step is skipped, and `steps.login-ecr.outputs.registry` would be undefined, causing the docker build to fail. The deploy job should only run when AWS is configured. The workflow structure looks OK – the docker build step also has the `if` condition. Good.

## 2.5 Create ECS Cluster

1. AWS Console → **ECS** → **Clusters** → **Create cluster**
2. **Cluster name:** `artisan-cluster`
3. **Infrastructure:** AWS Fargate (serverless)
4. Create

## 2.6 Create IAM Role for ECS Task Execution

1. **IAM** → **Roles** → **Create role**
2. **Trusted entity:** AWS service → **ECS** → **ECS Task**
3. Next
4. Attach policy: `AmazonECSTaskExecutionRolePolicy`
5. Create role, name: `artisan-ecs-task-execution-role`

## 2.7 Create Task Definition (User Service Example)

1. **ECS** → **Task definitions** → **Create new task definition**
2. **Task definition family:** `artisan-user-service`
3. **Launch type:** AWS Fargate
4. **Task role:** (optional, create if needed) or leave default
5. **Task execution role:** `artisan-ecs-task-execution-role`
6. **CPU:** 256
7. **Memory:** 512 MB
8. **Container:**
   - **Name:** `user-service`
   - **Image URI:** `123456789.dkr.ecr.us-east-1.amazonaws.com/artisan-user-service:latest` (use your ECR URI)
   - **Port mappings:** 8080
   - **Environment variables:**
     - `SPRING_DATA_MONGODB_URI` = (from Secrets Manager or env – see 2.10)
     - `JWT_SECRET` = (from Secrets Manager or env)
   - **Log configuration:** awslogs, create log group `artisan-user-service`
9. Create

Repeat similar task definitions for listing, order, review services.

## 2.8 Create Application Load Balancer (ALB)

1. **EC2** → **Load Balancers** → **Create load balancer**
2. Choose **Application Load Balancer**
3. **Name:** `artisan-alb`
4. **Scheme:** Internet-facing
5. **IP address type:** IPv4
6. **Network:** Default VPC
7. **Mappings:** Select at least 2 AZs
8. **Security group:** Create new or use existing – must allow **80** and **443** from `0.0.0.0/0` (or restrict later)
9. **Listeners:** HTTP:80
10. Create

## 2.9 Create Target Group

1. **EC2** → **Target Groups** → **Create target group**
2. **Target type:** IP (for Fargate)
3. **Target group name:** `artisan-user-service-tg`
4. **Protocol:** HTTP
5. **Port:** 8080
6. **VPC:** Default
7. **Health check path:** `/actuator/health` or `/swagger-ui.html` (or add a simple `/health` if your app has it – User Service may not have actuator; use `/swagger-ui.html` or create a health endpoint)

**Note:** User Service doesn't have Spring Actuator by default. Add it or use `/api/auth/login` as a health check (not ideal). Better: add `spring-boot-starter-actuator` to expose `/actuator/health`.

8. Create target group

## 2.10 Create ECS Service

1. **ECS** → **Clusters** → `artisan-cluster` → **Create service**
2. **Launch type:** Fargate
3. **Task definition:** `artisan-user-service`
4. **Service name:** `user-service`
5. **Desired tasks:** 1
6. **VPC:** Default
7. **Subnets:** Select at least 2
8. **Security group:** Create new – allow **8080** from ALB security group only (not 0.0.0.0/0 on 8080)
9. **Load balancer:** Application Load Balancer
10. **Load balancer:** `artisan-alb`
11. **Container to load balance:** `user-service:8080`
12. **Listener:** 80:HTTP
13. **Target group:** `artisan-user-service-tg`
14. Create service

## 2.11 Configure ALB Listener Rule

1. **EC2** → **Load Balancers** → `artisan-alb` → **Listeners**
2. Edit listener 80
3. Add rule: **path** `/user/*` → forward to `artisan-user-service-tg` (or use host-based routing if you have a domain)

For a simple setup, forward all traffic on 80 to the User Service target group first. For multiple services, use path-based routing:
- `/user/*` → User Service
- `/listing/*` → Listing Service
- etc.

Or use one ALB per service (simpler but more resources).

## 2.12 Store Secrets (MongoDB URI, JWT)

**Option A: ECS Task Definition – Environment variables (not for secrets)**

Use only for non-sensitive config. For production, use Secrets Manager.

**Option B: AWS Secrets Manager**

1. **Secrets Manager** → **Store a new secret**
2. **Type:** Other
3. Key/value: `SPRING_DATA_MONGODB_URI` = `mongodb+srv://...`
4. Name: `artisan/mongodb-uri`
5. Create

6. Repeat for `JWT_SECRET` → `artisan/jwt-secret`

7. In Task Definition, under **Secrets** (not Environment):
   - Name: `SPRING_DATA_MONGODB_URI`
   - ValueFrom: `arn:aws:secretsmanager:us-east-1:123456789:secret:artisan/mongodb-uri`

The task execution role needs `secretsmanager:GetSecretValue` permission for these secrets.

**Option C: Task Definition env (for demo only)**

For a quick demo, you can put values in plain env vars in the task definition. Not recommended for production.

## 2.13 Add Actuator Health Endpoint (Recommended)

Add to `user-service/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

In `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: when-authorized
```

Then ALB health check path: `/actuator/health`

## 2.14 Get Public URL

After the service is running:
1. **EC2** → **Load Balancers** → `artisan-alb` → **DNS name**
2. Example: `artisan-alb-123456789.us-east-1.elb.amazonaws.com`
3. Test: `http://<dns-name>/user/actuator/health` (if path routing) or `http://<dns-name>/actuator/health` (if default)

---

# Part 3: Security Setup

## 3.1 IAM Roles for ECS

**Task Execution Role** (pulls image, fetches secrets, writes logs):
- `AmazonECSTaskExecutionRolePolicy` – attached by default when you select the execution role
- For Secrets Manager: add inline policy or attach `SecretsManagerReadWrite` (or custom policy with `secretsmanager:GetSecretValue` on your secrets only)

**Task Role** (optional, for app-level AWS API calls):
- If your app doesn't call AWS APIs, leave default or use the execution role.

## 3.2 Security Groups

**ALB Security Group (`artisan-alb-sg`):**
| Type | Port | Source |
|------|------|--------|
| HTTP | 80 | 0.0.0.0/0 |
| HTTPS | 443 | 0.0.0.0/0 (if using SSL) |

**ECS Service Security Group (`artisan-ecs-sg`):**
| Type | Port | Source |
|------|------|--------|
| Custom TCP | 8080 | `artisan-alb-sg` (ALB security group ID only) |

This ensures only the ALB can reach your containers on 8080.

## 3.3 No Secrets in Code

- [ ] No MongoDB URI in `application.yml` – use `${SPRING_DATA_MONGODB_URI}`
- [ ] No JWT secret in code – use `${JWT_SECRET}` or Secrets Manager
- [ ] `.gitignore` includes `.env`
- [ ] No AWS keys in repo – use GitHub Secrets and IAM roles

## 3.4 Principle of Least Privilege

- ECS task execution role: only ECR pull, CloudWatch logs, Secrets Manager (for your specific secrets)
- GitHub Actions: use OIDC with AWS for better security (optional, advanced) instead of long-lived access keys

---

# Quick Checklist

## SonarCloud
- [ ] Account linked to GitHub
- [ ] Project(s) created
- [ ] `SONAR_TOKEN` in GitHub Secrets
- [ ] Push triggers scan
- [ ] Critical/High issues addressed

## AWS Deployment
- [ ] ECR repos created
- [ ] AWS secrets in GitHub
- [ ] CI/CD pushes images to ECR
- [ ] ECS cluster created
- [ ] Task definitions with correct image URIs
- [ ] ALB + target groups
- [ ] ECS services running
- [ ] Health checks passing
- [ ] Public URL accessible

## Security
- [ ] ECS security group allows 8080 from ALB only
- [ ] Secrets in Secrets Manager (or at least env vars, not in code)
- [ ] IAM roles follow least privilege
- [ ] No credentials in Git

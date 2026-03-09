# Global Artisan Marketplace

A microservices-based e-commerce platform for handmade crafts (jewelry, textiles, etc.) built for **CTSE Assignment (SE4010) – Cloud Computing**.

## Architecture

| Component | Port | Responsibility |
|-----------|------|----------------|
| **API Gateway** | 8084 | Single entry point, routes to all services |
| **User Service** | 8080 | Auth, profiles (buyer/seller) |
| **Listing Service** | 8081 | Product catalog, search |
| **Order Service** | 8082 | Cart, checkout, mock payment |
| **Review Service** | 8083 | Post-purchase reviews |

## Project Structure

```
Artisan/
├── api-gateway/           # API Gateway (Spring Cloud Gateway)
├── user-service/          # Auth, profiles
├── listing-service/      # Product catalog, stock
├── order-service/        # Cart, checkout
├── review-service/       # Post-purchase reviews
├── docs/api-contracts/   # OpenAPI specs
├── docker-compose.yml    # Run all services locally
└── .github/workflows/    # CI/CD per service
```

## Quick Start – All Services

### Option 1: Docker Compose (recommended)

**Local MongoDB:**
```bash
docker-compose up --build -d
```

**MongoDB Atlas:**
```bash
# 1. Copy .env.example to .env
cp .env.example .env

# 2. Edit .env – use Atlas URI without a database name (each service has its own):
#    SPRING_DATA_MONGODB_URI=mongodb+srv://user:pass@cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority

# 3. Run
docker-compose up --build -d
```

| Entry Point | URL |
|-------------|-----|
| **API Gateway** (all routes) | http://localhost:8084 |
| User (via gateway) | http://localhost:8084/api/auth/*, /api/users/* |
| Listing (via gateway) | http://localhost:8084/api/listings/* |
| Order (via gateway) | http://localhost:8084/api/orders/* |
| Review (via gateway) | http://localhost:8084/api/reviews/* |

Direct service URLs (for Swagger):
- User: http://localhost:8080/swagger-ui.html
- Listing: http://localhost:8081/swagger-ui.html
- Order: http://localhost:8082/swagger-ui.html
- Review: http://localhost:8083/swagger-ui.html

### Option 2: Run Individually (Maven)

Prerequisites: Java 17+, Maven, MongoDB (local or [MongoDB Atlas](https://www.mongodb.com/cloud/atlas))

**With MongoDB Atlas** (omit database name; each service uses its own):
```bash
export SPRING_DATA_MONGODB_URI="mongodb+srv://user:pass@cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority"
```

```bash
# Terminal 1 – User Service
cd user-service && mvn spring-boot:run

# Terminal 2 – Listing Service
cd listing-service && mvn spring-boot:run

# Terminal 3 – Order Service (requires User & Listing)
cd order-service && mvn spring-boot:run

# Terminal 4 – Review Service (requires User)
cd review-service && mvn spring-boot:run
```

## End-to-End Flow

1. **Register/Login** (User Service): `POST /api/auth/register`, `POST /api/auth/login`
2. **Create Listing** (Listing Service): `POST /api/listings`
3. **Add to Cart** (Order Service): `POST /api/orders/cart` with header `X-Buyer-Id: <userId>`
4. **Checkout** (Order Service): `POST /api/orders/checkout` with header `X-Buyer-Id: <userId>`
5. **Leave Review** (Review Service): `POST /api/reviews` (fetches user profile from User Service)

## Database per Service

Each microservice has its own MongoDB database (same cluster, different databases):

| Service | Database |
|---------|----------|
| User | `artisan_users` |
| Listing | `artisan_listings` |
| Order | `artisan_orders` |
| Review | `artisan_reviews` |

## Integration Points

| From | To | Purpose |
|------|-----|---------|
| Order | User | Validate buyer before cart/checkout |
| Order | Listing | Check stock, reduce stock on checkout |
| Review | User | Fetch `displayName`, `avatarUrl` for reviews |

## CI/CD

Each service has its own workflow (triggers on path changes):

- `user-service-ci.yml` → User Service
- `listing-service-ci.yml` → Listing Service
- `order-service-ci.yml` → Order Service
- `review-service-ci.yml` → Review Service

GitHub secrets for deploy: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `SONAR_TOKEN`

## Assignment Deliverables

- [x] All 4 microservices implemented
- [x] Docker & docker-compose
- [x] CI/CD pipelines
- [x] OpenAPI/Swagger per service
- [ ] Shared architecture diagram (create with group)
- [ ] Deploy to cloud (AWS ECS/EKS)
- [ ] Live integration demo

See **IMPLEMENTATION_GUIDE.md** for the full step-by-step guide.

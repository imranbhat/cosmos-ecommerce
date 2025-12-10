# OpenShift Production Deployment Walkthrough

This guide explains how to deploy the refactored microservices application to an OpenShift cluster using the generated K8s-native manifests.

## 1. Prerequisites
- OpenShift CLI (`oc`) installed and logged in.
- Maven installed (to rebuild the application).
- Docker/Podman (to build and push images).
- Access to an Image Registry.

## 2. Refactoring Summary
The application has been refactored to be **Kubernetes Native**:
- **Services Only**: `product-service`, `order-service`, `inventory-service`, `user-service`.
- **Removed**: `api-gateway` and `discovery-service` (Code deleted).
- **Service Discovery**: Handled by Kubernetes DNS.
- **External Access**: Managed by OpenShift Routes.
- **Configuration**: Managed via ConfigMaps and Secrets (Env Vars).

## 3. Build and Push Images
You must rebuild your JARs and Docker images, then push them to your registry.

```bash
# 1. Build JARs
mvn clean package -DskipTests

# 2. Build & Push Images (Repeat for all services)
# Replace 'your-registry' with your actual registry (e.g., image-registry.openshift-image-registry.svc:5000/myproject)
docker build -t your-registry/product-service:latest ./product-service
docker push your-registry/product-service:latest
# ... repeat for order-service, inventory-service, user-service
```

> [!IMPORTANT]
> **Update Manifests**: Edit `openshift/microservices.yaml` and update the `image:` fields to match your registry paths.

## 4. Deploy Infrastructure
Deploy the configurations and databases first.

```bash
oc apply -f openshift/config-maps.yaml
oc apply -f openshift/secrets.yaml
oc apply -f openshift/databases.yaml
```

Wait for databases to be ready:
```bash
oc get pods -l app=order-db
```

## 5. Deploy Microservices
Deploy the application services.

```bash
oc apply -f openshift/microservices.yaml
```

## 6. Expose Services
Create Routes to access the services from outside the cluster.

```bash
oc apply -f openshift/routes.yaml
```

Get the public URLs:
```bash
oc get routes
```

## 7. Verification
1.  **Check Pods**: Ensure all pods are `Running` and `Ready`.
    ```bash
    oc get pods
    ```
2.  **Test Connectivity**:
    Use the Route URL for `product-service` to fetch products.
    ```bash
    curl https://product-service-myproject.doman/products/1
    ```
3.  **Test Inter-Service**:
    Trigger an order creation. The `order-service` will use K8s DNS (`http://inventory-service:8080`) to contact `inventory-service`.
    ```bash
    curl -X POST https://order-service-myproject.domain/orders ...
    ```

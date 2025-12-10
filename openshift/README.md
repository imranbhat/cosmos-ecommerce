# OpenShift Manifests Explained

This directory contains the "Blueprints" (YAML files) that tell OpenShift how to run your application. Think of OpenShift/Kubernetes as a construction manager, and these files are the architectural drawings.

## 1. `config-maps.yaml` (The Configuration Board)
**What it is:** A shared place to store non-secret settings.
**What it does:** It holds variables like "Where is the database?" or "What URL does the inventory service have?".
**Why we use it:** So we can change settings without rebuilding our code. If the database hostname changes, we just update this file.
**Key Content:**
- `ORDER_DB_HOST`: The address of the order database.
- `INVENTORY_SERVICE_URL`: The internal address of the inventory service.

## 2. `secrets.yaml` (The Safe)
**What it is:** Like a ConfigMap, but for sensitive data.
**What it does:** Stores passwords and keys.
**Why we use it:** OpenShift encrypts this (or handles it securely) so your passwords aren't visible in plain text in the console logs.
**Key Content:**
- `POSTGRES_PASSWORD`: The password your services use to connect to the databases.

## 3. `databases.yaml` (The Storage Room)
**What it is:** Defines your database servers.
**What it does:**
- **Deployment**: Tells OpenShift to start a "Pod" (container) running PostgreSQL.
- **Service**: Gives that Pod a stable internal name (e.g., `order-db`) so other apps can find it even if the Pod restarts and gets a new IP address.
**Note**: We configured these as *ephemeral*, meaning if the Pod restarts, the data is wiped clean (good for testing, bad for real production).

## 4. `microservices.yaml` (The Application)
**What it is:** Defines your actual Java applications.
**What it does:**
- **Deployment**: Downloads your Docker image (e.g., `product-service:latest`) and runs it. It also injects the "ConfigMaps" and "Secrets" as environment variables so your code can read them.
- **Service**: Creates an internal phonebook entry (e.g., `product-service` on port 8080) so other microservices can talk to it purely by name.
- **Probes**: Little health checks that OpenShift runs to ask "Are you alive?" (Liveness) and "Are you ready to take traffic?" (Readiness).

## 5. `routes.yaml` (The Front Door)
**What it is:** Only available in OpenShift (standard K8s calls this "Ingress").
**What it does:** Exposes your internal Services to the outside internet.
**How to use it:** When you apply this, OpenShift gives you a public URL (like `https://product-service-myproject.openshiftapps.com`) that you can put in your browser or Postman.

---

## How to Apply Them (The "Go" Command)
You apply these files using the OpenShift CLI (`oc`). The order matters slightly (Config/DBs first, then Apps).

```bash
# 1. Setup Configs and Secrets
oc apply -f openshift/config-maps.yaml
oc apply -f openshift/secrets.yaml

# 2. Start Databases
oc apply -f openshift/databases.yaml

# 3. Start Microservices
oc apply -f openshift/microservices.yaml

# 4. Open the Front Door
oc apply -f openshift/routes.yaml
```

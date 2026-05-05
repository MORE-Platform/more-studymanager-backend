# More Studymanager Backend

## Development Setup

This repository contains a `docker-compose.yaml` that can be used to launch the required services
for local development:
```shell
docker compose up -d
```

The default settings in the `application.yaml` are set to use these local services.

### Configuring a local Auth-Server
If you're running a [local (development) Keycloak][keycloak-dev], you need to launch the Studymanager Backend
with the following additional environment-variables set:
```dotenv
OAUTH2_SERVER: http://localhost:8099/realms/More-Platform
OAUTH2_CLIENT_ID: study-manager
OAUTH2_CLIENT_SECRET: ''

MORE_FE_KC_SERVER: http://localhost:8099/
MORE_FE_KC_REALM: More-Platform
MORE_FE_KC_CLIENT_ID: study-manager
```

[keycloak-dev]: https://github.com/MORE-Platform/more-auth-keycloak#connect-the-applications-to-keycloak

### Login Token Configuration

The Login Token service can be configured via the following properties (environment variables):

```yaml
login-token:
  length: 8
  use-numbers: true
  use-letters: true
  encryption-key: ${LOGIN_TOKEN_ENCRYPTION_KEY:}
  salt-key: ${LOGIN_TOKEN_SALT_KEY:}
  hash-algorithm: ${LOGIN_TOKEN_HASH_ALGORITHM:SHA-256}
```

Note: The `encryption-key` and `salt-key` should be a hash.

## Planning & Documentation

* [Architecture Decision Records](docs/adr)

## Tagging & Deployment

To safely deploy new versions, we use a Git tagging strategy.

- **Tag Format:** `v<Major>.<Minor>.<Patch>` (e.g., `v1.0.1`)
- **Behavior:** When a tag matching the `v*.*.*` pattern is pushed, the GitHub Action pipeline automatically:
    1. Compiles and tests the code.
    2. Builds a Docker image.
    3. Tags the Docker image with the version from the Git tag (e.g.,
       `ghcr.io/more-platform/more-studymanager-backend:v1.0.1`).
    4. Publishes the image to GitHub Packages.

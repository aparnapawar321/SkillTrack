# SkillTrack Environment Variables

## Required Environment Variables

Before running the application, you must set the following environment variables:

### GitHub OAuth Configuration
```bash
export GITHUB_CLIENT_ID=your-github-client-id
export GITHUB_CLIENT_SECRET=your-github-client-secret
```

**How to get these values:**
1. Go to https://github.com/settings/developers
2. Click "New OAuth App"
3. Fill in:
   - Application name: `SkillTrack` (or your preferred name)
   - Homepage URL: `http://localhost:8080`
   - Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Click "Register application"
5. Copy the **Client ID** and **Client Secret**

### Optional Environment Variables

```bash
# JWT Configuration (has defaults, but you can override)
export JWT_SECRET=your-custom-jwt-secret-key

# SendGrid Email (if using email features)
export SENDGRID_API_KEY=your-sendgrid-api-key

# Admin User (has defaults)
export ADMIN_EMAIL=admin@yourcompany.com
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=your-secure-password
```

## Running the Application

### Option 1: Export variables in your shell
```bash
export GITHUB_CLIENT_ID=Ov23li1WhGMUbONpqRcW
export GITHUB_CLIENT_SECRET=4cd7bb992d57b465ac769cdfc9539d870383a1e5
./gradlew :skilltrack-api:bootRun
```

### Option 2: Create a `.env` file (recommended for local development)
Create a file named `.env` in the project root:
```bash
GITHUB_CLIENT_ID=Ov23li1WhGMUbONpqRcW
GITHUB_CLIENT_SECRET=4cd7bb992d57b465ac769cdfc9539d870383a1e5
```

Then load it before running:
```bash
source .env
./gradlew :skilltrack-api:bootRun
```

### Option 3: IntelliJ IDEA / IDE
1. Open Run Configuration
2. Add environment variables in the "Environment variables" field:
   ```
   GITHUB_CLIENT_ID=Ov23li1WhGMUbONpqRcW;GITHUB_CLIENT_SECRET=4cd7bb992d57b465ac769cdfc9539d870383a1e5
   ```

## Production Deployment

For production environments, use your platform's secret management:
- **AWS**: AWS Secrets Manager or Parameter Store
- **Azure**: Azure Key Vault
- **Google Cloud**: Secret Manager
- **Heroku**: Config Vars
- **Docker**: Environment variables in docker-compose or Kubernetes secrets

**NEVER commit your `.env` file or actual credentials to Git!**

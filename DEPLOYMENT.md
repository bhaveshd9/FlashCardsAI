# FlashCards AI - Deployment Guide

## Overview
This guide covers deploying the FlashCards AI application to various platforms. The application consists of:
- **Backend**: Spring Boot application (Java 17)
- **Frontend**: React.js application
- **Database**: MongoDB
- **AI Integration**: OpenAI API

## Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- MongoDB (local or cloud)
- OpenAI API key
- Git

## 1. Local Development Deployment

### Backend Setup
```bash
# Clone the repository
git clone <repository-url>
cd FlashCardsAI

# Set up environment variables
cp .env.sample .env
# Edit .env with your configuration

# Build and run backend
mvn clean package
java -jar target/flashcards-ai-1.0.0.jar
```

### Frontend Setup
```bash
cd frontend
npm install
npm start
```

### Environment Variables (.env)
```env
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/flashcards_ai
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=flashcards_ai

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRATION=86400000

# Email Configuration (for password reset)
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# OpenAI Configuration
OPENAI_API_KEY=your-openai-api-key
OPENAI_API_URL=https://api.openai.com/v1/chat/completions
```

## 2. Docker Deployment

### Create Dockerfile for Backend
```dockerfile
# Backend Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/flashcards-ai-1.0.0.jar app.jar
COPY .env .env

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

### Create Dockerfile for Frontend
```dockerfile
# Frontend Dockerfile
FROM node:16-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=0 /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
```

### Docker Compose Setup
```yaml
# docker-compose.yml
version: '3.8'

services:
  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    environment:
      MONGO_INITDB_DATABASE: flashcards_ai

  backend:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mongodb
    environment:
      - MONGODB_URI=mongodb://mongodb:27017/flashcards_ai
      - JWT_SECRET=your-jwt-secret
      - OPENAI_API_KEY=your-openai-key

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mongodb_data:
```

### Deploy with Docker
```bash
# Build and run
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## 3. Cloud Deployment Options

### Option A: Heroku

#### Backend Deployment
```bash
# Install Heroku CLI
# Create Heroku app
heroku create flashcards-ai-backend

# Set environment variables
heroku config:set MONGODB_URI=your-mongodb-atlas-uri
heroku config:set JWT_SECRET=your-jwt-secret
heroku config:set OPENAI_API_KEY=your-openai-key

# Deploy
git push heroku main
```

#### Frontend Deployment
```bash
# Create separate app for frontend
heroku create flashcards-ai-frontend

# Set buildpack
heroku buildpacks:set mars/create-react-app

# Deploy
git push heroku main
```

### Option B: AWS

#### Using AWS Elastic Beanstalk
```bash
# Install EB CLI
pip install awsebcli

# Initialize EB application
eb init flashcards-ai --platform java-17

# Create environment
eb create production

# Deploy
eb deploy
```

#### Using AWS ECS with Fargate
```bash
# Create ECS cluster
aws ecs create-cluster --cluster-name flashcards-ai

# Create task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json

# Create service
aws ecs create-service --cluster flashcards-ai --service-name backend --task-definition flashcards-ai:1
```

### Option C: Google Cloud Platform

#### Using Google App Engine
```yaml
# app.yaml
runtime: java17
service: flashcards-ai

env_variables:
  MONGODB_URI: "your-mongodb-uri"
  JWT_SECRET: "your-jwt-secret"
  OPENAI_API_KEY: "your-openai-key"
```

```bash
# Deploy
gcloud app deploy
```

### Option D: DigitalOcean

#### Using DigitalOcean App Platform
```yaml
# .do/app.yaml
name: flashcards-ai
services:
  - name: backend
    source_dir: /
    github:
      repo: your-username/flashcards-ai
      branch: main
    run_command: java -jar target/flashcards-ai-1.0.0.jar
    environment_slug: java
    instance_count: 1
    instance_size_slug: basic-xxs
    envs:
      - key: MONGODB_URI
        value: your-mongodb-uri
      - key: JWT_SECRET
        value: your-jwt-secret
      - key: OPENAI_API_KEY
        value: your-openai-key
```

## 4. Database Setup

### MongoDB Atlas (Recommended for Production)
1. Create account at [MongoDB Atlas](https://www.mongodb.com/atlas)
2. Create new cluster
3. Set up database access (username/password)
4. Set up network access (IP whitelist)
5. Get connection string
6. Update environment variables

### Local MongoDB
```bash
# Install MongoDB
# Ubuntu/Debian
sudo apt-get install mongodb

# macOS
brew install mongodb-community

# Start MongoDB
sudo systemctl start mongodb
# or
brew services start mongodb-community
```

## 5. SSL/HTTPS Setup

### Using Let's Encrypt (Free)
```bash
# Install Certbot
sudo apt-get install certbot

# Get certificate
sudo certbot certonly --standalone -d yourdomain.com

# Configure nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;
    
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 6. Monitoring and Logging

### Application Monitoring
```bash
# Add monitoring dependencies to pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Logging Configuration
```yaml
# application.yml
logging:
  level:
    com.flashcards: DEBUG
    org.springframework.web: INFO
  file:
    name: logs/flashcards-ai.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 7. Performance Optimization

### Backend Optimization
```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379

server:
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
```

### Frontend Optimization
```javascript
// Add to package.json
{
  "scripts": {
    "build": "GENERATE_SOURCEMAP=false react-scripts build"
  }
}
```

## 8. Security Considerations

### Environment Variables
- Never commit `.env` files to version control
- Use secrets management services in production
- Rotate API keys regularly

### CORS Configuration
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Rate Limiting
```java
@Configuration
public class RateLimitConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }
}
```

## 9. Backup and Recovery

### Database Backup
```bash
# MongoDB backup
mongodump --uri="mongodb://localhost:27017/flashcards_ai" --out=/backup

# Restore
mongorestore --uri="mongodb://localhost:27017/flashcards_ai" /backup
```

### Automated Backups
```bash
#!/bin/bash
# backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
mongodump --uri="your-mongodb-uri" --out="/backup/$DATE"
aws s3 sync /backup s3://your-backup-bucket
```

## 10. Troubleshooting

### Common Issues

#### Backend Won't Start
```bash
# Check logs
tail -f logs/flashcards-ai.log

# Check port availability
netstat -tulpn | grep :8080

# Check Java version
java -version
```

#### Frontend Build Fails
```bash
# Clear cache
npm cache clean --force
rm -rf node_modules package-lock.json
npm install

# Check Node.js version
node --version
```

#### Database Connection Issues
```bash
# Test MongoDB connection
mongo "your-mongodb-uri" --eval "db.runCommand('ping')"

# Check network connectivity
telnet your-mongodb-host 27017
```

### Health Checks
```bash
# Backend health
curl http://localhost:8080/actuator/health

# Frontend health
curl http://localhost:3000

# Database health
curl http://localhost:8080/api/test
```

## 11. Scaling Considerations

### Horizontal Scaling
- Use load balancers (AWS ALB, GCP Load Balancer)
- Implement session management with Redis
- Use CDN for static assets

### Vertical Scaling
- Increase server resources (CPU, RAM)
- Optimize database queries
- Implement caching strategies

## 12. Cost Optimization

### Cloud Cost Management
- Use spot instances for non-critical workloads
- Implement auto-scaling policies
- Monitor and optimize resource usage
- Use reserved instances for predictable workloads

### Resource Optimization
- Implement connection pooling
- Use efficient data structures
- Optimize images and assets
- Implement lazy loading

## Support

For deployment issues:
1. Check the logs first
2. Verify environment variables
3. Test connectivity between services
4. Review security group/firewall settings
5. Check resource limits and quotas

## Next Steps

After deployment:
1. Set up monitoring and alerting
2. Configure automated backups
3. Implement CI/CD pipelines
4. Set up staging environment
5. Plan for disaster recovery 
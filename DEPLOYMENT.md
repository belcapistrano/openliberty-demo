# AWS ECS Fargate Deployment Guide

This guide provides step-by-step instructions for deploying the Open Liberty Demo application to AWS ECS Fargate.

## Prerequisites

### 1. AWS Account Setup
- AWS Account with appropriate permissions
- AWS CLI installed and configured
- Docker installed and running

### 2. Required AWS Permissions
Your AWS user/role needs permissions for:
- ECS (Elastic Container Service)
- ECR (Elastic Container Registry)
- CloudFormation
- EC2 (VPC, Security Groups, Load Balancer)
- IAM (for service roles)
- CloudWatch (for logging)

### 3. Installation Requirements
```bash
# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Configure AWS CLI
aws configure

# Install Docker (if not already installed)
# Follow instructions at https://docs.docker.com/get-docker/
```

## Architecture Overview

```
Internet → ALB → ECS Fargate Service → Multiple Tasks (AZs)
                      ↓
                 CloudWatch Logs
                      ↓
                 ECR Repository
```

### Components Created:
- **VPC**: Multi-AZ setup with public subnets
- **Application Load Balancer**: SSL termination and health checks
- **ECS Cluster**: Fargate-based container orchestration
- **ECR Repository**: Private container image storage
- **Security Groups**: Network access control
- **CloudWatch**: Logging and monitoring
- **IAM Roles**: Task execution and application permissions

## Deployment Options

### Option 1: One-Click Deployment (Recommended)

The automated script handles the entire deployment process:

```bash
cd deployment/aws
./deploy.sh
```

This script will:
1. ✅ Check prerequisites
2. 🏗️ Deploy infrastructure via CloudFormation
3. 🐳 Build and push Docker image to ECR
4. 📋 Register ECS task definition
5. 🚀 Create/update ECS service
6. ⏱️ Wait for deployment completion
7. 📊 Display deployment information

### Option 2: Manual Step-by-Step Deployment

#### Step 1: Deploy Infrastructure
```bash
cd deployment/aws

# Deploy CloudFormation stack
aws cloudformation deploy \
    --template-file cloudformation-template.yaml \
    --stack-name openliberty-demo-prod-infrastructure \
    --parameter-overrides \
        ProjectName=openliberty-demo \
        Environment=prod \
    --capabilities CAPABILITY_NAMED_IAM \
    --region us-east-1
```

#### Step 2: Build and Push Docker Image
```bash
# Get ECR repository URI from CloudFormation outputs
ECR_URI=$(aws cloudformation describe-stacks \
    --stack-name openliberty-demo-prod-infrastructure \
    --query 'Stacks[0].Outputs[?OutputKey==`ECRRepositoryUri`].OutputValue' \
    --output text)

# Build Docker image
docker build -t openliberty-demo:latest ../../

# Tag for ECR
docker tag openliberty-demo:latest $ECR_URI:latest

# Login to ECR
aws ecr get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin $ECR_URI

# Push to ECR
docker push $ECR_URI:latest
```

#### Step 3: Update Configuration Files
```bash
# Update task-definition.json with your account ID and ECR URI
sed -i 's/YOUR_ACCOUNT_ID/123456789012/g' task-definition.json
sed -i 's|YOUR_ECR_URI|'$ECR_URI'|g' task-definition.json

# Update service-definition.json with actual subnet and security group IDs
# (Get these from CloudFormation outputs)
```

#### Step 4: Register Task Definition and Create Service
```bash
# Register task definition
aws ecs register-task-definition \
    --cli-input-json file://task-definition.json

# Create ECS service
aws ecs create-service \
    --cli-input-json file://service-definition.json
```

## Post-Deployment

### Verify Deployment
```bash
# Check service status
aws ecs describe-services \
    --cluster openliberty-demo-prod-cluster \
    --services openliberty-demo-service

# Check task health
aws ecs list-tasks \
    --cluster openliberty-demo-prod-cluster \
    --service-name openliberty-demo-service
```

### Access Your Application
After deployment, your application will be available at:
- **Main Application**: `http://ALB_DNS_NAME/openliberty-demo/`
- **Health Check**: `http://ALB_DNS_NAME/openliberty-demo/api/health`
- **API Endpoints**: `http://ALB_DNS_NAME/openliberty-demo/api/users`

### Monitoring and Logs
```bash
# View CloudWatch logs
aws logs describe-log-streams \
    --log-group-name /ecs/openliberty-demo

# Stream logs in real-time
aws logs tail /ecs/openliberty-demo --follow
```

## Configuration Options

### Environment Variables
Modify `task-definition.json` to add environment variables:
```json
"environment": [
    {
        "name": "JAVA_OPTS",
        "value": "-Xmx512m -Xms256m"
    },
    {
        "name": "WLP_LOGGING_CONSOLE_LOGLEVEL",
        "value": "info"
    }
]
```

### Auto Scaling
Add auto-scaling policies:
```bash
# Register scalable target
aws application-autoscaling register-scalable-target \
    --service-namespace ecs \
    --resource-id service/openliberty-demo-prod-cluster/openliberty-demo-service \
    --scalable-dimension ecs:service:DesiredCount \
    --min-capacity 1 \
    --max-capacity 10

# Create scaling policy based on CPU
aws application-autoscaling put-scaling-policy \
    --service-namespace ecs \
    --resource-id service/openliberty-demo-prod-cluster/openliberty-demo-service \
    --scalable-dimension ecs:service:DesiredCount \
    --policy-name cpu-scaling \
    --policy-type TargetTrackingScaling \
    --target-tracking-scaling-policy-configuration \
        TargetValue=70.0,PredefinedMetricSpecification='{PredefinedMetricType=ECSServiceAverageCPUUtilization}'
```

### SSL/HTTPS Setup
To enable HTTPS:
1. Request an SSL certificate in AWS Certificate Manager
2. Update the ALB listener in CloudFormation template
3. Redeploy the stack

## Troubleshooting

### Common Issues

#### 1. Task Startup Failures
```bash
# Check task definition
aws ecs describe-task-definition --task-definition openliberty-demo

# Check task failures
aws ecs describe-tasks \
    --cluster openliberty-demo-prod-cluster \
    --tasks TASK_ARN
```

#### 2. Health Check Failures
- Verify health endpoint returns 200: `/openliberty-demo/api/health`
- Check security group allows traffic on port 9080
- Review CloudWatch logs for application errors

#### 3. Image Pull Errors
```bash
# Verify ECR permissions
aws ecr describe-repositories --repository-names openliberty-demo

# Check task execution role has ECR permissions
aws iam get-role-policy \
    --role-name openliberty-demo-prod-task-execution-role \
    --policy-name ECRAccess
```

### Useful Commands
```bash
# Scale service
aws ecs update-service \
    --cluster openliberty-demo-prod-cluster \
    --service openliberty-demo-service \
    --desired-count 3

# Force new deployment
aws ecs update-service \
    --cluster openliberty-demo-prod-cluster \
    --service openliberty-demo-service \
    --force-new-deployment

# Stop all tasks
aws ecs update-service \
    --cluster openliberty-demo-prod-cluster \
    --service openliberty-demo-service \
    --desired-count 0
```

## Cost Optimization

### Fargate Spot
For development environments, consider using Fargate Spot:
```json
"capacityProviderStrategy": [
    {
        "capacityProvider": "FARGATE_SPOT",
        "weight": 1
    }
]
```

### Resource Right-Sizing
Monitor and adjust CPU/memory allocation:
- Start with 0.5 vCPU / 1GB RAM
- Monitor CloudWatch metrics
- Adjust based on actual usage

## Cleanup

To remove all resources:
```bash
# Delete ECS service
aws ecs update-service \
    --cluster openliberty-demo-prod-cluster \
    --service openliberty-demo-service \
    --desired-count 0

aws ecs delete-service \
    --cluster openliberty-demo-prod-cluster \
    --service openliberty-demo-service

# Delete CloudFormation stack
aws cloudformation delete-stack \
    --stack-name openliberty-demo-prod-infrastructure

# Delete ECR images (optional)
aws ecr batch-delete-image \
    --repository-name openliberty-demo \
    --image-ids imageTag=latest
```

## Security Best Practices

1. **Least Privilege**: Use minimal IAM permissions
2. **Network Security**: Use private subnets for production
3. **Secrets Management**: Use AWS Secrets Manager for sensitive data
4. **Image Scanning**: Enable ECR vulnerability scanning
5. **SSL/TLS**: Use HTTPS in production
6. **VPC Flow Logs**: Enable for network monitoring

## Production Considerations

1. **Multi-Region**: Deploy to multiple regions for DR
2. **Database**: Add RDS or external database
3. **CDN**: Use CloudFront for static assets
4. **Monitoring**: Set up comprehensive CloudWatch dashboards
5. **Alerting**: Configure SNS notifications for critical events
6. **Backup**: Implement backup strategies for data persistence
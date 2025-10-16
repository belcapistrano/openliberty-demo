#!/bin/bash

# Open Liberty Demo - AWS ECS Fargate Deployment Script
set -e

# Configuration
PROJECT_NAME="openliberty-demo"
ENVIRONMENT="prod"
AWS_REGION="us-east-1"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."

    command -v aws >/dev/null 2>&1 || { print_error "AWS CLI is required but not installed. Aborting."; exit 1; }
    command -v docker >/dev/null 2>&1 || { print_error "Docker is required but not installed. Aborting."; exit 1; }

    # Check AWS credentials
    aws sts get-caller-identity >/dev/null 2>&1 || { print_error "AWS credentials not configured. Run 'aws configure'. Aborting."; exit 1; }

    print_success "Prerequisites check passed"
}

# Deploy CloudFormation stack
deploy_infrastructure() {
    print_status "Deploying infrastructure with CloudFormation..."

    STACK_NAME="${PROJECT_NAME}-${ENVIRONMENT}-infrastructure"

    aws cloudformation deploy \
        --template-file cloudformation-template.yaml \
        --stack-name $STACK_NAME \
        --parameter-overrides \
            ProjectName=$PROJECT_NAME \
            Environment=$ENVIRONMENT \
        --capabilities CAPABILITY_NAMED_IAM \
        --region $AWS_REGION

    if [ $? -eq 0 ]; then
        print_success "Infrastructure deployed successfully"
    else
        print_error "Infrastructure deployment failed"
        exit 1
    fi
}

# Get stack outputs
get_stack_outputs() {
    print_status "Retrieving stack outputs..."

    STACK_NAME="${PROJECT_NAME}-${ENVIRONMENT}-infrastructure"

    ECR_URI=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`ECRRepositoryUri`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    VPC_ID=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`VPCId`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    SUBNET1_ID=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`PublicSubnet1Id`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    SUBNET2_ID=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`PublicSubnet2Id`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    SECURITY_GROUP_ID=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`ECSSecurityGroupId`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    TARGET_GROUP_ARN=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`ALBTargetGroupArn`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    CLUSTER_NAME=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`ECSClusterName`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    TASK_EXECUTION_ROLE_ARN=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`TaskExecutionRoleArn`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    TASK_ROLE_ARN=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`TaskRoleArn`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    ALB_URL=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerURL`].OutputValue' \
        --output text \
        --region $AWS_REGION)

    print_success "Stack outputs retrieved"
}

# Build and push Docker image
build_and_push_image() {
    print_status "Building and pushing Docker image..."

    # Navigate to project root (assuming script is in deployment/aws/)
    cd ../..

    # Build the Docker image
    docker build -t $PROJECT_NAME:latest .

    # Tag for ECR
    docker tag $PROJECT_NAME:latest $ECR_URI:latest

    # Login to ECR
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URI

    # Push to ECR
    docker push $ECR_URI:latest

    print_success "Docker image built and pushed to ECR"

    # Return to deployment directory
    cd deployment/aws
}

# Update task definition with actual values
update_task_definition() {
    print_status "Updating task definition with actual values..."

    # Create updated task definition
    sed -e "s|YOUR_ACCOUNT_ID|$AWS_ACCOUNT_ID|g" \
        -e "s|YOUR_ECR_URI|$ECR_URI|g" \
        -e "s|us-east-1|$AWS_REGION|g" \
        task-definition.json > task-definition-updated.json

    # Update execution and task role ARNs
    jq --arg exec_role "$TASK_EXECUTION_ROLE_ARN" \
       --arg task_role "$TASK_ROLE_ARN" \
       '.executionRoleArn = $exec_role | .taskRoleArn = $task_role' \
       task-definition-updated.json > task-definition-final.json

    print_success "Task definition updated"
}

# Register task definition
register_task_definition() {
    print_status "Registering ECS task definition..."

    aws ecs register-task-definition \
        --cli-input-json file://task-definition-final.json \
        --region $AWS_REGION

    if [ $? -eq 0 ]; then
        print_success "Task definition registered"
    else
        print_error "Task definition registration failed"
        exit 1
    fi
}

# Update service definition
update_service_definition() {
    print_status "Updating service definition..."

    # Update service definition with actual values
    jq --arg cluster "$CLUSTER_NAME" \
       --arg subnet1 "$SUBNET1_ID" \
       --arg subnet2 "$SUBNET2_ID" \
       --arg sg "$SECURITY_GROUP_ID" \
       --arg tg "$TARGET_GROUP_ARN" \
       --arg account "$AWS_ACCOUNT_ID" \
       '.cluster = $cluster |
        .networkConfiguration.awsvpcConfiguration.subnets = [$subnet1, $subnet2] |
        .networkConfiguration.awsvpcConfiguration.securityGroups = [$sg] |
        .loadBalancers[0].targetGroupArn = $tg' \
       service-definition.json > service-definition-updated.json

    print_success "Service definition updated"
}

# Create or update ECS service
deploy_service() {
    print_status "Deploying ECS service..."

    SERVICE_NAME="${PROJECT_NAME}-service"

    # Check if service exists
    if aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME --region $AWS_REGION >/dev/null 2>&1; then
        print_status "Service exists, updating..."

        aws ecs update-service \
            --cluster $CLUSTER_NAME \
            --service $SERVICE_NAME \
            --task-definition $PROJECT_NAME:LATEST \
            --region $AWS_REGION
    else
        print_status "Creating new service..."

        aws ecs create-service \
            --cli-input-json file://service-definition-updated.json \
            --region $AWS_REGION
    fi

    if [ $? -eq 0 ]; then
        print_success "ECS service deployed"
    else
        print_error "ECS service deployment failed"
        exit 1
    fi
}

# Wait for deployment to complete
wait_for_deployment() {
    print_status "Waiting for deployment to complete..."

    aws ecs wait services-stable \
        --cluster $CLUSTER_NAME \
        --services "${PROJECT_NAME}-service" \
        --region $AWS_REGION

    if [ $? -eq 0 ]; then
        print_success "Deployment completed successfully"
    else
        print_warning "Deployment may still be in progress. Check AWS console for status."
    fi
}

# Display deployment information
show_deployment_info() {
    print_success "=== DEPLOYMENT COMPLETE ==="
    echo
    echo "Application URL: $ALB_URL/openliberty-demo/"
    echo "Health Check: $ALB_URL/openliberty-demo/api/health"
    echo "API Endpoints: $ALB_URL/openliberty-demo/api/"
    echo
    echo "AWS Resources:"
    echo "  ECS Cluster: $CLUSTER_NAME"
    echo "  ECR Repository: $ECR_URI"
    echo "  VPC ID: $VPC_ID"
    echo
    echo "To monitor the deployment:"
    echo "  aws ecs describe-services --cluster $CLUSTER_NAME --services ${PROJECT_NAME}-service --region $AWS_REGION"
    echo
    print_success "Deployment script completed!"
}

# Cleanup function
cleanup() {
    print_status "Cleaning up temporary files..."
    rm -f task-definition-updated.json task-definition-final.json service-definition-updated.json
}

# Main execution
main() {
    print_status "Starting deployment of $PROJECT_NAME to AWS ECS Fargate"
    echo

    check_prerequisites
    deploy_infrastructure
    get_stack_outputs
    build_and_push_image
    update_task_definition
    register_task_definition
    update_service_definition
    deploy_service
    wait_for_deployment
    show_deployment_info
    cleanup
}

# Run main function
main "$@"
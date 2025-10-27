# Terraform MinIO Infrastructure Configuration

## Definitions

- **TF** - Terraform
- **tf state** - Terraform state file used to store the current state of managed infrastructure. You can read more [here](https://developer.hashicorp.com/terraform/language/state) about it.
- **IaC** - Infrastructure as Code
- **mc** - MinIO Client, the official command-line tool for interacting with MinIO and S3-compatible storage services

## Prerequisites

### Required Tools

1. **Terraform CLI** - Install from [here](https://developer.hashicorp.com/terraform/install)
2. **MinIO Client (mc)** - Install from [here](https://min.io/docs/minio/linux/reference/minio-mc.html)

### Required Credentials

To run this configuration, you will need:

- MinIO instance `ACCESS_KEY` and `SECRET_ACCESS_KEY`
- Access to the MinIO server at `petclinic-bucket.benmusicgeek.synology.me:9000`

## Configuration Overview

This is a proof of concept to configure the MinIO instance using Infrastructure as Code (IaC) principles. This approach allows any "click-ops" (manual operations performed through a user interface) to be imported and reproduced, preventing configuration drift.

**Important**: This configuration manages MinIO infrastructure (buckets, policies, configurations) but **does not manage the actual data/files** inside the buckets. Data backup and replication is handled separately using the MinIO client tool `mc`.

## Getting Started

### 1. Environment Setup

Set up your credentials using environment variables:

```bash
# Create terraform-env.sh (DO NOT COMMIT THIS FILE)
export TF_VAR_minio_access_key="your_access_key"
export TF_VAR_minio_secret_key="your_secret_key"

# Source the environment
source terraform-env.sh
```

### 2. Initialize Terraform

```bash
cd files-service/infra
terraform init
```

### 3. Plan and Apply

```bash
terraform plan
terraform apply
```

## Security Considerations

### State File Security

**CRITICAL**: Never commit `terraform.tfstate` files to Git. These files contain:

- Infrastructure secrets and credentials
- Resource IDs and sensitive metadata
- Provider authentication tokens

### Remote State Backend

This configuration uses a remote S3-compatible backend to store Terraform state securely:

```hcl
terraform {
  backend "s3" {
    endpoint = "https://petclinic-bucket.benmusicgeek.synology.me:9000"
    bucket   = "terraform-state-bucket"
    key      = "files-service/terraform.tfstate"
    encrypt  = true
  }
}
```

**Benefits of remote state**:

- Encrypted storage
- Team collaboration via shared state
- Automatic state locking
- Version history and backup

## Data Backup Strategy

**Important**: Terraform manages infrastructure (buckets, policies) but not the actual data inside buckets. For data backup, we have identified several solutions:

### 1. MinIO Client (mc) Replication

The primary method for backing up bucket data:

```bash
# Configure MinIO client
mc alias set myminio https://petclinic-bucket.benmusicgeek.synology.me:9000 ACCESS_KEY SECRET_KEY

# Backup bucket data
mc mirror myminio/petclinic-dev/ ./backups/petclinic-dev/
mc mirror myminio/terraform-bucket-benmusicgeek/ ./backups/terraform-bucket/

# Restore from backup
mc mirror ./backups/petclinic-dev/ myminio/petclinic-dev/
```

### 2. Docker Compose Backup Service

Add backup storage to your existing Docker Compose setup:

```yaml
# In your production docker-compose.yml
version: '3.8'

services:
  # Your existing services...
  
  # Add backup storage service
  minio-backup-storage:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    ports:
      - "9001:9000"      # Different port from main MinIO
      - "9002:9001"      # Console port
    environment:
      - MINIO_ROOT_USER=backup_admin
      - MINIO_ROOT_PASSWORD=your_backup_password
    volumes:
      - backup_data:/data
      - ./backup-scripts:/scripts
    networks:
      - petclinic-network

  # Backup scheduler service
  backup-scheduler:
    image: minio/mc:latest
    depends_on:
      - minio-backup-storage
    volumes:
      - ./backup-scripts:/scripts
      - backup_logs:/logs
    environment:
      - SOURCE_MINIO_URL=petclinic-bucket.benmusicgeek.synology.me:9000
      - SOURCE_ACCESS_KEY=${MINIO_ACCESS_KEY}
      - SOURCE_SECRET_KEY=${MINIO_SECRET_KEY}
      - BACKUP_MINIO_URL=minio-backup-storage:9000
      - BACKUP_ACCESS_KEY=backup_admin
      - BACKUP_SECRET_KEY=${BACKUP_PASSWORD}
    command: >
      sh -c "
        sleep 30 &&
        mc alias set source https://$$SOURCE_MINIO_URL $$SOURCE_ACCESS_KEY $$SOURCE_SECRET_KEY &&
        mc alias set backup http://$$BACKUP_MINIO_URL $$BACKUP_ACCESS_KEY $$BACKUP_SECRET_KEY &&
        while true; do
          echo 'Starting backup at $(date)'
          mc mirror source/petclinic-dev backup/petclinic-dev-$(date +%Y%m%d)/
          mc mirror source/terraform-bucket-benmusicgeek backup/terraform-backup-$(date +%Y%m%d)/
          echo 'Backup completed at $(date)'
          sleep 86400
        done
      "
    networks:
      - petclinic-network

volumes:
  backup_data:
  backup_logs:

networks:
  petclinic-network:
    external: true
```

### 3. Automated Data Restoration

Data restoration can be automated via scripts after Terraform redeployment:

- Triggered automatically after `terraform apply` in CI/CD pipelines
- Executed via the dev-tools service for manual restoration
- Integrated with GitHub Actions for seamless deployment

## CI/CD Integration

### GitHub Actions Pipeline

For production deployments, use GitHub Actions to:

- Store MinIO credentials in GitHub Secrets
- Centrally manage Infrastructure as Code
- Allow students to add buckets as code in their pull requests
- Ensure consistent deployment across environments

### Example GitHub Actions Workflow

```yaml
name: Deploy MinIO Infrastructure
on:
  push:
    branches: [main]
    paths: ['files-service/infra/**']

jobs:
  terraform:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: hashicorp/setup-terraform@v3
      
      - name: Terraform Init
        run: terraform init
        working-directory: files-service/infra
        env:
          TF_VAR_minio_access_key: ${{ secrets.MINIO_ACCESS_KEY }}
          TF_VAR_minio_secret_key: ${{ secrets.MINIO_SECRET_KEY }}
      
      - name: Terraform Apply
        run: terraform apply -auto-approve
        working-directory: files-service/infra
        env:
          TF_VAR_minio_access_key: ${{ secrets.MINIO_ACCESS_KEY }}
          TF_VAR_minio_secret_key: ${{ secrets.MINIO_SECRET_KEY }}
```

## Importing Existing Resources

To import existing MinIO buckets into Terraform state:

### 1. Add Resource Definition

```hcl
resource "minio_s3_bucket" "existing_bucket" {
  bucket = "existing-bucket-name"
  acl    = "private"
}
```

> Note: Adding the definition of the resource is required to be able to import it into the state in the next command

### 2. Import Command

```bash
terraform import minio_s3_bucket.existing_bucket existing-bucket-name
```

### 3. Verify State

```bash
terraform plan  # Should show no changes if import was successful
```

## Troubleshooting

### Common Issues

1. **Connection Timeout**: Check MinIO server accessibility and port configuration
2. **SSL Certificate Issues**: Use `minio_ssl = false` for testing, ensure proper certificates for production
3. **Import Failures**: Verify bucket exists and credentials have proper permissions
4. **State Lock Issues**: Ensure only one Terraform operation runs at a time

### Debug Commands

```bash
# Enable debug logging
export TF_LOG=DEBUG
terraform plan

# Check provider connectivity
terraform console
> provider.minio
```

## Quick Reference

### Essential Commands

```bash
# Initialize and apply
terraform init
terraform plan
terraform apply

# Import existing bucket
terraform import minio_s3_bucket.bucket_name actual-bucket-name

# Backup bucket data
mc mirror myminio/bucket-name/ ./backups/bucket-name/

# Check state
terraform state list
terraform state show minio_s3_bucket.bucket_name
```

### File Structure

```text
files-service/infra/
├── main.tf              # Resource definitions
├── provider.tf          # Provider configuration
├── backend.tf           # Remote state configuration
├── terraform-env.sh     # Environment variables (DO NOT COMMIT)
├── .gitignore          # Exclude sensitive files
└── README.md           # This documentation
```

### Environment Variables

```bash
# Required for provider authentication
TF_VAR_minio_access_key="your-access-key"
TF_VAR_minio_secret_key="your-secret-key"

# Optional for backend authentication, the AWS ACCESS KEY would be the same as the MINIO access key if you were looking to store the tf state in the same minio instance or within a different instance
AWS_ACCESS_KEY_ID="your-access-key"
AWS_SECRET_ACCESS_KEY="your-secret-key"
```

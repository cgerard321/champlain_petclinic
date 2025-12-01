variable "minio_access_key" {
  description = "MinIO access key"
  type        = string
  sensitive   = true
}

variable "minio_secret_key" {
  description = "MinIO secret key"
  type        = string
  sensitive   = true
}

provider "minio" {
  minio_server   = "petclinic-bucket.benmusicgeek.synology.me"
  minio_user     = var.minio_access_key
  minio_password = var.minio_secret_key
  minio_ssl      = true
}

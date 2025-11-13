output "minio_id" {
  value = minio_s3_bucket.terraform-bucket-benmusicgeek.id
}

output "minio_url" {
  value = minio_s3_bucket.terraform-bucket-benmusicgeek.bucket_domain_name
}
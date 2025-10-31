resource "minio_s3_bucket" "state_terraform_s3" {
  bucket = "terraform-bucket-benmusicgeek"
  acl    = "public"
}

resource "minio_s3_bucket" "dev_petclinic_bucket" {
  bucket = "petclinic-dev"
  acl    = "public"
}


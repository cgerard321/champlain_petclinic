# terraform {
#   # This is used to backup the terraform state to be able to restore the state at a specific time
#   backend "s3" {
#     endpoint = "https://petclinic-bucket.benmusicgeek.synology.me:9000"
#     bucket   = "terraform-state-bucket" # This bucket must exist beforehand
#     key      = "files-service/terraform.tfstate"
#     region   = "us-east-1"

#     encrypt                     = true
#     skip_credentials_validation = true
#     skip_metadata_api_check     = true
#     skip_region_validation      = true
#     force_path_style            = true
#   }

#   required_providers {
#     minio = {
#       source  = "aminueza/minio"
#       version = "3.3.0"
#     }
#   }
# }

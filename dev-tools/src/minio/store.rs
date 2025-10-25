use crate::http::prelude::{AppError, AppResult};
use minio::s3::Client;
use minio::s3::creds::StaticProvider;
use minio::s3::http::BaseUrl;
use std::env;

pub struct MinioStore {
    client: Client,
}

impl MinioStore {
    pub fn from_env() -> AppResult<Self> {
        let endpoint = env::var("MINIO_URL").map_err(|_| AppError::Internal)?;
        let access_key = env::var("FILE_ACCESS_KEY_ID").map_err(|_| AppError::Internal)?;
        let secret_key = env::var("FILE_SECRET_ACCESS_KEY").map_err(|_| AppError::Internal)?;

        let base_url: BaseUrl = endpoint.parse().map_err(|_e| AppError::Internal)?;
        let static_provider = StaticProvider::new(&access_key, &secret_key, None);

        let client = Client::new(base_url, Some(Box::new(static_provider)), None, None)
            .map_err(|_e| AppError::Internal)?;

        Ok(Self { client })
    }

    #[inline]
    pub fn client(&self) -> &Client {
        &self.client
    }
}

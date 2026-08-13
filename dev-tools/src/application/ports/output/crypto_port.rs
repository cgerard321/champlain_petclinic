use crate::shared::error::AppResult;
use std::sync::Arc;

pub trait CryptoPort: Send + Sync {
    fn hash(&self, plain: &str) -> AppResult<String>;
    fn verify_hash(&self, stored_hash_bytes: &[u8], candidate: &str) -> AppResult<bool>;
}

pub type DynCrypto = Arc<dyn CryptoPort>;

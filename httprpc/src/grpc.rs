use crate::api::challenger_client::ChallengerClient;
use deadpool::managed;

pub type Client = ChallengerClient<tonic::transport::Channel>;

pub struct GrpcPoolManager {
    url: String,
}

impl GrpcPoolManager {
    pub fn new(url: &str) -> Self {
        Self { url: url.into() }
    }
}

#[async_trait::async_trait]
impl deadpool::managed::Manager for GrpcPoolManager {
    type Type = Client;
    type Error = tonic::transport::Error;

    async fn create(&self) -> Result<Self::Type, Self::Error> {
        ChallengerClient::connect(self.url.clone()).await
    }

    async fn recycle(
        &self,
        _conn: &mut Self::Type,
        metrics: &managed::Metrics,
    ) -> managed::RecycleResult<Self::Error> {
        if metrics.age() < std::time::Duration::from_secs(60) {
            Ok(())
        } else {
            Err(managed::RecycleError::StaticMessage("stale"))
        }
    }
}

pub type Pool = managed::Pool<GrpcPoolManager>;

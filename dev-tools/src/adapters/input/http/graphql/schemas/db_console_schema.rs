use crate::adapters::input::http::graphql::contracts::mongo::MongoResultResponseContract;
use crate::adapters::input::http::graphql::contracts::service::{
    ServiceDbResponseContract, ServiceResponseContract,
};
use crate::adapters::input::http::graphql::contracts::sql::SqlResultResponseContract;
use crate::application::ports::input::docker_port::DynDockerPort;
use crate::application::ports::input::mongo_console_port::MongoConsolePort;
use crate::application::ports::input::sql_console_port::SqlConsolePort;
use crate::application::services::user_context::UserContext;
use async_graphql::{Context, InputObject, Object, Result};
use std::sync::Arc;

#[derive(InputObject)]
struct ExecuteSqlQueryInput {
    /// Name of the service as registered in dev-tools
    pub service: String,

    /// SQL query to execute
    pub query: String,

    /// Optional database name if the service exposes multiple DBs
    pub db_name: Option<String>,
}

#[derive(InputObject)]
struct ExecuteMongoQueryInput {
    /// Name of the service as registered in dev-tools
    pub service: String,

    /// MongoDB query in JSON or shell-like syntax
    pub mongo_query: String,

    /// Optional database name if the service exposes multiple DBs
    pub db_name: Option<String>,
}

#[Object]
impl ServiceDbResponseContract {
    async fn db_name(&self) -> &Option<String> {
        &self.db_name
    }

    async fn db_host(&self) -> &Option<String> {
        &self.db_host
    }

    async fn db_type(&self) -> &Option<String> {
        &self.db_type
    }
}

#[Object]
impl ServiceResponseContract {
    async fn name(&self) -> &str {
        &self.name
    }

    async fn docker_service(&self) -> &str {
        &self.docker_service
    }

    /// Returns the list of databases available in the service
    async fn dbs(&self) -> &Option<Vec<ServiceDbResponseContract>> {
        &self.dbs
    }
}

pub struct QueryRoot;

#[Object]
impl QueryRoot {
    async fn api_health(&self) -> &str {
        "ok"
    }

    async fn query_monitored_services(
        &self,
        ctx: &Context<'_>,
    ) -> Result<Vec<ServiceResponseContract>> {
        let docker_port = ctx.data::<DynDockerPort>()?;
        let user_ctx = ctx.data::<UserContext>()?;

        let services = docker_port.container_list(user_ctx).await?;

        Ok(services
            .into_iter()
            .map(ServiceResponseContract::from)
            .collect())
    }
}

pub struct MutationRoot;

// After some thinking, I decided to put these operations in mutations
// because they are not idempotent, even if a SELECT or find operation
// is idempotent. There's no easy and not complex way (without parsers or reading the query)
// to know if a query is a SELECT or a find. I found this simpler, and since this is executing a query,
// I think treating it as a mutation is fair and reasonable.
#[Object]
impl MutationRoot {
    async fn execute_sql_query(
        &self,
        ctx: &Context<'_>,
        #[graphql(desc = "Parameters for the SQL execution")]
        input: ExecuteSqlQueryInput,
    ) -> Result<SqlResultResponseContract> {
        let user_ctx = ctx.data::<UserContext>()?;
        let sql_console = ctx.data::<Arc<dyn SqlConsolePort>>()?;
        let result = sql_console
            .exec_sql_on_service(user_ctx, input.service, input.query, input.db_name)
            .await?;

        Ok(SqlResultResponseContract::from(result))
    }

    async fn execute_mongo_query(
        &self,
        ctx: &Context<'_>,
        input: ExecuteMongoQueryInput,
    ) -> Result<MongoResultResponseContract> {
        let user_ctx = ctx.data::<UserContext>()?;
        let mongo_console = ctx.data::<Arc<dyn MongoConsolePort>>()?;
        let result = mongo_console
            .exec_mongo_on_service(user_ctx, input.service, input.mongo_query, input.db_name)
            .await?;

        Ok(MongoResultResponseContract::from(result))
    }
}

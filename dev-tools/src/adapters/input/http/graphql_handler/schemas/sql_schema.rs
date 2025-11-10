use async_graphql::{Object, OutputType};

pub struct QueryRoot;

#[Object]
impl QueryRoot {
    async fn execute_sql_query(&self, query: String) -> String {
        format!("Executed SQL Query: {}", query)
    }
}

use crate::adapters::input::rest_handler::auth_guard::{
    require_all, require_any, AuthenticatedUser,
};
use crate::adapters::input::rest_handler::contracts::docker_contracts::docker::LogResponseContract;
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::config::{ADMIN_ROLE_UUID, AUTH_SERVICE_DEV_ROLE, READER_ROLE_UUID};
use crate::core::error::{AppError, AppResult};
use futures::{SinkExt, StreamExt};
use rocket::serde::json::serde_json;
use rocket::State;
use rocket_ws::stream::DuplexStream;
use rocket_ws::{Channel, Message, WebSocket};

#[get("/docker/logs/ws/auth-service")]
pub fn auth_service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
) -> AppResult<Channel<'static>> {
    require_any(&user, &[ADMIN_ROLE_UUID, AUTH_SERVICE_DEV_ROLE])?;
    require_all(&user, &[READER_ROLE_UUID])?;

    // Here we clone the docker instance so that we can move it into the closure.
    // This is because the lifeline is different for the WebSocket connection.
    let docker = docker.inner().clone();

    Ok(ws.channel(move |mut socket| {
        let docker = docker.clone();

        Box::pin(async move {
            let mut stream = match docker.stream_auth_service_logs().await {
                Ok(stream) => stream,
                Err(err) => {
                    let _ = socket
                        .send(Message::Text(format!(r#"{{"error":"{err}"}}"#)))
                        .await;
                    return Ok(());
                }
            };

            send_logs(socket, &mut stream).await;
            Ok(())
        })
    }))
}

async fn send_logs(
    mut socket: DuplexStream,
    mut stream: impl futures::Stream<Item = Result<impl Into<LogResponseContract>, AppError>> + Unpin,
) {
    while let Some(item) = stream.next().await {
        match item {
            Ok(entry) => {
                let json = serde_json::to_string(&LogResponseContract::from(entry.into())).unwrap();
                if socket.send(Message::Text(json)).await.is_err() {
                    log::info!("WebSocket client disconnected");
                    break;
                }
            }
            Err(e) => {
                log::error!("Error streaming logs: {e}");
                let _ = socket
                    .send(Message::Text(format!(r#"{{"error":"{e}"}}"#)))
                    .await;
                break;
            }
        }
    }
}

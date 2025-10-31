use crate::adapters::input::rest_handler::contracts::docker_contracts::docker::LogResponseContract;
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::error::{AppError, AppResult};
use futures::{SinkExt, StreamExt};
use rocket::serde::json::serde_json;
use rocket::State;
use rocket_ws::stream::DuplexStream;
use rocket_ws::{Channel, Message, WebSocket};
use uuid::Uuid;
use crate::adapters::input::rest_handler::auth_guard::{require_all, require_any, AuthenticatedUser};
use crate::core::config::{ADMIN_ROLE_UUID, EDITOR_ROLE_UUID, READER_ROLE_UUID};

pub fn ws_logs_for_container(
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    container: &'static str,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    // We clone docker here because we need to move it into the closure,
    // so we need it to own the reference.
    let docker = docker.inner().clone();
    let container = container.to_string();

    Ok(ws.channel(move |mut socket| {
        let docker = docker.clone();
        let container = container.clone();

        Box::pin(async move {
            let mut stream = match docker
                .stream_container_logs(&container, number_of_lines)
                .await
            {
                Ok(s) => s,
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

pub async fn send_logs(
    mut socket: DuplexStream,
    mut stream: impl futures::Stream<Item = Result<impl Into<LogResponseContract>, AppError>> + Unpin,
) {
    while let Some(item) = stream.next().await {
        match item {
            Ok(entry) => {
                let contract: LogResponseContract = entry.into();
                match serde_json::to_string(&contract) {
                    Ok(json) => {
                        if socket.send(Message::Text(json)).await.is_err() {
                            log::info!("WebSocket client disconnected");
                            break;
                        }
                    }
                    Err(e) => {
                        log::error!("Failed to serialize log entry to JSON: {e}");
                        let _ = socket
                            .send(Message::Text(
                                r#"{"error":"Failed to serialize log entry"}"#.into(),
                            ))
                            .await;
                        break;
                    }
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

pub fn ensure_logs_permissions(user: &AuthenticatedUser, extra_role: Option<Uuid>) -> AppResult<()> {
    if let Some(sr) = extra_role {
        require_any(user, &[ADMIN_ROLE_UUID, sr])?;
    } else {
        require_any(user, &[ADMIN_ROLE_UUID])?;
    }
    require_all(user, &[READER_ROLE_UUID])?;
    Ok(())
}

pub fn ensure_restart_permissions(user: &AuthenticatedUser, service_role: Option<Uuid>) -> AppResult<()> {
    if let Some(sr) = service_role {
        require_any(user, &[ADMIN_ROLE_UUID, sr])?;
    } else {
        require_any(user, &[ADMIN_ROLE_UUID])?;
    }
    require_all(user, &[EDITOR_ROLE_UUID])?;
    Ok(())
}
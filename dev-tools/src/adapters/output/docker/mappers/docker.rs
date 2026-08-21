use crate::domain::entities::docker::DockerLogEntity;
use bollard::container::LogOutput;

impl From<LogOutput> for DockerLogEntity {
    fn from(value: LogOutput) -> Self {
        DockerLogEntity {
            type_name: match value {
                LogOutput::StdErr { .. } => "stderr".to_string(),
                LogOutput::StdOut { .. } => "stdout".to_string(),
                LogOutput::Console { .. } => "console".to_string(),
                _ => "unknown".to_string(),
            },
            message: match value {
                LogOutput::StdErr { message } => String::from_utf8_lossy(&message).to_string(),
                LogOutput::StdOut { message } => String::from_utf8_lossy(&message).to_string(),
                LogOutput::Console { message } => String::from_utf8_lossy(&message).to_string(),
                _ => "".to_string(),
            },
        }
    }
}

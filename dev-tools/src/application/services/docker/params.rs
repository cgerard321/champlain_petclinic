pub struct ViewLogsParams {
    pub container_name: String,
    pub number_of_lines: Option<usize>,
    pub container_type: String,
}

pub struct RestartContainerParams {
    pub container_name: String,
    pub container_type: String,
}
# Project Monitoring with Prometheus and Grafana

![Alt text](../monitoring/attachments/Running.png "Optional Title")

## Running the Stack

1.  Run the following command from the project's ROOT directory:

    ```sh
    docker compose -f docker-compose.yml -f monitoring/docker-compose.yml up --build
    ```

2.  If your project is already built

    ```sh
    docker compose -f docker-compose.yml -f monitoring/docker-compose.yml up
    ```

### Important Note on Current Implementation

> **Note:** Currently, the monitoring stack is configured to collect metrics only from the **auth service**. The configuration for the remaining services will be implemented in a subsequent sprint.

## Accessing Prometheus

1.  You can now access the Prometheus UI by navigating to the following URL in your web browser:
    [http://localhost:9090](http://localhost:9090)

2.  From the Prometheus UI, you can write and run queries to inspect the collected metrics.

## Accessing Grafana and Viewing Dashboards

Grafana is a visualization and analytics software that allows you to query, visualize, alert on, and explore your metrics.

1.  Access the Grafana UI by navigating to the following URL in your web browser:
    [http://localhost:3001](http://localhost:3001)

![Alt text](../monitoring/attachments/DashboardButton.png "Optional Title") 2. Use the following default credentials to log in:

| Username | Password |
| :------- | :------- |
| `admin`  | `admin`  |

3.  In the Grafana UI, navigate to the dashboards section on the left-hand side menu.

4.  Click on the "JVM" dashboard to view the templated dashboard for the application.

### Important Note on Data Visualization

> **Note:** Make sure to adjust the time range to something smaller because there is little data

![Alt text](../monitoring/attachments/Troubleshooting.webp "Optional Title")

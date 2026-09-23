package com.petclinic.products.utils;

import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.h2.tools.Server;

import java.sql.SQLException;

// this is used to create a tcp connection to connect to the dev h2 db during dev
@Component
@Profile("default")
public class H2Connection {
    private Server webServer;
    
    @EventListener(ContextRefreshedEvent.class)
    public void start() throws SQLException {
        webServer = Server.createTcpServer("-tcpPort", "9092").start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        webServer.stop();
    }
}

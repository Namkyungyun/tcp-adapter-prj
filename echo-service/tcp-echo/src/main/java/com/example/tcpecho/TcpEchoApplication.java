package com.example.tcpecho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TcpEchoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(TcpEchoApplication.class, args);
        TcpServer tcpServer = run.getBean(TcpServer.class);
        tcpServer.run();

    }

}

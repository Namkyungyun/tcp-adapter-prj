package com.example.tcpechobasic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TcpEchoBasicApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(TcpEchoBasicApplication.class, args);
        TcpEchoBasicServer server = run.getBean(TcpEchoBasicServer.class);
        server.run();
    }

}

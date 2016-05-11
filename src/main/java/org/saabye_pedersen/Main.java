package org.saabye_pedersen;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class Main {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FrontendApplication.class)
                .properties("server.port=${api.port}", "server.tomcat.max-threads=1")
                .run();

        new SpringApplicationBuilder(BackendApplication.class)
                .properties("server.port=${backend.port}", "server.tomcat.max-threads=100")
                .run();
    }

}

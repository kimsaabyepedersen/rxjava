package org.saabye_pedersen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rx.Observable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class IntegrationService {

    public static final Logger LOGGER = LoggerFactory.getLogger(IntegrationService.class);


    @Value("${backend.port}")
    private int backendPort;

    public Observable<String> call() {

        return Observable.create(subscriber -> {

            logThreadInfo();

            ResponseEntity<String> responseEntity = getStringResponseEntity();
            subscriber.onNext(responseEntity.getBody());


        });

    }

    public Observable<String> call(String s) {
        return Observable.create(subscriber -> {

            logThreadInfo();

            ResponseEntity<String> responseEntity = getStringResponseEntityWithArg(s);
            subscriber.onNext(responseEntity.getBody());

        });

    }

    public Observable<String> callError() {

        return Observable.create(subscriber -> {

            logThreadInfo();

            ResponseEntity<String> responseEntity = getStringResponseEntityFail();
            subscriber.onNext(responseEntity.getBody());


        });

    }


    public ResponseEntity<String> getStringResponseEntity() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity("http://localhost:" + backendPort + "/service", String.class);
    }


    public ResponseEntity<String> getStringResponseEntityWithArg(String arg) {

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("arg", arg);
        return restTemplate.getForEntity("http://localhost:" + backendPort + "/service?arg={arg}", String.class,
                urlVariables);
    }

    public ResponseEntity<String> getStringResponseEntityFail() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity("http://localhost:" + backendPort + "/fail", String.class);
    }




    private void logThreadInfo() {
        LOGGER.info("Processing request in api to backend at {} on thread {}", LocalDateTime.now(), Thread.currentThread().getName());
    }
}

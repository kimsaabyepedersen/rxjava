package org.saabye_pedersen;

import org.saabye_pedersen.service.IntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static rx.Observable.merge;
import static rx.Observable.zip;

@SpringBootApplication
@Controller
public class FrontendApplication {

    public static final Logger LOGGER = LoggerFactory.getLogger(FrontendApplication.class);

    @Autowired
    private IntegrationService integrationService;

    @Value("${backend.port}")
    private int backendPort;

    @RequestMapping(method = RequestMethod.GET, path = "/syncOne")
    @ResponseBody
    public String syncOne() throws InterruptedException {

        logThreadInfo();

        ResponseEntity<String> responseEntity = integrationService.getStringResponseEntity();
        return "Reply from backend: " + responseEntity.getBody();

    }

    @RequestMapping(method = RequestMethod.GET, path = "/syncTwo")
    @ResponseBody
    public String syncTwo() throws InterruptedException {

        logThreadInfo();

        ResponseEntity<String> responseEntityOne = integrationService.getStringResponseEntity();
        ResponseEntity<String> responseEntityTwo = integrationService.getStringResponseEntity();

        return "Reply from backend: " + responseEntityOne.getBody() + " " + responseEntityTwo.getBody();

    }


    @RequestMapping(method = RequestMethod.GET, path = "/syncSequence")
    @ResponseBody
    public String syncSequence() throws InterruptedException {

        logThreadInfo();

        ResponseEntity<String> responseEntity = integrationService.getStringResponseEntity();
        ResponseEntity<String> responseEntityWithArg = integrationService.getStringResponseEntityWithArg(responseEntity.getBody());
        return "Reply from backend: " + responseEntityWithArg.getBody();

    }


    @RequestMapping(method = RequestMethod.GET, path = "/asyncOne")
    @ResponseBody
    public DeferredResult<String> asyncOne() throws InterruptedException {

        logThreadInfo();

        DeferredResult<String> deferredResult = new DeferredResult<>();

        integrationService.call()
                .subscribeOn(Schedulers.io())
                .subscribe(o ->
                {
                    logReply();

                    deferredResult.setResult("Reply from backend: " + o);
                });

        return deferredResult;

    }


    @RequestMapping(method = RequestMethod.GET, path = "/asyncTwo")
    @ResponseBody
    public DeferredResult<String> asyncTwo() throws InterruptedException {

        logThreadInfo();

        DeferredResult<String> deferredResult = new DeferredResult<>();

        zip(integrationService.call().subscribeOn(Schedulers.io()), integrationService.call()
                .subscribeOn(Schedulers.io()), (s, s2) -> s + " " + s2)
                .subscribe(o ->
                {
                    logReply();

                    deferredResult.setResult("Reply from backend: " + o);
                });

        return deferredResult;

    }

    @RequestMapping(method = RequestMethod.GET, path = "/asyncSequence")
    @ResponseBody
    public DeferredResult<String> asyncSequence() throws InterruptedException {

        logThreadInfo();

        DeferredResult<String> deferredResult = new DeferredResult<>();

        integrationService.call()
                .subscribeOn(Schedulers.io())
                .flatMap(s ->
                        {

                            return integrationService.call(s);

                        }
                )
                .subscribe(o ->
                {
                    logReply();

                    deferredResult.setResult("Reply from backend: " + o);
                });

        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/timeout")
    @ResponseBody
    public DeferredResult<String> timeout() throws InterruptedException {

        logThreadInfo();

        DeferredResult<String> deferredResult = new DeferredResult<>();

        integrationService.call()
                .subscribeOn(Schedulers.io())
                .timeout(2, TimeUnit.SECONDS)
                .onErrorReturn(new Func1<Throwable, String>() {
                    @Override
                    public String call(Throwable throwable) {
                        //Possibly log error
                        return "Dummy";
                    }
                })
                .subscribe(o ->
                {
                    logReply();

                    deferredResult.setResult("Reply from backend: " + o);
                });

        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/exp")
    @ResponseBody
    public DeferredResult<String> exp() throws InterruptedException {

        logThreadInfo();

        DeferredResult<String> deferredResult = new DeferredResult<>();

        integrationService.callError()
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(integrationService.call())
                .subscribe(o ->
                {
                    logReply();

                    deferredResult.setResult("Reply from backend: " + o);
                });

        return deferredResult;
    }

    private void logReply() {
        LOGGER.info("Processing reply from backend in api on thread {}", Thread.currentThread().getName());
    }

    private void logThreadInfo() {
        LOGGER.info("\n\nRequest to API at {} on thread {}", LocalDateTime.now(), Thread.currentThread().getName());
    }

}

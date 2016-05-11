package org.saabye_pedersen;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class BackendApplication {

    @RequestMapping(method = RequestMethod.GET, path = "/service")
    @ResponseBody
    public String service(@RequestParam(required = false) String arg) throws InterruptedException {

        Thread.sleep(5000);

        if (arg != null)
            return arg + ": " + arg.length();
        return "Hello World";

    }

    @RequestMapping(method = RequestMethod.GET, path = "/fail")
    @ResponseBody
    public String fail() throws InterruptedException {

        Thread.sleep(500);
        throw new RuntimeException("Fail");

    }

}

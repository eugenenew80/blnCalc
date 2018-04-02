package kz.kegoc.bln.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "demo";
    }

    @RequestMapping("/demo")
    public String demo() {
        return "demo";
    }

    @RequestMapping("/docs")
    public String docs() {
        return "docs";
    }
}

package pt.isec.pdrestapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class HelloController {

    @GetMapping("/hello") //GET + URI(/hello)
    public String hello(Principal principal) //Principal é default e recebe o user logado
    {
        return "Hello, " + principal.getName() + "!";
    }
}

package pt.isec.pdrestapi.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pdrestapi.security.TokenService;

@RestController  //Indicação que estarão exposto a API REST
public class AuthController
{
    private final TokenService tokenService;

    public AuthController(TokenService tokenService)
    {
        this.tokenService = tokenService;
    }

    @PostMapping("/login") //POST + URI (login)
    public String login(Authentication authentication)
    {
        return tokenService.generateToken(authentication); //Devolve o token
    }
}

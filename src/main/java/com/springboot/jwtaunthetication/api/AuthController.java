package com.springboot.jwtaunthetication.api;

import com.springboot.jwtaunthetication.jwt.JwtTokenUtil;
import com.springboot.jwtaunthetication.userapi.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @PostMapping("/auth/login")
    public ResponseEntity<?>login(@RequestBody @Valid AuthRequest authRequest){
        try{
                   Authentication authentication= authenticationManager.authenticate(
                           new UsernamePasswordAuthenticationToken(authRequest.getEmail(),authRequest.getPassword())
                   );
            User user=(User) authentication.getPrincipal();

            String accessToken=jwtTokenUtil.generateAccessToken(user);
            AuthResponse response=new AuthResponse(user.getEmail(),accessToken);
            return ResponseEntity.ok(response);

        }catch (BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        }

    }
}

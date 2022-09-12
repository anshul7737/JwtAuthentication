package com.springboot.jwtaunthetication.api.authentication;

import com.springboot.jwtaunthetication.dto.AuthRequest;
import com.springboot.jwtaunthetication.dto.AuthResponse;
import com.springboot.jwtaunthetication.configuration.jwt.JwtTokenUtil;
import com.springboot.jwtaunthetication.dto.AuthenticationResponse;
import com.springboot.jwtaunthetication.entity.User;

import com.springboot.jwtaunthetication.exception.InvalidPasswordException;
import com.springboot.jwtaunthetication.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private JwtUserDetailsService jwtDetailsService;

    @PostMapping("/auth/login")
    public ResponseEntity<?>login(@RequestBody @Valid AuthRequest authRequest){
//        if(userService.getUserVerificationStatus(authenticationRequest.getUsername()).equals(UserVerificationStatus.STATUS_PENDING)) {
//            throw new UserVerificationTokenException(messageHelper.getMessage("user.not.verified"));
//        }


            authenticate(authRequest.getEmail(), authRequest.getPassword());

            final UserDetails userDetails = jwtDetailsService.loadUserByUsername(authRequest.getEmail());

            // update user invalid count 0 when user login in successfully

            // generate token
            AuthenticationResponse reponse = jwtTokenUtil.generateToken(userDetails);

            return ResponseEntity.ok(reponse);


    }

    /**
     * authenticate user from given username and password
     *
     * @param username - registered username
     * @param password - user password
     */
    private void authenticate(String username, String password)  {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        } catch (DisabledException e) {
            throw new InvalidPasswordException("User is disabled");
        } catch (BadCredentialsException e) {
            // update user invalid password count
            // to show count 1 instead 0 add maxInvalidPasswordCount by 1

            // for user perspective, for now , we have added one to not to show 0 attempts
            throw new InvalidPasswordException("Invalid user credential");

        }
    }
}

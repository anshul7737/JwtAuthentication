package com.springboot.jwtaunthetication.api.user;


import com.springboot.jwtaunthetication.entity.User;
import com.springboot.jwtaunthetication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid User user){
        User savedUser = userRepository.save(user);
        URI userURI = URI.create("/users/"+savedUser.getId());
        return ResponseEntity.created(userURI).body(savedUser);
    }

    @GetMapping
    public List<User> getUser() {
        return userRepository.findAll();
    }
}

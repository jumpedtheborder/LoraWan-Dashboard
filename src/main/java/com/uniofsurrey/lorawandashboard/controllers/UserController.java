package com.uniofsurrey.lorawandashboard.controllers;

import com.uniofsurrey.lorawandashboard.config.AuthenticatedUser;
import com.uniofsurrey.lorawandashboard.config.UserPasswordEncoder;
import com.uniofsurrey.lorawandashboard.entities.Region;
import com.uniofsurrey.lorawandashboard.entities.User;
import com.uniofsurrey.lorawandashboard.exceptions.BadRequestException;
import com.uniofsurrey.lorawandashboard.models.UserDTO;
import com.uniofsurrey.lorawandashboard.repositories.RegionRepository;
import com.uniofsurrey.lorawandashboard.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
    private UserRepository userRepository;
    private RegionRepository regionRepository;

    @Autowired
    public UserController(UserRepository userRepository, RegionRepository regionRepository) {
        this.userRepository = userRepository;
        this.regionRepository =regionRepository;
    }

    @GetMapping("/rest/users")
    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/rest/user/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException ("not found"));
    }

    @GetMapping("/rest/user/self")
    public User getUserSelf() {
        return AuthenticatedUser.getPrincipal();
    }

    @DeleteMapping("/rest/user/{id}")
    public void deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException ("not found"));
        userRepository.delete(user);
    }

    @PostMapping("/rest/user")
    public User createUser(@Valid @RequestBody UserDTO newUser) {
        User test = userRepository.findByUsername(newUser.getUsername());
        if (test != null) throw new BadRequestException("This email is already being used!");

        User user = new User();
        user.setUsername(newUser.getUsername());
        user.setPassword(new UserPasswordEncoder().encode(newUser.getPassword()));
        if (newUser.getIsAdmin() == "yes") {
            user.setAdmin(true);
        }
        else {
            user.setAdmin(false);
        }
        Region region = regionRepository.findByRegionName(newUser.getRegion());
        user.setRegion(region);
        return userRepository.save(user);
    }

}

package com.example.lms.controller;

import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

  @GetMapping("/{userId}")
  public Map<String, Object> getUserById(@PathVariable String userId) {
    Map<String, Object> user = new HashMap<>();
    user.put("id", userId);
    user.put("name", "Test User");
    user.put("email", "test@example.com");
    user.put("role", "student");
    return user;
  }

  @PutMapping("/{userId}")
  public Map<String, Object> updateUser(@PathVariable String userId, @RequestBody Map<String, Object> userData) {
    Map<String, Object> user = new HashMap<>();
    user.put("id", userId);
    user.put("name", userData.getOrDefault("name", "Test User"));
    user.put("email", userData.getOrDefault("email", "test@example.com"));
    user.put("role", userData.getOrDefault("role", "student"));
    return user;
  }
}

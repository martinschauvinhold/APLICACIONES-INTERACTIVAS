package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.Session;
import com.uade.tpo.demo.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sessions")
public class SessionsController {

    @Autowired
    private SessionService sessionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Session>> getSessionsByUser(@PathVariable int userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUser(userId));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<Session> getSessionById(@PathVariable int sessionId) {
        Optional<Session> result = sessionService.getSessionById(sessionId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Object> deleteSession(@PathVariable int sessionId) {
        Optional<Session> result = sessionService.getSessionById(sessionId);
        if (result.isPresent()) {
            sessionService.deleteSession(sessionId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

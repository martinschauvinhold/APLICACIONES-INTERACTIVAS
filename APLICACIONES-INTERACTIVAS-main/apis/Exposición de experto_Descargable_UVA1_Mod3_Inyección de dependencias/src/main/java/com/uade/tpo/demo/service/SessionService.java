package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.Session;
import java.util.List;
import java.util.Optional;

public interface SessionService {
    List<Session> getSessionsByUser(int userId);
    Optional<Session> getSessionById(int sessionId);
    Session createSession(Session session);
    void deleteSession(int sessionId);
}

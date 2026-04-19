package com.uade.tpo.demo.service;



import com.uade.tpo.demo.entity.Session;
import com.uade.tpo.demo.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    public List<Session> getSessionsByUser(int userId) {
        return sessionRepository.findByUserId(userId);
    }

    public Optional<Session> getSessionById(int sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    public void deleteSession(int sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}

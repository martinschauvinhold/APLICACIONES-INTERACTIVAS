package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.Session;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.SessionRepository;
import com.uade.tpo.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Session> getSessionsByUser(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        return sessionRepository.findByUserId(userId);
    }

    public Optional<Session> getSessionById(int sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(int sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}

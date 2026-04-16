package com.uade.tpo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Session;
import com.uade.tpo.demo.entity.User;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findByUser_Id(int userId);
    void deleteByUser(User user);
}

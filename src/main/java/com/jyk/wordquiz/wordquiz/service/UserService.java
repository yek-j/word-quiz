package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<String> getUserList(User user, String username) {
        return userRepository.findByUsernameContainingAndIdNot(username, user.getId());
    }
}

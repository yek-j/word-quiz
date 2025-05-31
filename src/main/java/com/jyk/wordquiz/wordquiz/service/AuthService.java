package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.DuplicateUserException;
import com.jyk.wordquiz.wordquiz.model.dto.request.ChangePwd;
import com.jyk.wordquiz.wordquiz.model.dto.request.DeleteAccountRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.LoginRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.SignupRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.LoginResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.UserInfoResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider provider;

    @Transactional
    public void siginup(SignupRequest signupReq) {
        Optional<User> findUser = userRepository.findByEmail(signupReq.getEmail());

        if(findUser.isPresent()) {
            throw new DuplicateUserException("사용할 수 없는 이메일입니다.");
        } else {
            User newUser = new User();
            newUser.setEmail(signupReq.getEmail());
            newUser.setUsername(signupReq.getUsername());
            newUser.setPassword(passwordEncoder.encode(signupReq.getPassword()));
            userRepository.save(newUser);
        }
    }

    public LoginResponse login(LoginRequest loginReq){
        Optional<User> findUser = userRepository.findByEmail(loginReq.getEmail());

        if(findUser.isEmpty()) {
            throw new BadCredentialsException("로그인에 실패했습니다.");
        }

        User user = findUser.get();
        if(!passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("로그인에 실패했습니다.");
        }

        return LoginResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .token(provider.createJwt(user))
                .build();
    }

    public UserInfoResponse getUserInfo(String token){
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));
        return new UserInfoResponse(user.getUsername(), user.getEmail());
    }

    @Transactional
    public void changePassword(String token, ChangePwd changePwd){
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        if(!passwordEncoder.matches(changePwd.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(changePwd.getNewPassword()));

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String token, DeleteAccountRequest deleteReq) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        if(!passwordEncoder.matches(deleteReq.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }
}

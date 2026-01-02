package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.type.UserConnectionStatus;
import com.jyk.wordquiz.wordquiz.common.type.UserConnectionType;
import com.jyk.wordquiz.wordquiz.model.dto.request.FriendRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.FriendRequestResult;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.UserConnection;
import com.jyk.wordquiz.wordquiz.repository.UserConnectionRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class SocialService {
    @Autowired
    private UserConnectionRepository userConnectionRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * 요청한 사용자로 UserConnection에 FRIEND 타입, PENDING 상태로 추가
     * @param user
     * @param friendRequest
     */
    @Transactional
    public FriendRequestResult friendRequest(User user, FriendRequest friendRequest) {
        if (Objects.equals(user.getUsername(), friendRequest.getFriendUserName())) return new FriendRequestResult(HttpStatus.BAD_REQUEST, "본인에게 요청 불가능합니다.");

        Optional<User> friend = userRepository.findByUsername(friendRequest.getFriendUserName());

        if (friend.isPresent()) {
            Optional<UserConnection> alreadyConnection = userConnectionRepository.findByTargetUser(friend.get());

            if(alreadyConnection.isPresent()) return new FriendRequestResult(HttpStatus.CONFLICT, "이미 친구인 사용자 입니다.");

            UserConnection userConnection = new UserConnection();

            userConnection.setUser(user);
            userConnection.setConnectionType(UserConnectionType.FRIEND);
            userConnection.setConnectionStatus(UserConnectionStatus.PENDING);
            userConnection.setTargetUser(friend.get());

            userConnectionRepository.save(userConnection);

            return new FriendRequestResult(HttpStatus.CREATED, "친구 요청 성공");
        } else {
            return new FriendRequestResult(HttpStatus.NOT_FOUND, "친구 요청한 사용자가 존재하지 않습니다.");
        }
    }
}

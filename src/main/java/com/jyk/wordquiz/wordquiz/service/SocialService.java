package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.UserConnectionNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.UserNotFoundException;
import com.jyk.wordquiz.wordquiz.common.type.UserConnectionStatus;
import com.jyk.wordquiz.wordquiz.common.type.UserConnectionType;
import com.jyk.wordquiz.wordquiz.model.dto.request.FriendRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.Friend;
import com.jyk.wordquiz.wordquiz.model.dto.response.FriendRequestResult;
import com.jyk.wordquiz.wordquiz.model.dto.response.FriendsResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.UsersResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.UserConnection;
import com.jyk.wordquiz.wordquiz.repository.UserConnectionRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
     * 친구 거절이 이미 되어 있을 경우 1시간 후에 다시 가능
     * @param user: 사용자
     * @param friendRequest: 친구 요청할 사용자
     */
    @Transactional
    public FriendRequestResult friendRequest(User user, FriendRequest friendRequest) {
        if (Objects.equals(user.getUsername(), friendRequest.getFriendUserName())) return new FriendRequestResult(HttpStatus.BAD_REQUEST, "본인에게 요청 불가능합니다.");

        Optional<User> friend = userRepository.findByUsername(friendRequest.getFriendUserName());

        if (friend.isPresent()) {
            Optional<UserConnection> alreadyConnection = userConnectionRepository.findByUserAndTargetUser(user, friend.get());

            if(alreadyConnection.isPresent()) {
                UserConnection connection = alreadyConnection.get();
                UserConnectionStatus UCStatus = connection.getConnectionStatus();

                if(UCStatus == UserConnectionStatus.ACCEPTED) {
                    return new FriendRequestResult(HttpStatus.CONFLICT, "이미 친구인 사용자 입니다.");
                } else if (UCStatus == UserConnectionStatus.PENDING){
                    return new FriendRequestResult(HttpStatus.CONFLICT, "이미 친구 요청한 사용자 입니다.");
                } else {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime updateTime = connection.getUpdatedAt();

                    // 친구 거절 24시간 후 다시 요청 가능
                    if(now.isAfter(updateTime.plusDays(1))) {
                        connection.setConnectionStatus(UserConnectionStatus.PENDING);
                        userConnectionRepository.save(connection);
                        return new FriendRequestResult(HttpStatus.CREATED, "친구 요청 성공");
                    }

                    return new FriendRequestResult(HttpStatus.CONFLICT, "이미 친구 요청한 사용자 입니다.");
                }
            }

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

    /**
     * 사용자에게 요청온 사용자 이름 리스트 가져오기
     * @param user: 사용자
     * @param page: 페이지 번호
     * @return FriendsResponse: 사용자 리스트
     */
    public FriendsResponse getFriendRequestList(User user, int page) {
        Pageable pageReq = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<UserConnection> userConnections = userConnectionRepository.findByTargetUserAndConnectionStatus(user, UserConnectionStatus.PENDING, pageReq);

        List<Friend> userList = userConnections.getContent().stream()
                .map(uc -> new Friend(uc.getUser().getId(), uc.getUser().getUsername())).toList();

        int totalPage = userConnections.getTotalPages();

        return new FriendsResponse(userList, totalPage);
    }

    /**
     * 친구 요청 수락
     * @param user: 사용자
     * @param friendRequestId: 친구 요청한 사용자 ID
     */
    @Transactional
    public void friendRequestAccept(User user, Long friendRequestId) {
        User friendRequestUser = userRepository.findById(friendRequestId).orElseThrow(() -> new UserNotFoundException(friendRequestId));

        // targetID로 사용자 검색
        UserConnection userConnection = userConnectionRepository.findByUserAndTargetUser(friendRequestUser, user)
                .orElseThrow(() -> new UserConnectionNotFoundException(user.getId(), friendRequestId));

        if(userConnection.getConnectionStatus() != UserConnectionStatus.PENDING) {
            throw new IllegalArgumentException("수락할 수 없는 상태의 친구 요청입니다.");
        }

        // 요청받은 사용자를 targetId로 UserConnection 생성
        UserConnection newUserConnection = new UserConnection();
        newUserConnection.setUser(user);
        newUserConnection.setConnectionType(UserConnectionType.FRIEND);
        newUserConnection.setConnectionStatus(UserConnectionStatus.ACCEPTED);
        newUserConnection.setTargetUser(friendRequestUser);

        userConnectionRepository.save(newUserConnection);

        // PENDING을 ACCEPTED로 수정
        userConnection.setConnectionStatus(UserConnectionStatus.ACCEPTED);

        userConnectionRepository.save(userConnection);
    }

    /**
     * 친구 요청 거절
     * @param user: 사용자
     * @param friendRequestId: 친구 요청한 사용자 ID
     */
    @Transactional
    public void friendRequestReject(User user, Long friendRequestId) {
        User friendRequestUser = userRepository.findById(friendRequestId).orElseThrow(() -> new UserNotFoundException(friendRequestId));

        UserConnection userConnection = userConnectionRepository.findByUserAndTargetUser(friendRequestUser, user)
                .orElseThrow(() -> new UserConnectionNotFoundException(user.getId(), friendRequestId));

        userConnection.setConnectionStatus(UserConnectionStatus.REJECTED);
        userConnectionRepository.save(userConnection);
    }

    /**
     * 친구 리스트 가져오기
     * @param user: 사용자
     * @param page: 페이지 번호
     * @return FriendsResponse: 친구 리스트
     */
    public FriendsResponse getFriendList(User user, int page) {
        Pageable pageReq = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<UserConnection> userConnections = userConnectionRepository.findByUserAndConnectionTypeAndConnectionStatus(
                user, UserConnectionType.FRIEND, UserConnectionStatus.ACCEPTED, pageReq);

        List<Friend> userList = userConnections.getContent().stream()
                .map(uc -> new Friend(uc.getTargetUser().getId(), uc.getTargetUser().getUsername())).toList();

        int totalPage = userConnections.getTotalPages();

        return new FriendsResponse(userList, totalPage);
    }

    /**
     * 친구 삭제하기
     * @param user: 사용자
     * @param deleteFriendId: 삭제할 친구 ID
     */
    @Transactional
    public void deleteFriend(User user, Long deleteFriendId) {
        User deleteFriend = userRepository.findById(deleteFriendId)
                .orElseThrow(() -> new UserNotFoundException(deleteFriendId));

        UserConnection myConnection = userConnectionRepository
                .findByUserAndTargetUser(user, deleteFriend)
                .orElseThrow(() -> new UserConnectionNotFoundException(deleteFriendId, user.getId()));

        UserConnection friendConnection = userConnectionRepository
                .findByUserAndTargetUser(deleteFriend, user)
                .orElseThrow(() -> new UserConnectionNotFoundException(user.getId(), deleteFriendId));

        userConnectionRepository.delete(myConnection);
        userConnectionRepository.delete(friendConnection);
    }
}

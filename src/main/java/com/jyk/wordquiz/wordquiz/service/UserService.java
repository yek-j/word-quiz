package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.model.dto.response.UsersResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    /**
     * 사용자 이름으로 사용자 리스트 가져오기
     * @param user: 사용자
     * @param username: 검색할 사용자
     * @param page: 페이지 번호
     * @return UsersResponse: 사용자 이름 리스트
     */
    public UsersResponse getUserList(User user, String username, int page) {
        if (username.isBlank()) {
            throw new IllegalArgumentException("검색할 사용자 이름이 비어있습니다.");
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "username"));

        Page<User> findUsernames = userRepository.findByUsernameContainingAndIdNot(username, user.getId(), pageReq);

        List<String> usernameList = findUsernames.getContent().stream().map(User::getUsername).toList();

        int totalPages = findUsernames.getTotalPages();

        return new UsersResponse(usernameList, totalPages);
    }
}

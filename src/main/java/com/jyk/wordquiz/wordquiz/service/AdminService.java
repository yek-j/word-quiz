package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUserListResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUsers;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.repository.LoginLogRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;

    public AdminService(UserRepository userRepository, LoginLogRepository loginLogRepository) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
    }

    public AdminUserListResponse getAllUsers(int page, String criteria, String sort, String username) {
        Sort.Direction direction = Sort.Direction.ASC;

        if(sort.equals("DESC")) {
            direction = Sort.Direction.DESC;
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));
        Page<User> findUsers = userRepository.findByUsernameContaining(username, pageReq);
        List<Long> userIds = findUsers.getContent().stream().map(User::getId).toList();

        // 마지막 로그인 기록 검색
        List<Object[]> userLastLogins = loginLogRepository.findUserLastLoginAt(userIds);
        Map<Long, LocalDateTime> lastLoginMap = userLastLogins.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row-> (LocalDateTime) row[1]
        ));

        Page<AdminUsers> pageUsers = findUsers.map(u -> new AdminUsers(
            u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt(), lastLoginMap.get(u.getId()), u.getRole()
        ));

        int totalPage = pageUsers.getTotalPages();
        List<AdminUsers> adminUsers = pageUsers.getContent();

        return new AdminUserListResponse(adminUsers, totalPage);
    }
}

package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.type.PromptType;
import com.jyk.wordquiz.wordquiz.model.dto.request.PromptRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUserListResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUsers;
import com.jyk.wordquiz.wordquiz.model.dto.response.ListResultResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.PromptResponse;
import com.jyk.wordquiz.wordquiz.model.entity.Prompt;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.repository.LoginLogRepository;
import com.jyk.wordquiz.wordquiz.repository.PromptRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PromptRepository promptRepository;

    public AdminService(UserRepository userRepository, LoginLogRepository loginLogRepository, PromptRepository promptRepository) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.promptRepository = promptRepository;
    }

    public AdminUserListResponse getAllUsers(int page, String criteria, String sort, String username) {
        Sort.Direction direction = Sort.Direction.ASC;

        if(sort.equals("DESC")) {
            direction = Sort.Direction.DESC;
        }

        if(!List.of("id", "username", "createdAt").contains(criteria)) {
            throw new IllegalArgumentException("정렬 기준 오류");
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

    @Transactional
    public void addPrompt(PromptRequest promptRequest) {
        promptRepository.save(Prompt.builder()
                .promptType(promptRequest.getPromptType())
                .promptName(promptRequest.getPromptName())
                .content(promptRequest.getContent())
                .build());
    }

    public ListResultResponse getPromptList(int page, String criteria, String sort, String promptName, String promptType) {
        Sort.Direction direction = Sort.Direction.ASC;

        if(sort.equals("DESC")) {
            direction = Sort.Direction.DESC;
        }

        if(!List.of("id", "promptName", "promptType", "createdAt", "updatedAt", "createdBy", "lastModifiedBy").contains(criteria)) {
            throw new IllegalArgumentException("정렬 기준 오류");
        }

        PromptType promptTypeEnum = null;

        if (!promptType.isEmpty()) {
            try {
                promptTypeEnum = PromptType.valueOf(promptType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("프롬프트 타입 오류: " + promptType);
            }
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));
        Page<Prompt> findPrompts = promptRepository.findByPromptNameAndType(promptName, promptTypeEnum, pageReq);

        List<Long> userIds = findPrompts.getContent().stream()
                .flatMap(p -> Stream.of(p.getCreatedBy(), p.getLastModifiedBy()))
                .distinct()
                .toList();

        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<PromptResponse> promptResponses = findPrompts.getContent().stream()
                .map(p -> PromptResponse.builder()
                        .promptId(p.getId())
                        .promptName(p.getPromptName())
                        .content(p.getContent())
                        .promptType(p.getPromptType())
                        .disabled(p.isDisabled())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .createdUserName(usernameMap.getOrDefault(p.getCreatedBy(), "unknown"))
                        .lastModifiedUserName(usernameMap.getOrDefault(p.getLastModifiedBy(), "unknown"))
                        .build())
                .toList();

        return new ListResultResponse(promptResponses, findPrompts.getTotalPages());
    }
}

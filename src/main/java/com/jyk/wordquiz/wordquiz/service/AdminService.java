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

    /**
     * Creates an AdminService configured with the required repositories.
     */
    public AdminService(UserRepository userRepository, LoginLogRepository loginLogRepository, PromptRepository promptRepository) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.promptRepository = promptRepository;
    }

    /**
     * Retrieve a paginated list of users filtered by username and sorted by the specified criteria,
     * including each user's last login timestamp and role for administrative display.
     *
     * @param page     zero-based page index to retrieve
     * @param criteria field to sort by; allowed values: "id", "username", "createdAt"
     * @param sort     sort direction string; use "DESC" for descending, any other value results in ascending
     * @param username substring to filter usernames by (matches users whose username contains this value)
     * @return         an AdminUserListResponse containing the list of users enriched with last login times and the total number of pages
     * @throws IllegalArgumentException if {@code criteria} is not one of "id", "username", or "createdAt"
     */
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

    /**
     * Creates and persists a new Prompt using values from the provided PromptRequest and attributes creation and last modification to the given user.
     *
     * @param user the user performing the operation; their id will be recorded as `createdBy` and `lastModifiedBy` on the new prompt
     * @param promptRequest DTO containing `promptType`, `promptName`, and `content` for the new prompt
     */
    @Transactional
    public void addPrompt(User user, PromptRequest promptRequest) {
        promptRepository.save(Prompt.builder()
                .promptType(promptRequest.getPromptType())
                .promptName(promptRequest.getPromptName())
                .content(promptRequest.getContent())
                .createdBy(user.getId())
                .lastModifiedBy(user.getId())
                .build());
    }

    /**
     * Retrieve a paginated, sorted list of prompts filtered by name and type.
     *
     * @param page       zero-based page index to retrieve
     * @param criteria   field to sort by; allowed values: "id", "promptName", "promptType", "createdAt", "updatedAt", "createdBy", "lastModifiedBy"
     * @param sort       sorting direction; use "DESC" for descending, any other value yields ascending
     * @param promptName substring to filter prompt names (empty string matches all)
     * @param promptType name of a PromptType enum value to filter by (empty string matches all)
     * @return           a ListResultResponse containing PromptResponse objects for the requested page and the total number of pages
     * @throws IllegalArgumentException if the provided sort criteria is invalid or if promptType is not a valid PromptType name
     */
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

        Map<Long, String> usernameMap = getUsernameMap(findPrompts.getContent());

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

    /**
     * Fetches a prompt by its ID and returns a populated PromptResponse that includes creator and last-modifier usernames.
     *
     * @param promptId the ID of the prompt to retrieve
     * @return a PromptResponse containing the prompt's id, name, content, type, disabled flag, timestamps, and creator/last-modifier usernames (defaults to "unknown" if unavailable)
     * @throws IllegalArgumentException if no prompt exists with the given id
     */
    public PromptResponse getPrompt(Long promptId) {
        Prompt prompt = promptRepository.findById(promptId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프롬프트입니다. id: " + promptId));
        Map<Long, String> usernameMap = getUsernameMap(List.of(prompt));

        return PromptResponse.builder()
                .promptId(prompt.getId())
                .promptName(prompt.getPromptName())
                .content(prompt.getContent())
                .promptType(prompt.getPromptType())
                .disabled(prompt.isDisabled())
                .createdAt(prompt.getCreatedAt())
                .updatedAt(prompt.getUpdatedAt())
                .createdUserName(usernameMap.getOrDefault(prompt.getCreatedBy(), "unknown"))
                .lastModifiedUserName(usernameMap.getOrDefault(prompt.getLastModifiedBy(), "unknown"))
                .build();
    }

    /**
     * Builds a map of user IDs to usernames for all creators and last modifiers referenced by the given prompts.
     *
     * @param prompts list of prompts whose `createdBy` and `lastModifiedBy` IDs will be resolved
     * @return a map where keys are user IDs and values are the corresponding usernames
     */
    private Map<Long, String> getUsernameMap(List<Prompt> prompts) {
        List<Long> userIds = prompts.stream()
                .flatMap(p -> Stream.of(p.getCreatedBy(), p.getLastModifiedBy()))
                .distinct()
                .toList();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    /**
     * Disable the prompt identified by the given id and set the acting user as the prompt's last modifier.
     *
     * @param user the user performing the deletion; their id is recorded as lastModifiedBy
     * @param promptId the id of the prompt to disable
     * @throws IllegalArgumentException if no prompt exists with the provided id
     */
    @Transactional
    public void deletePrompt(User user, Long promptId) {
        Prompt prompt = promptRepository.findById(promptId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프롬프트입니다. id: " + promptId));

        prompt.setDisabled(true);
        prompt.setLastModifiedBy(user.getId());

        promptRepository.save(prompt);
    }

    /**
     * Update an existing prompt's name, type, and content and set the acting user as its last modifier.
     *
     * @param user the user performing the update; their id is recorded as the prompt's lastModifiedBy
     * @param promptId the id of the prompt to update
     * @param promptRequest the new prompt data (promptName, promptType, content)
     * @throws IllegalArgumentException if no prompt exists with the given id
     */
    @Transactional
    public void updatePrompt(User user, Long promptId, PromptRequest promptRequest) {
        Prompt prompt = promptRepository.findById(promptId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프롬프트입니다. id: " + promptId));

        prompt.setPromptName(promptRequest.getPromptName());
        prompt.setPromptType(promptRequest.getPromptType());
        prompt.setContent(promptRequest.getContent());
        prompt.setLastModifiedBy(user.getId());

        promptRepository.save(prompt);
    }
}

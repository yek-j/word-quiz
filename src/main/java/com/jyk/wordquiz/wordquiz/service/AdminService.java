package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.UserNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.ConfigRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.PromptRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizTypeRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.UserRoleRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUserListResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUsers;
import com.jyk.wordquiz.wordquiz.model.dto.response.AiQuizResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.ListResultResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.PromptResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.PromptValidateResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizTypeResponse;
import com.jyk.wordquiz.wordquiz.model.entity.Config;
import com.jyk.wordquiz.wordquiz.model.entity.Prompt;
import com.jyk.wordquiz.wordquiz.model.entity.QuizType;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.repository.*;
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
    private static final String SAMPLE_TERM = "apple";
    private static final String SAMPLE_MEANING = "사과 (과일의 한 종류)";

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PromptRepository promptRepository;
    private final ConfigRepository configRepository;
    private final QuizTypeRepository quizTypeRepository;
    private final AIQuestionService aiQuestionService;

    public AdminService(UserRepository userRepository,
                        LoginLogRepository loginLogRepository,
                        PromptRepository promptRepository,
                        ConfigRepository configRepository,
                        QuizTypeRepository quizTypeRepository,
                        AIQuestionService aiQuestionService) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.promptRepository = promptRepository;
        this.configRepository = configRepository;
        this.quizTypeRepository = quizTypeRepository;
        this.aiQuestionService = aiQuestionService;
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
    public void addQuizType(User user, QuizTypeRequest quizTypeRequest) {
        quizTypeRepository.save(QuizType.builder()
                .quizTypeName(quizTypeRequest.getQuizTypeName())
                .quizTypeDescription(quizTypeRequest.getQuizTypeDescription())
                .useAi(quizTypeRequest.isUseAi())
                .createdBy(user.getId())
                .lastModifiedBy(user.getId())
                .build());
    }

    public ListResultResponse getQuizTypeList(int page, String criteria, String sort, String quizTypeName) {
        Sort.Direction direction = Sort.Direction.ASC;

        if(sort.equals("DESC")) {
            direction = Sort.Direction.DESC;
        }

        if(!List.of("id", "quizTypeName", "useAi", "createdAt", "updatedAt", "createdBy", "lastModifiedBy").contains(criteria)) {
            throw new IllegalArgumentException("정렬 기준 오류");
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));
        Page<QuizType> findQuizTypes = quizTypeRepository.findByQuizTypeNameContaining(quizTypeName, pageReq);

        List<Long> userIds = findQuizTypes.getContent().stream()
                .flatMap(q -> Stream.of(q.getCreatedBy(), q.getLastModifiedBy()))
                .distinct()
                .toList();
        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<QuizTypeResponse> quizTypeResponses = findQuizTypes.getContent().stream()
                .map(q -> QuizTypeResponse.builder()
                        .quizTypeId(q.getId())
                        .quizTypeName(q.getQuizTypeName())
                        .quizTypeDescription(q.getQuizTypeDescription())
                        .useAi(q.isUseAi())
                        .createdAt(q.getCreatedAt())
                        .updatedAt(q.getUpdatedAt())
                        .createdUserName(usernameMap.getOrDefault(q.getCreatedBy(), "unknown"))
                        .lastModifiedUserName(usernameMap.getOrDefault(q.getLastModifiedBy(), "unknown"))
                        .build())
                .toList();

        return new ListResultResponse(quizTypeResponses, findQuizTypes.getTotalPages());
    }

    public QuizTypeResponse getQuizType(Long quizTypeId) {
        QuizType quizType = quizTypeRepository.findById(quizTypeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈 타입입니다. id: " + quizTypeId));

        List<Long> userIds = Stream.of(quizType.getCreatedBy(), quizType.getLastModifiedBy())
                .distinct()
                .toList();
        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return QuizTypeResponse.builder()
                .quizTypeId(quizType.getId())
                .quizTypeName(quizType.getQuizTypeName())
                .quizTypeDescription(quizType.getQuizTypeDescription())
                .useAi(quizType.isUseAi())
                .createdAt(quizType.getCreatedAt())
                .updatedAt(quizType.getUpdatedAt())
                .createdUserName(usernameMap.getOrDefault(quizType.getCreatedBy(), "unknown"))
                .lastModifiedUserName(usernameMap.getOrDefault(quizType.getLastModifiedBy(), "unknown"))
                .build();
    }

    @Transactional
    public void addPrompt(User user, PromptRequest promptRequest) {
        QuizType quizType = quizTypeRepository.findById(promptRequest.getPromptTypeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈 타입입니다. id:" + promptRequest.getPromptTypeId()));

        if (promptRepository.existsByPromptType_Id(promptRequest.getPromptTypeId())) {
            throw new IllegalArgumentException("해당 퀴즈 타입의 프롬프트가 이미 존재합니다. id:" + promptRequest.getPromptTypeId());
        }

        if (!quizType.isUseAi()) {
            throw new IllegalArgumentException("AI 사용 퀴즈 타입에만 프롬프트를 추가할 수 있습니다.");
        }

        promptRepository.save(Prompt.builder()
                .promptType(quizType)
                .promptName(promptRequest.getPromptName())
                .content(promptRequest.getContent())
                .createdBy(user.getId())
                .lastModifiedBy(user.getId())
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

        String quizTypeName = (promptType == null || promptType.isEmpty()) ? null : promptType;

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));
        Page<Prompt> findPrompts = promptRepository.findByPromptNameAndType(promptName, quizTypeName, pageReq);

        Map<Long, String> usernameMap = getUsernameMap(findPrompts.getContent());

        List<PromptResponse> promptResponses = findPrompts.getContent().stream()
                .map(p -> PromptResponse.builder()
                        .promptId(p.getId())
                        .promptName(p.getPromptName())
                        .content(p.getContent())
                        .promptTypeId(p.getPromptType().getId())
                        .promptTypeName(p.getPromptType().getQuizTypeName())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .createdUserName(usernameMap.getOrDefault(p.getCreatedBy(), "unknown"))
                        .lastModifiedUserName(usernameMap.getOrDefault(p.getLastModifiedBy(), "unknown"))
                        .build())
                .toList();

        return new ListResultResponse(promptResponses, findPrompts.getTotalPages());
    }

    public PromptResponse getPrompt(Long promptId) {
        Prompt prompt = promptRepository.findById(promptId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프롬프트입니다. id: " + promptId));
        Map<Long, String> usernameMap = getUsernameMap(List.of(prompt));

        return PromptResponse.builder()
                .promptId(prompt.getId())
                .promptName(prompt.getPromptName())
                .content(prompt.getContent())
                .promptTypeId(prompt.getPromptType().getId())
                .promptTypeName(prompt.getPromptType().getQuizTypeName())
                .createdAt(prompt.getCreatedAt())
                .updatedAt(prompt.getUpdatedAt())
                .createdUserName(usernameMap.getOrDefault(prompt.getCreatedBy(), "unknown"))
                .lastModifiedUserName(usernameMap.getOrDefault(prompt.getLastModifiedBy(), "unknown"))
                .build();
    }

    private Map<Long, String> getUsernameMap(List<Prompt> prompts) {
        List<Long> userIds = prompts.stream()
                .flatMap(p -> Stream.of(p.getCreatedBy(), p.getLastModifiedBy()))
                .distinct()
                .toList();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    @Transactional
    public void deletePrompt(User user, Long promptId) {
        Prompt prompt = promptRepository.findById(promptId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프롬프트입니다. id: " + promptId));
        promptRepository.delete(prompt);
    }

    @Transactional
    public void updateQuizType(User user, Long quizTypeId, QuizTypeRequest quizTypeRequest) {
        QuizType quizType = quizTypeRepository.findById(quizTypeId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈 타입입니다. id: " + quizTypeId));

        if(quizType.isUseAi() && !quizTypeRequest.isUseAi()
            && promptRepository.existsByPromptType_Id(quizTypeId)) {
            throw new IllegalArgumentException("연결된 프롬프트가 존재하여 useAi를 false로 변경할 수 없습니다.");
        }

        quizType.setQuizTypeName(quizTypeRequest.getQuizTypeName());
        quizType.setQuizTypeDescription(quizTypeRequest.getQuizTypeDescription());
        quizType.setUseAi(quizTypeRequest.isUseAi());
        quizType.setLastModifiedBy(user.getId());
    }

    @Transactional
    public void deleteQuizType(User user, Long quizTypeId) {
        QuizType quizType = quizTypeRepository.findById(quizTypeId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈 타입입니다. id: " + quizTypeId));

        // MEANING_TO_WORD(2)와 WORD_TO_MEANING(1)은 삭제 불가능 하도록
        if (List.of("MEANING_TO_WORD", "WORD_TO_MEANING").contains(quizType.getQuizTypeName())) {
            throw new IllegalArgumentException("삭제할 수 없는 기본 퀴즈 타입입니다.");
        }

        if (promptRepository.existsByPromptType_Id(quizTypeId)) {
            promptRepository.deleteByPromptType_Id(quizTypeId);
        }

        quizTypeRepository.delete(quizType);
    }

    @Transactional
    public void updatePrompt(User user, Long promptId, PromptRequest promptRequest) {
        Prompt prompt = promptRepository.findById(promptId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프롬프트입니다. id: " + promptId));
        QuizType quizType = quizTypeRepository.findById(promptRequest.getPromptTypeId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈 타입입니다. id:" + promptRequest.getPromptTypeId()));

        if (promptRepository.existsByPromptType_IdAndIdNot(promptRequest.getPromptTypeId(), promptId)) {
            throw new IllegalArgumentException("해당 퀴즈 타입의 프롬프트가 이미 존재합니다. id:" + promptRequest.getPromptTypeId());
        }

        prompt.setPromptName(promptRequest.getPromptName());
        prompt.setPromptType(quizType);
        prompt.setContent(promptRequest.getContent());
        prompt.setLastModifiedBy(user.getId());
    }

    @Transactional
    public void setUserRole(Long changeUserId, UserRoleRequest userRoleRequest) {
        User changeUser = userRepository.findById(changeUserId).orElseThrow(() -> new UserNotFoundException(changeUserId));
        changeUser.setRole(userRoleRequest.getUserRole());
    }

    @Transactional
    public void updateConfig(ConfigRequest configRequest) {
        Config config = configRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Config가 초기화되지 않았습니다."));
        config.update(configRequest);
    }

    /**
     * 어드민이 "검증" 버튼을 눌렀을 때, 샘플 단어로 LLM을 호출해
     * 프롬프트가 구조화된 출력을 내고 정답 단어가 sentence에 노출되지 않는지 검사한다.
     * 저장/수정 자체는 막지 않으며, 프론트가 valid=true인 것만 저장하도록 게이트한다.
     */
    public PromptValidateResponse validatePrompt(String promptContent) {
        AiQuizResponse sample;
        try {
            sample = aiQuestionService.runWithSample(promptContent, SAMPLE_TERM, SAMPLE_MEANING);
        } catch (Exception e) {
            return new PromptValidateResponse(false, "LLM 호출 또는 응답 파싱 실패: " + e.getMessage(), null);
        }

        if (sample == null) {
            return new PromptValidateResponse(false, "응답이 비어있습니다.", null);
        }
        if (sample.sentence() == null || sample.sentence().isBlank()) {
            return new PromptValidateResponse(false, "sentence가 비어있습니다.", sample);
        }
        if (sample.translation() == null || sample.translation().isBlank()) {
            return new PromptValidateResponse(false, "translation이 비어있습니다.", sample);
        }
        if (sample.sentence().toLowerCase().contains(SAMPLE_TERM.toLowerCase())) {
            return new PromptValidateResponse(
                    false,
                    "정답 단어(" + SAMPLE_TERM + ")가 sentence에 그대로 노출됩니다. 빈칸 처리되어야 합니다.",
                    sample
            );
        }

        return new PromptValidateResponse(true, "검증 성공", sample);
    }
}

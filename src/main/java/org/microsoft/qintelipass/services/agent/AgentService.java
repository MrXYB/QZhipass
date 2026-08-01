package org.microsoft.qintelipass.services.agent;

import lombok.extern.slf4j.Slf4j;
import org.microsoft.qintelipass.exceptions.BadRequestException;
import org.microsoft.qintelipass.exceptions.ConflictException;
import org.microsoft.qintelipass.exceptions.NotFoundException;
import org.microsoft.qintelipass.entity.PublicAgentTemplate;
import org.microsoft.qintelipass.entity.User;
import org.microsoft.qintelipass.entity.UserAgent;
import org.microsoft.qintelipass.repository.PublicAgentTemplateRepository;
import org.microsoft.qintelipass.repository.UserAgentRepository;
import org.microsoft.qintelipass.dtos.request.CreateAgentRequest;
import org.microsoft.qintelipass.dtos.response.AgentResponse;
import org.microsoft.qintelipass.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AgentService {

    private static final int MAX_AGENT_NAME_LENGTH = 20;
    private static final int MAX_AGENTS_PER_USER = 10;
    private static final Pattern AGENT_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+$");

    private final UserAgentRepository userAgentRepository;
    private final PublicAgentTemplateRepository publicTemplateRepository;
    private final UserService userService;

    public AgentService(
            UserAgentRepository userAgentRepository,
            PublicAgentTemplateRepository publicTemplateRepository,
            UserService userService
    ) {
        this.userAgentRepository = userAgentRepository;
        this.publicTemplateRepository = publicTemplateRepository;
        this.userService = userService;
    }

    @Transactional
    public AgentResponse createAgent(Long userId, CreateAgentRequest request) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }

        String name = normalizeAgentName(request == null ? null : request.getName());
        String prompt = normalizePrompt(request == null ? null : request.getPrompt());

        long count = userAgentRepository.countByUser_IdAndStatus(userId, UserAgent.STATUS_ACTIVE);
        if (count >= MAX_AGENTS_PER_USER) {
            throw new BadRequestException("创建Agent数量已达上限（" + MAX_AGENTS_PER_USER + "个），无法继续创建");
        }

        userAgentRepository.findByUser_IdAndNameAndStatus(userId, name, UserAgent.STATUS_ACTIVE)
                .ifPresent(existing -> {
                    throw new ConflictException("该Agent名称已存在，保存失败，请重新输入");
                });

        UserAgent agent = UserAgent.builder()
                .user(user)
                .name(name)
                .prompt(prompt)
                .status(UserAgent.STATUS_ACTIVE)
                .build();

        UserAgent saved = userAgentRepository.save(agent);
        log.info("User {} created agent: id={}, name={}", userId, saved.getId(), saved.getName());
        return AgentResponse.fromUserAgent(saved);
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> listAgents(Long userId) {
        List<AgentResponse> result = new ArrayList<>();

        List<PublicAgentTemplate> templates = publicTemplateRepository
                .findByStatusOrderByCreatedAtAsc(PublicAgentTemplate.STATUS_ACTIVE);
        for (PublicAgentTemplate t : templates) {
            result.add(AgentResponse.fromTemplate(t.getId(), t.getName(), t.getPrompt()));
        }

        List<UserAgent> userAgents = userAgentRepository
                .findByUser_IdAndStatusOrderByCreatedAtDesc(userId, UserAgent.STATUS_ACTIVE);
        for (UserAgent a : userAgents) {
            result.add(AgentResponse.fromUserAgent(a));
        }

        return result;
    }

    @Transactional
    public void deleteAgent(Long userId, Long agentId) {
        UserAgent agent = userAgentRepository.findByIdAndUser_IdAndStatus(agentId, userId, UserAgent.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("Agent不存在或不属于当前用户"));

        agent.setStatus(UserAgent.STATUS_DELETED);
        userAgentRepository.save(agent);
        log.info("User {} deleted agent: id={}, name={}", userId, agent.getId(), agent.getName());
    }

    private String normalizeAgentName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestException("Agent名称不能为空");
        }
        String normalized = name.trim();

        if (normalized.codePointCount(0, normalized.length()) > MAX_AGENT_NAME_LENGTH) {
            throw new BadRequestException("Agent名称长度不得多于" + MAX_AGENT_NAME_LENGTH + "个字符");
        }

        if (!AGENT_NAME_PATTERN.matcher(normalized).matches()) {
            throw new BadRequestException("不得使用特殊字符");
        }

        return normalized;
    }

    private String normalizePrompt(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new BadRequestException("提示词内容不能为空");
        }
        String normalized = prompt.trim();
        if (normalized.codePointCount(0, normalized.length()) > 5_000) {
            throw new BadRequestException("提示词内容过长，最多5000字符");
        }
        return normalized;
    }
}

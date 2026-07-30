package org.microsoft.qintelipass.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.microsoft.qintelipass.dtos.request.CreateConversationRequest;
import org.microsoft.qintelipass.dtos.request.ConversationTurnRequest;
import org.microsoft.qintelipass.dtos.request.SaveConversationMessageRequest;
import org.microsoft.qintelipass.dtos.request.UpdateConversationModelRequest;
import org.microsoft.qintelipass.dtos.request.UpdateConversationTitleRequest;
import org.microsoft.qintelipass.dtos.response.*;
import org.microsoft.qintelipass.entity.User;
import org.microsoft.qintelipass.services.censor.CensorService;
import org.microsoft.qintelipass.services.chat.ConversationService;
import org.microsoft.qintelipass.services.chat.ConversationTurnService;
import org.microsoft.qintelipass.services.user.CurrentUserService;
import org.microsoft.qintelipass.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/conversations")
// Controller only resolves the current user and request body; ownership is enforced in the service.
public class ConversationController {
    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;
    private final CensorService censorService;
    private final UserService userService;
    private final ConversationTurnService conversationTurnService;

    public ConversationController(ConversationService conversationService,
                                  CurrentUserService currentUserService,
                                  CensorService censorService,
                                  UserService userService,
                                  ConversationTurnService conversationTurnService) {
        this.conversationService = conversationService;
        this.currentUserService = currentUserService;
        this.censorService = censorService;
        this.userService = userService;
        this.conversationTurnService = conversationTurnService;
    }

    @PostMapping("/{conversationId}/turns")
    public ResponseEntity<ApiResponse<ConversationTurnResponse>> sendTurn(
            @PathVariable Long conversationId,
            @Valid @RequestBody ConversationTurnRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userService.getUserById(currentUserService.requireUserId(httpRequest));
        ConversationTurnResponse response = conversationTurnService.send(user, conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Turn completed.", response));
    }

    @PostMapping("/turns")
    public ResponseEntity<ApiResponse<ConversationTurnResponse>> sendFirstTurn(
            @Valid @RequestBody ConversationTurnRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userService.getUserById(currentUserService.requireUserId(httpRequest));
        ConversationTurnResponse response = conversationTurnService.sendNew(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Conversation created and turn completed.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @RequestBody(required = false) CreateConversationRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = currentUserService.requireUserId(httpRequest);
        ConversationResponse response = conversationService.createConversation(userService.getUserById(userId), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Conversation created.", response));
    }

    @PostMapping("/initial")
    public ResponseEntity<ApiResponse<ConversationResponse>> createInitialConversation(HttpServletRequest request) {
        User user = userService.getUserById(currentUserService.requireUserId(request));
        ConversationResponse response = conversationService.createInitialConversation(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Initial conversation created.", response));
    }

    @GetMapping
    public ApiResponse<List<ConversationSummaryResponse>> listRecentConversations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request
    ) {
        Long userId = currentUserService.requireUserId(request);
        return ApiResponse.ok(conversationService.listRecentConversations(userId, page, limit));
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<ConversationDetailResponse> getConversation(
            @PathVariable Long conversationId,
            HttpServletRequest request
    ) {
        User user = userService.getUserById(currentUserService.requireUserId(request));
        return ApiResponse.ok(conversationService.getConversation(user, conversationId));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ConversationMessageResponse>> saveMessage(
            @PathVariable Long conversationId,
            @RequestBody SaveConversationMessageRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userService.getUserById(currentUserService.requireUserId(httpRequest));
        ConversationMessageResponse response = conversationService.saveMessage(user, conversationId, request);

        // Safe fallback: run sensitive-word check on request content if available
        try {
            if (user != null) {
                String inputContent = request != null ? request.getContent() : "";
                String outputContent = response.content() != null ? response.content() : "";
                censorService.checkAndRecord(
                        user,
                        user.getName(),
                        user.getPhone(),
                        user.getDepartment() != null ? user.getDepartment() : "",
                        response.modelKey() != null ? response.modelKey() : "",
                        inputContent,
                        outputContent
                );
            }
        } catch (Exception ignored) {
            // never fail the message-save flow because of censor
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message saved.", response));
    }

    @PatchMapping("/{conversationId}/model")
    public ApiResponse<ConversationResponse> updateModel(
            @PathVariable Long conversationId,
            @RequestBody UpdateConversationModelRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userService.getUserById(currentUserService.requireUserId(httpRequest));
        return ApiResponse.ok(conversationService.updateModel(user, conversationId, request));
    }

    @PatchMapping("/{conversationId}/title")
    public ApiResponse<ConversationResponse> updateTitle(
            @PathVariable Long conversationId,
            @RequestBody UpdateConversationTitleRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userService.getUserById(currentUserService.requireUserId(httpRequest));
        return ApiResponse.ok(conversationService.updateTitle(user, conversationId, request));
    }
}

package com.grapheople.controllers;

import agents.pax.PaxAgent;
import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.grapheople.utils.JsonUtil;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/agent")
@Component
public class ApiController {

    private InMemoryRunner runner;
    private static final String DEFAULT_USER_ID = "api_user";
    private final Map<String, Session> sessionsByUser = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        BaseAgent agent = PaxAgent.initAgent();
        this.runner = new InMemoryRunner(agent);
    }

    @GetMapping("/ask")
    public ResponseEntity<?> askQuestion(
            @RequestParam String question,
            @RequestParam(name = "sessionId", required = false) String sessionId
    ) {
        return processQuestion(question, DEFAULT_USER_ID, sessionId);
    }

    @GetMapping("/ask/{userId}")
    public ResponseEntity<?> askQuestionWithUser(
            @PathVariable String userId,
            @RequestParam String question,
            @RequestParam(name = "sessionId", required = false) String sessionId
    ) {
        return processQuestion(question, userId, sessionId);
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestionPost(@RequestBody QuestionRequest request) {
        String userId = request.getUserId() != null ? request.getUserId() : DEFAULT_USER_ID;
        return processQuestion(request.getQuestion(), userId, request.getSessionId());
    }

    private ResponseEntity<?> processQuestion(String question, String userId, String sessionId) {
        Session session = null;
        try {
            // 기존 세션이 있으면 재사용, 없으면 새로 생성
            session = getOrCreateSession(userId);

            System.out.println(JsonUtil.toPrettyJsonString(session));

            // 사용자 메시지 생성
            Content userMsg = Content.fromParts(Part.fromText(question));

            // 에이전트 실행
            Flowable<Event> events = runner.runAsync(userId, session.id(), userMsg);

            // 응답 수집
            StringBuilder responseBuilder = new StringBuilder();
            events.blockingForEach(event -> {
                String content = event.stringifyContent();
                if (content != null && !content.trim().isEmpty()) {
                    responseBuilder.append(content).append(" ");
                }
            });

            String response = responseBuilder.toString().trim();
            if (response.isEmpty()) {
                response = "Agent processed the request but returned no content.";
            }

            return ResponseEntity.ok(new AgentResponse("success", response, question));

        } catch (Exception e) {
            System.err.println("Error processing question for user " + userId + ": " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(new AgentResponse("error", "Error processing question: " + e.getMessage(), question));
        } finally {
            // 세션 정리는 ADK에서 자동으로 처리되도록 함
            if (session != null) {
                System.out.println("Completed processing for session: " + session.id());
            }
        }
    }

    private Session createNewSession(String userId) {
        try {
            // 유니크한 세션 이름 생성
            String sessionName = "pax_agent";

            Session session = runner.sessionService()
                    .createSession(sessionName, userId)
                    .blockingGet();

            System.out.println("Created new session: " + session.id() + " for user: " + userId);
            // Save for later retrieval
            sessionsByUser.put(userId, session);
            return session;

        } catch (Exception e) {
            System.err.println("Failed to create session for user: " + userId + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to create session for user: " + userId, e);
        }
    }

    private Session getOrCreateSession(String userId) {
        Session existingForUser = sessionsByUser.get(userId);
        if (existingForUser != null) {
            return existingForUser;
        }
        return createNewSession(userId);
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            // 간단한 헬스체크를 위한 테스트 세션 생성
            String testUserId = "health_check_" + System.currentTimeMillis();
            Session testSession = createNewSession(testUserId);

            return ResponseEntity.ok(Map.of(
                    "status", "healthy",
                    "timestamp", System.currentTimeMillis(),
                    "testSessionId", testSession.id()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "unhealthy",
                            "error", e.getMessage(),
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> testAgent() {
        String testQuestion = "Hello, can you tell me what tools you have?";
        return processQuestion(testQuestion, "test_user_" + System.currentTimeMillis(), null);
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("Shutting down MultiToolAgentController...");
    }

    // DTO classes
    public static class QuestionRequest {
        private String question;
        private String userId;
        private String sessionId;

        // Constructors
        public QuestionRequest() {}

        public QuestionRequest(String question, String userId) {
            this.question = question;
            this.userId = userId;
        }

        // Getters and Setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    public static class AgentResponse {
        private String status;
        private String response;
        private String originalQuestion;
        private long timestamp;

        public AgentResponse(String status, String response, String originalQuestion) {
            this.status = status;
            this.response = response;
            this.originalQuestion = originalQuestion;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getStatus() { return status; }
        public String getResponse() { return response; }
        public String getOriginalQuestion() { return originalQuestion; }
        public long getTimestamp() { return timestamp; }
    }

    // Session utility endpoints
    @PostMapping("/session")
    public ResponseEntity<?> createSession(@RequestParam(name = "userId", required = false) String userId) {
        String uid = (userId != null && !userId.isBlank()) ? userId : DEFAULT_USER_ID;
        Session session = createNewSession(uid);
        return ResponseEntity.ok(Map.of(
                "status", "created",
                "userId", uid,
                "sessionId", session.id()
        ));
    }

    @GetMapping("/session/{userId}")
    public ResponseEntity<?> getSession(@PathVariable String userId) {
        Session session = sessionsByUser.get(userId);
        if (session == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "not_found",
                    "sessionId", userId
            ));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "sessionId", session.id()
        ));
    }
}

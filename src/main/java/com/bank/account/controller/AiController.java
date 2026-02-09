package com.bank.account.controller;

import com.bank.account.dto.AiRequest;
import com.bank.account.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public Map<String, String> askQuestion(@RequestBody Map<String, String> body) {

        String question = body.get("question");
        String answer = aiService.askAi(question);

        return Map.of(
                "question", question,
                "answer", answer
        );
    }
}
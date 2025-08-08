package com.flashcards.controller;

import com.flashcards.model.Quiz;
import com.flashcards.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/deck/{deckId}")
    public ResponseEntity<Quiz> createQuiz(@PathVariable String deckId, 
                                         @RequestParam int questions,
                                         Authentication authentication) {
        try {
            String userId = authentication.getName();
            Quiz quiz = quizService.createQuiz(deckId, userId, questions);
            return ResponseEntity.ok(quiz);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<Quiz> submitQuiz(@PathVariable String quizId,
                                         @RequestBody Map<String, Integer> answers,
                                         Authentication authentication) {
        try {
            String userId = authentication.getName();
            Quiz quiz = quizService.submitQuiz(quizId, answers);
            return ResponseEntity.ok(quiz);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<Quiz>> getUserQuizHistory(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<Quiz> history = quizService.getUserQuizHistory(userId);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/deck/{deckId}/history")
    public ResponseEntity<List<Quiz>> getDeckQuizHistory(@PathVariable String deckId,
                                                        Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<Quiz> history = quizService.getDeckQuizHistory(deckId, userId);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<Quiz> getQuiz(@PathVariable String quizId) {
        try {
            Quiz quiz = quizService.getQuiz(quizId);
            return ResponseEntity.ok(quiz);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable String quizId,
                                         Authentication authentication) {
        try {
            String userId = authentication.getName();
            quizService.deleteQuiz(quizId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 
package org.example.webproject.controller;

import org.example.webproject.dto.ReviewRequest;
import org.example.webproject.entity.Review;
import org.example.webproject.service.ReviewService;
import org.example.webproject.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping
    public List<Review> getAll() {
        return reviewService.getAll();
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable Long id) {
        return reviewService.getById(id);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getByMovieId(@PathVariable Long movieId) {
        Review review = reviewService.getByMovieId(movieId);
        if (review != null) {
            return ResponseEntity.ok(review);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public Review add(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                      @RequestBody ReviewRequest request) {
        String username = userService.getUsernameByToken(token);
        return reviewService.add(request, username);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ReviewRequest request) {
        if (reviewService.update(id, request)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (reviewService.delete(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}

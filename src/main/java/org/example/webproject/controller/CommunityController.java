package org.example.webproject.controller;

import org.example.webproject.entity.Review;
import org.example.webproject.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 社区：展示所有用户的影评（评价者、电影、时间、星级） */
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final ReviewService reviewService;

    public CommunityController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews")
    public List<Review> communityReviews() {
        return reviewService.getCommunity();
    }
}

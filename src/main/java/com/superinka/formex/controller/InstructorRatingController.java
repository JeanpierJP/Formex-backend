package com.superinka.formex.controller;

import com.superinka.formex.payload.request.InstructorRatingRequest;
import com.superinka.formex.service.InstructorRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class InstructorRatingController {

    private final InstructorRatingService ratingService;

    @PostMapping
    public ResponseEntity<?> rateInstructor(
            @RequestBody InstructorRatingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ratingService.createRating(request, jwt);
        return ResponseEntity.ok().build();
    }
}


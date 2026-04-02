package com.superinka.formex.service;

import com.superinka.formex.model.User;
import com.superinka.formex.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorGamificationService {

    private final UserRepository userRepository;

    public void addTeachingMinutes(User instructor, int minutes) {

        int addedHours = minutes / 60;
        if (addedHours <= 0) return;

        int previousHours = instructor.getTeachingHours();
        int newTotalHours = previousHours + addedHours;

        // 🔥 bloques de 20 horas
        int previousBlocks = previousHours / 20;
        int newBlocks = newTotalHours / 20;

        int blocksToReward = newBlocks - previousBlocks;

        if (blocksToReward > 0) {
            int pointsToAdd = blocksToReward * 50;
            instructor.setPoints(instructor.getPoints() + pointsToAdd);
        }

        instructor.setTeachingHours(newTotalHours);
        userRepository.save(instructor);
    }
}


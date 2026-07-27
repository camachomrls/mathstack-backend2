package com.mathstack.social.application

import com.mathstack.academic.domain.model.Exercise
import com.mathstack.academic.domain.repository.AcademicRepository
import com.mathstack.admin.domain.repository.AdminChallengeRepository
import com.mathstack.shared.domain.exception.NotFoundException
import java.util.UUID

class GetChallengeExercisesUseCase(
    private val adminChallengeRepository: AdminChallengeRepository,
    private val academicRepository: AcademicRepository
) {
    operator fun invoke(challengeId: UUID, limit: Int = 10): List<Exercise> {
        val challenge = adminChallengeRepository.findById(challengeId)
            ?: throw NotFoundException("Challenge not found")
            
        val subjectId = challenge.subjectId ?: 1
        val difficultyLevel = when (challenge.difficulty.lowercase()) {
            "beginner" -> 1
            "intermediate" -> 2
            "advanced" -> 3
            else -> 2
        }
        
        return academicRepository.listRandomExercisesBySubjectAndDifficulty(
            subjectId = subjectId,
            difficultyLevel = difficultyLevel,
            limit = limit
        )
    }
}

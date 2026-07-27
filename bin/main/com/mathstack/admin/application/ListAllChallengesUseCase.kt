package com.mathstack.admin.application

import com.mathstack.admin.domain.repository.AdminChallengeRepository
import com.mathstack.admin.infrastructure.rest.dto.ChallengeResponse
import com.mathstack.social.domain.repository.SocialRepository

class ListAllChallengesUseCase(
    private val adminChallengeRepository: AdminChallengeRepository,
    private val socialRepository: SocialRepository
) {
    operator fun invoke(): List<ChallengeResponse> {
        return adminChallengeRepository.listAll().map { challenge ->
            ChallengeResponse(
                id = challenge.id.toString(),
                creatorId = "admin",
                status = challenge.status,
                createdAt = challenge.createdAt.toString(),
                title = challenge.title,
                description = challenge.description,
                subjectId = challenge.subjectId,
                difficulty = challenge.difficulty,
                startDate = challenge.startDate?.toString(),
                endDate = challenge.endDate?.toString(),
                rewardCoins = challenge.rewardCoins,
                rewardXP = challenge.rewardXp,
                targetScore = challenge.targetScore,
                participants = socialRepository.getChallengeParticipants(challenge.id).size,
                isActive = challenge.status == "ACTIVE"
            )
        }
    }
}

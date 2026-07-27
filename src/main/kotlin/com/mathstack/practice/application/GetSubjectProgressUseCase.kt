package com.mathstack.practice.application

import com.mathstack.academic.domain.repository.AcademicRepository
import com.mathstack.practice.domain.repository.PracticeRepository
import java.util.UUID

import kotlinx.serialization.Serializable

@Serializable
data class SubjectProgressDto(
    val subjectId: Int,
    val subjectName: String,
    val totalLessons: Int,
    val completedLessons: Int,
    val isMastered: Boolean,
    val progressPercentage: Int
)

class GetSubjectProgressUseCase(
    private val practiceRepository: PracticeRepository,
    private val academicRepository: AcademicRepository
) {
    operator fun invoke(userId: UUID): List<SubjectProgressDto> {
        val diagnostics = practiceRepository.findDiagnosticsByUserId(userId)
        val maxDeficiencyBySubject = diagnostics.groupBy { it.subjectId }
            .mapValues { entry -> entry.value.maxOf { it.deficiencyScore } }
            
        var targetSubjectIds = maxDeficiencyBySubject.filterValues { it >= 20 }.keys.toList()
        if (targetSubjectIds.isEmpty() && maxDeficiencyBySubject.isNotEmpty()) {
            val highest = maxDeficiencyBySubject.maxByOrNull { it.value }?.key ?: 1
            targetSubjectIds = listOf(highest)
        }
        
        val allSubjects = academicRepository.listSubjects()
        val userPaths = practiceRepository.findLearningPathsByUserId(userId)
        val userPathMap = userPaths.associateBy { it.lessonId }

        val relevantSubjects = allSubjects.filter { subject ->
            targetSubjectIds.contains(subject.id) || (maxDeficiencyBySubject[subject.id]?.let { it < 20 } ?: false)
        }

        return relevantSubjects.map { subject ->
            val isMastered = maxDeficiencyBySubject[subject.id]?.let { it < 20 } ?: false
            val lessons = academicRepository.listLessonsBySubject(subject.id)
            
            val totalLessons = lessons.size
            val completedLessons = if (isMastered) {
                totalLessons
            } else {
                lessons.count { lesson -> 
                    userPathMap[lesson.id]?.status == "completed"
                }
            }
            
            val progressPercentage = if (totalLessons == 0) {
                0
            } else if (isMastered) {
                100
            } else {
                ((completedLessons.toDouble() / totalLessons) * 100).toInt()
            }

            SubjectProgressDto(
                subjectId = subject.id,
                subjectName = subject.name,
                totalLessons = totalLessons,
                completedLessons = completedLessons,
                isMastered = isMastered,
                progressPercentage = progressPercentage
            )
        }
    }
}

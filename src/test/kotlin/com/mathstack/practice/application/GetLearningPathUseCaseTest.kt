package com.mathstack.practice.application

import com.mathstack.academic.domain.model.Lesson
import com.mathstack.academic.domain.model.Subject
import com.mathstack.academic.domain.repository.AcademicRepository
import com.mathstack.practice.domain.model.DiagnosticResult
import com.mathstack.practice.domain.repository.PracticeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class GetLearningPathUseCaseTest {

    class MockPracticeRepository : PracticeRepository {
        val diagnostics = mutableListOf<DiagnosticResult>()
        override fun createExerciseAttempt(attempt: com.mathstack.practice.domain.model.ExerciseAttempt): com.mathstack.practice.domain.model.ExerciseAttempt = attempt
        override fun getPracticeSession(userId: UUID, date: java.time.LocalDate): com.mathstack.practice.domain.model.PracticeSession? = null
        override fun createPracticeSession(session: com.mathstack.practice.domain.model.PracticeSession): com.mathstack.practice.domain.model.PracticeSession = session
        override fun updatePracticeSession(session: com.mathstack.practice.domain.model.PracticeSession): com.mathstack.practice.domain.model.PracticeSession = session
        override fun getWeeklyMinutesSpent(userId: UUID): Int = 0
        override fun getPendingLessonsCount(userId: UUID): Int = 0
        override fun createDiagnosticResult(result: DiagnosticResult): DiagnosticResult {
            diagnostics.add(result)
            return result
        }
        override fun createLearningPath(path: com.mathstack.practice.domain.model.LearningPath): com.mathstack.practice.domain.model.LearningPath = path
        override fun upsertLearningPath(path: com.mathstack.practice.domain.model.LearningPath): com.mathstack.practice.domain.model.LearningPath = path
        override fun findAllDiagnostics(): List<DiagnosticResult> = diagnostics
        override fun findDiagnosticsByUserId(userId: UUID): List<DiagnosticResult> = diagnostics.filter { it.userId == userId }
        override fun findAllSessions(): List<com.mathstack.practice.domain.model.PracticeSession> = emptyList()
        override fun findLearningPathsByUserId(userId: UUID): List<com.mathstack.practice.domain.model.LearningPath> = emptyList()
        override fun findAllExerciseAttempts(): List<com.mathstack.practice.domain.model.ExerciseAttempt> = emptyList()
        override fun findAllLearningPaths(): List<com.mathstack.practice.domain.model.LearningPath> = emptyList()
    }

    class MockAcademicRepository : AcademicRepository {
        override fun createSubject(name: String): Subject = Subject(1, name)
        override fun findSubjectById(id: Int): Subject? = listSubjects().find { it.id == id }
        override fun listSubjects(): List<Subject> = listOf(
            Subject(1, "Aritmética"),
            Subject(2, "Álgebra")
        )
        override fun updateSubject(subject: Subject): Subject? = subject
        override fun deleteSubject(id: Int): Boolean = true
        override fun createLessonType(name: String): com.mathstack.academic.domain.model.LessonType = com.mathstack.academic.domain.model.LessonType(1, name)
        override fun findLessonTypeById(id: Int): com.mathstack.academic.domain.model.LessonType? = null
        override fun listLessonTypes(): List<com.mathstack.academic.domain.model.LessonType> = emptyList()
        override fun createLesson(lesson: Lesson): Lesson = lesson
        override fun findLessonById(id: UUID): Lesson? = null
        override fun listLessons(): List<Lesson> = emptyList()
        override fun listLessonsBySubject(subjectId: Int): List<Lesson> {
            return if (subjectId == 1) {
                listOf(
                    Lesson(UUID.randomUUID(), 1, 1, "Suma", 1),
                    Lesson(UUID.randomUUID(), 1, 1, "Fracciones", 2)
                )
            } else if (subjectId == 2) {
                listOf(
                    Lesson(UUID.randomUUID(), 2, 1, "Variables", 1),
                    Lesson(UUID.randomUUID(), 2, 1, "Ecuaciones", 3)
                )
            } else emptyList()
        }
        override fun updateLesson(lesson: Lesson): Lesson? = lesson
        override fun deleteLesson(id: UUID): Boolean = true
        override fun createExercise(exercise: com.mathstack.academic.domain.model.Exercise): com.mathstack.academic.domain.model.Exercise = exercise
        override fun findExerciseById(id: UUID): com.mathstack.academic.domain.model.Exercise? = null
        override fun listAllExercises(): List<com.mathstack.academic.domain.model.Exercise> = emptyList()
        override fun listExercisesByLesson(lessonId: UUID): List<com.mathstack.academic.domain.model.Exercise> = emptyList()
        override fun listRandomExercisesBySubjectAndDifficulty(subjectId: Int, difficultyLevel: Int, limit: Int): List<com.mathstack.academic.domain.model.Exercise> = emptyList()
        override fun updateExercise(exercise: com.mathstack.academic.domain.model.Exercise): com.mathstack.academic.domain.model.Exercise? = exercise
        override fun deleteExercise(id: UUID): Boolean = true
    }

    @Test
    fun `should combine lessons from multiple deficient subjects and sort by difficulty`() {
        val practiceRepo = MockPracticeRepository()
        val academicRepo = MockAcademicRepository()
        val useCase = GetLearningPathUseCase(practiceRepo, academicRepo)

        val userId = UUID.randomUUID()
        
        practiceRepo.createDiagnosticResult(DiagnosticResult(UUID.randomUUID(), userId, 1, 50, LocalDateTime.now()))
        practiceRepo.createDiagnosticResult(DiagnosticResult(UUID.randomUUID(), userId, 2, 30, LocalDateTime.now()))

        val result = useCase(userId)

        assertEquals("Ruta de Reforzamiento", result.subjectName)
        assertEquals(0, result.subjectId)
        assertEquals(4, result.lessons.size)
        
        assertEquals(1, result.lessons[0].difficultyLevel)
        assertEquals(1, result.lessons[1].difficultyLevel)
        assertEquals(2, result.lessons[2].difficultyLevel)
        assertEquals(3, result.lessons[3].difficultyLevel)
        
        assert(result.lessons.any { it.subjectName == "Aritmética" })
        assert(result.lessons.any { it.subjectName == "Álgebra" })
    }
}

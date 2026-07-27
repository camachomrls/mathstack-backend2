package com.mathstack.academic.domain.repository

import com.mathstack.academic.domain.model.Exercise
import com.mathstack.academic.domain.model.Lesson
import com.mathstack.academic.domain.model.LessonType
import com.mathstack.academic.domain.model.Subject
import java.util.UUID

interface AcademicRepository {
    fun createSubject(name: String): Subject
    fun findSubjectById(id: Int): Subject?
    fun listSubjects(): List<Subject>
    fun updateSubject(subject: Subject): Subject?
    fun deleteSubject(id: Int): Boolean

    fun createLessonType(name: String): LessonType
    fun findLessonTypeById(id: Int): LessonType?
    fun listLessonTypes(): List<LessonType>

    fun createLesson(lesson: Lesson): Lesson
    fun findLessonById(id: UUID): Lesson?
    fun listLessons(): List<Lesson>
    fun listLessonsBySubject(subjectId: Int): List<Lesson>
    fun updateLesson(lesson: Lesson): Lesson?
    fun deleteLesson(id: UUID): Boolean

    fun createExercise(exercise: Exercise): Exercise
    fun findExerciseById(id: UUID): Exercise?
    fun listAllExercises(): List<Exercise>
    fun listExercisesByLesson(lessonId: UUID): List<Exercise>
    fun listRandomExercisesBySubjectAndDifficulty(subjectId: Int, difficultyLevel: Int, limit: Int): List<Exercise>
    fun updateExercise(exercise: Exercise): Exercise?
    fun deleteExercise(id: UUID): Boolean
}

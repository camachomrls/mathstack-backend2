package com.mathstack.academic.infrastructure.persistence

import com.mathstack.academic.domain.model.Exercise
import com.mathstack.academic.domain.model.Lesson
import com.mathstack.academic.domain.model.LessonType
import com.mathstack.academic.domain.model.Subject
import com.mathstack.academic.domain.repository.AcademicRepository
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin

class PostgresAcademicRepository : AcademicRepository {
    override fun createSubject(name: String): Subject = transaction {
        val id = SubjectTable.insert { it[SubjectTable.name] = name } get SubjectTable.id
        findSubjectByIdInTransaction(id)!!
    }

    override fun findSubjectById(id: Int): Subject? = transaction { findSubjectByIdInTransaction(id) }

    override fun listSubjects(): List<Subject> = transaction { SubjectTable.selectAll().map { it.toSubject() } }

    override fun updateSubject(subject: Subject): Subject? = transaction {
        if (SubjectTable.update({ SubjectTable.id eq subject.id }) { it[name] = subject.name } == 0) null
        else findSubjectByIdInTransaction(subject.id)
    }

    override fun deleteSubject(id: Int): Boolean = transaction { SubjectTable.deleteWhere { SubjectTable.id eq id } > 0 }

    override fun createLessonType(name: String): LessonType = transaction {
        val id = LessonTypeTable.insert { it[LessonTypeTable.name] = name } get LessonTypeTable.id
        findLessonTypeByIdInTransaction(id)!!
    }

    override fun findLessonTypeById(id: Int): LessonType? = transaction { findLessonTypeByIdInTransaction(id) }

    override fun listLessonTypes(): List<LessonType> = transaction { LessonTypeTable.selectAll().map { it.toLessonType() } }

    override fun createLesson(lesson: Lesson): Lesson = transaction {
        LessonTable.insert {
            it[id] = lesson.id
            it[subjectId] = lesson.subjectId
            it[lessonTypeId] = lesson.lessonTypeId
            it[title] = lesson.title
            it[difficultyLevel] = lesson.difficultyLevel
            it[content] = lesson.content
        }
        findLessonByIdInTransaction(lesson.id)!!
    }

    override fun findLessonById(id: UUID): Lesson? = transaction { findLessonByIdInTransaction(id) }

    override fun listLessons(): List<Lesson> = transaction { LessonTable.selectAll().map { it.toLesson() } }

    override fun listLessonsBySubject(subjectId: Int): List<Lesson> = transaction {
        LessonTable.selectAll().where { LessonTable.subjectId eq subjectId }.map { it.toLesson() }
    }

    override fun updateLesson(lesson: Lesson): Lesson? = transaction {
        val updated = LessonTable.update({ LessonTable.id eq lesson.id }) {
            it[subjectId] = lesson.subjectId
            it[lessonTypeId] = lesson.lessonTypeId
            it[title] = lesson.title
            it[difficultyLevel] = lesson.difficultyLevel
            it[content] = lesson.content
        }
        if (updated == 0) null else findLessonByIdInTransaction(lesson.id)
    }

    override fun deleteLesson(id: UUID): Boolean = transaction { LessonTable.deleteWhere { LessonTable.id eq id } > 0 }

    override fun createExercise(exercise: Exercise): Exercise = transaction {
        ExerciseTable.insert {
            it[id] = exercise.id
            it[lessonId] = exercise.lessonId
            it[content] = exercise.content
            it[conceptTested] = exercise.conceptTested
        }
        findExerciseByIdInTransaction(exercise.id)!!
    }

    override fun findExerciseById(id: UUID): Exercise? = transaction { findExerciseByIdInTransaction(id) }

    override fun listAllExercises(): List<Exercise> = transaction {
        ExerciseTable.selectAll().map { it.toExercise() }
    }

    override fun listExercisesByLesson(lessonId: UUID): List<Exercise> = transaction {
        ExerciseTable.selectAll().where { ExerciseTable.lessonId eq lessonId }.map { it.toExercise() }
    }

    override fun listRandomExercisesBySubjectAndDifficulty(subjectId: Int, difficultyLevel: Int, limit: Int): List<Exercise> = transaction {
        (ExerciseTable innerJoin LessonTable)
            .selectAll()
            .where { (LessonTable.subjectId eq subjectId) and (LessonTable.difficultyLevel eq difficultyLevel) }
            .orderBy(org.jetbrains.exposed.sql.Random())
            .limit(limit)
            .map { it.toExercise() }
    }

    override fun updateExercise(exercise: Exercise): Exercise? = transaction {
        val updated = ExerciseTable.update({ ExerciseTable.id eq exercise.id }) {
            it[lessonId] = exercise.lessonId
            it[content] = exercise.content
            it[conceptTested] = exercise.conceptTested
        }
        if (updated == 0) null else findExerciseByIdInTransaction(exercise.id)
    }

    override fun deleteExercise(id: UUID): Boolean = transaction { ExerciseTable.deleteWhere { ExerciseTable.id eq id } > 0 }

    private fun findSubjectByIdInTransaction(id: Int): Subject? =
        SubjectTable.selectAll().where { SubjectTable.id eq id }.singleOrNull()?.toSubject()

    private fun findLessonTypeByIdInTransaction(id: Int): LessonType? =
        LessonTypeTable.selectAll().where { LessonTypeTable.id eq id }.singleOrNull()?.toLessonType()

    private fun findLessonByIdInTransaction(id: UUID): Lesson? =
        LessonTable.selectAll().where { LessonTable.id eq id }.singleOrNull()?.toLesson()

    private fun findExerciseByIdInTransaction(id: UUID): Exercise? =
        ExerciseTable.selectAll().where { ExerciseTable.id eq id }.singleOrNull()?.toExercise()
}

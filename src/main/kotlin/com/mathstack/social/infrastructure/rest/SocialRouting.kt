package com.mathstack.social.infrastructure.rest

import com.mathstack.social.application.AcceptFriendRequestUseCase
import com.mathstack.social.application.CreateChallengeCommand
import com.mathstack.social.application.CreateChallengeUseCase
import com.mathstack.social.application.ListFriendsUseCase
import com.mathstack.social.application.SendFriendRequestCommand
import com.mathstack.social.application.SendFriendRequestUseCase
import com.mathstack.social.application.SubmitChallengeResultCommand
import com.mathstack.social.application.SubmitChallengeResultUseCase
import com.mathstack.social.infrastructure.rest.dto.CreateChallengeRequest
import com.mathstack.social.infrastructure.rest.dto.SendFriendRequest
import com.mathstack.social.infrastructure.rest.dto.SubmitChallengeResultRequest
import com.mathstack.social.infrastructure.rest.dto.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.socialRouting() {
    val sendFriendRequest by inject<SendFriendRequestUseCase>()
    val acceptFriendRequest by inject<AcceptFriendRequestUseCase>()
    val listFriends by inject<ListFriendsUseCase>()
    val createChallenge by inject<CreateChallengeUseCase>()
    val submitChallengeResult by inject<SubmitChallengeResultUseCase>()
    val listAllChallengesUseCase by inject<com.mathstack.admin.application.ListAllChallengesUseCase>()

    authenticate("auth-jwt") {
        route("/api/v1/social") {
            route("/friends") {
                post("/request") {
                    val principal = call.principal<JWTPrincipal>() ?: throw com.mathstack.shared.domain.exception.UnauthorizedException("Token missing")
                    val userIdStr = principal.payload.getClaim("user_id")?.asString() ?: principal.payload.subject
                    val userId = UUID.fromString(userIdStr)
                    val request = call.receive<SendFriendRequest>()
                    
                    val command = SendFriendRequestCommand(
                        userId = userId,
                        friendId = UUID.fromString(request.friendId)
                    )
                    val friendRequest = sendFriendRequest(command)
                    call.respond(HttpStatusCode.Created, friendRequest.toResponse())
                }

                put("/{friendId}/accept") {
                    val principal = call.principal<JWTPrincipal>() ?: throw com.mathstack.shared.domain.exception.UnauthorizedException("Token missing")
                    val userIdStr = principal.payload.getClaim("user_id")?.asString() ?: principal.payload.subject
                    val userId = UUID.fromString(userIdStr)
                    val friendIdStr = call.parameters["friendId"] ?: throw IllegalArgumentException("friendId is required")
                    val friendId = UUID.fromString(friendIdStr)
                    
                    val friendRequest = acceptFriendRequest(userId, friendId)
                    call.respond(HttpStatusCode.OK, friendRequest.toResponse())
                }

                get {
                    val principal = call.principal<JWTPrincipal>() ?: throw com.mathstack.shared.domain.exception.UnauthorizedException("Token missing")
                    val userIdStr = principal.payload.getClaim("user_id")?.asString() ?: principal.payload.subject
                    val userId = UUID.fromString(userIdStr)
                    
                    val friends = listFriends(userId)
                    call.respond(HttpStatusCode.OK, friends.map { it.toString() })
                }
            }

            route("/challenges") {
                get("/global") {
                    val challenges = listAllChallengesUseCase().filter { it.isActive }
                    call.respond(HttpStatusCode.OK, challenges)
                }

                post {
                    val principal = call.principal<JWTPrincipal>() ?: throw com.mathstack.shared.domain.exception.UnauthorizedException("Token missing")
                    val userIdStr = principal.payload.getClaim("user_id")?.asString() ?: principal.payload.subject
                    val userId = UUID.fromString(userIdStr)
                    val request = call.receive<CreateChallengeRequest>()
                    
                    val command = CreateChallengeCommand(
                        creatorId = userId,
                        exerciseId = UUID.fromString(request.exerciseId),
                        friendIds = request.friendIds.map { UUID.fromString(it) }
                    )
                    val challenge = createChallenge(command)
                    call.respond(HttpStatusCode.Created, challenge.toResponse())
                }

                post("/{challengeId}/submit") {
                    val principal = call.principal<JWTPrincipal>() ?: throw com.mathstack.shared.domain.exception.UnauthorizedException("Token missing")
                    val userIdStr = principal.payload.getClaim("user_id")?.asString() ?: principal.payload.subject
                    val userId = UUID.fromString(userIdStr)
                    val challengeIdStr = call.parameters["challengeId"] ?: throw IllegalArgumentException("challengeId is required")
                    val challengeId = UUID.fromString(challengeIdStr)
                    val request = call.receive<SubmitChallengeResultRequest>()
                    
                    val command = SubmitChallengeResultCommand(
                        challengeId = challengeId,
                        userId = userId,
                        score = request.score,
                        timeTakenSeconds = request.timeTakenSeconds
                    )
                    val participant = submitChallengeResult(command)
                    call.respond(HttpStatusCode.OK, participant.toResponse())
                }

                get("/{challengeId}/exercises") {
                    val challengeIdStr = call.parameters["challengeId"] ?: throw IllegalArgumentException("challengeId is required")
                    val challengeId = UUID.fromString(challengeIdStr)
                    
                    val getChallengeExercisesUseCase by call.application.inject<com.mathstack.social.application.GetChallengeExercisesUseCase>()
                    val exercises = getChallengeExercisesUseCase(challengeId, 10)
                    
                    val response = exercises.map { 
                        com.mathstack.academic.infrastructure.rest.dto.ExerciseResponse(
                            id = it.id.toString(),
                            lessonId = it.lessonId.toString(),
                            content = it.content,
                            conceptTested = it.conceptTested
                        ) 
                    }
                    call.respond(HttpStatusCode.OK, response)
                }
            }
        }
    }
}

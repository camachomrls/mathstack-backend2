package com.mathstack.social

import com.mathstack.social.application.AcceptFriendRequestUseCase
import com.mathstack.social.application.CreateChallengeUseCase
import com.mathstack.social.application.ListFriendsUseCase
import com.mathstack.social.application.SendFriendRequestUseCase
import com.mathstack.social.application.SubmitChallengeResultUseCase
import com.mathstack.social.application.CreateGroupUseCase
import com.mathstack.social.application.AddGroupMemberUseCase
import com.mathstack.social.application.ListGroupsUseCase
import com.mathstack.social.application.GetGroupDetailsUseCase
import com.mathstack.social.application.UpdateGroupActiveLevelUseCase
import com.mathstack.social.domain.repository.SocialRepository
import com.mathstack.social.domain.repository.GroupRepository
import com.mathstack.social.infrastructure.persistence.PostgresSocialRepository
import com.mathstack.social.infrastructure.persistence.PostgresGroupRepository
import org.koin.dsl.module

val socialModule = module {
    single<SocialRepository> { PostgresSocialRepository() }
    single<GroupRepository> { PostgresGroupRepository() }
    
    single { SendFriendRequestUseCase(get()) }
    single { AcceptFriendRequestUseCase(get()) }
    single { ListFriendsUseCase(get()) }
    single { CreateChallengeUseCase(get()) }
    single { SubmitChallengeResultUseCase(get()) }
    single { com.mathstack.social.application.GetChallengeExercisesUseCase(get(), get()) }
    
    single { CreateGroupUseCase(get()) }
    single { AddGroupMemberUseCase(get(), get()) }
    single { ListGroupsUseCase(get()) }
    single { GetGroupDetailsUseCase(get(), get()) }
    single { UpdateGroupActiveLevelUseCase(get()) }
}

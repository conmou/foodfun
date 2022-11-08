package edu.foodfun.usecase

import edu.foodfun.repository.PartyRepository
import edu.foodfun.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveUserFromPartyUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val partyRepository: PartyRepository,
) {
    suspend operator fun invoke(userId: String, partyId: String): Boolean = withContext(
        Dispatchers.Default) {
        userRepository.deleteCurrentParty(userId)
        partyRepository.removePrepare(userId, partyId)
        partyRepository.removeUser(userId, partyId)
        return@withContext true
    }
}
package edu.foodfun.usecase

import edu.foodfun.repository.PartyRepository
import edu.foodfun.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddUserToPartyUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val partyRepository: PartyRepository,
) {
    suspend operator fun invoke(userId: String, partyId: String): Boolean = withContext(
        Dispatchers.Default) {
        userRepository.updateCurrentParty(userId, partyId)
        partyRepository.addUser(userId, partyId)
        return@withContext true
    }
}
package edu.foodfun.hilt

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.foodfun.deserializer.*
import edu.foodfun.model.Message
import edu.foodfun.model.Restaurant
import edu.foodfun.model.User
import edu.foodfun.repository.PartyRepository
import edu.foodfun.repository.RestaurantRepository
import edu.foodfun.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): MyApplication {
        return app as MyApplication
    }

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Singleton
    @Provides
    fun provideUserRepository(userCommentDeserializer: UserCommentDocumentDeserializer ,userDeserializer: UserDocumentDeserializer, @ApplicationContext context: Context): UserRepository = UserRepository(userCommentDeserializer, userDeserializer, context)

    @Singleton
    @Provides
    fun provideRestaurantRepository(deserializer: RestaurantDocumentDeserializer, @ApplicationContext context: Context): RestaurantRepository = RestaurantRepository(deserializer, context)

    @Singleton
    @Provides
    fun providePartyRepository(partyDeserializer: PartyDocumentDeserializer, messageDeserializer: MessageDeserializer, @ApplicationContext context: Context): PartyRepository = PartyRepository(partyDeserializer, messageDeserializer, context)

    @Singleton
    @Provides
    fun provideUserDocumentDeserializer(deserializer: UserDocumentDeserializer): IFirestoreDocumentDeserializer<User> = deserializer

    @Singleton
    @Provides
    fun provideRestaurantDocumentDeserializer(deserializer: RestaurantDocumentDeserializer): IFirestoreDocumentDeserializer<Restaurant> = deserializer

    @Singleton
    @Provides
    fun provideChatRoomDocumentDeserializer(deserializer: MessageDocumentDeserializer): IFirestoreDocumentDeserializer<Message> = deserializer
}

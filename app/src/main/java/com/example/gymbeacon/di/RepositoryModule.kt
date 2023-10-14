package com.example.gymbeacon.di

import com.example.data.datasource.remote.UserRemoteDataSource
import com.example.data.repository.UserRepositoryImpl
import com.example.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideUserRepository(
        userRemoteDataSource: UserRemoteDataSource
    ): UserRepository{
        return UserRepositoryImpl(userRemoteDataSource)
    }
}
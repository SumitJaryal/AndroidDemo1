package com.paydat.di

import android.content.Context
import androidx.room.Room
import com.paydat.data.local.AppDatabase
import com.paydat.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext, AppDatabase::class.java, "payDat.db"
        ).build()
    }

    @Provides
    fun provideTestUser(appDatabase: AppDatabase): UserDao {
        return appDatabase.UserDao()
    }
}
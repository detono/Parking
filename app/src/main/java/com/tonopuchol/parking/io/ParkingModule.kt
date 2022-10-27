package com.tonopuchol.parking.io

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
abstract class ParkingModule {
    @Binds
    abstract fun getRepository(repo: ParkingRepositoryImpl): ParkingRepository
}
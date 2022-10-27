package com.tonopuchol.parking.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonopuchol.parking.ghent.ParkingSort
import com.tonopuchol.parking.io.ParkingRepository
import com.tonopuchol.parking.io.ParkingRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: ParkingRepository
): ViewModel() {
    val data = repo.getParkingData(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sortMethod = savedStateHandle.getStateFlow("sort", initialValue = ParkingSort.NAME).flatMapLatest {
        return@flatMapLatest MutableStateFlow(it)
    }

    //val sortMethod = MutableStateFlow(ParkingSort.NAME)
    val sortAscending = MutableStateFlow(true)

    init {
        refreshData()
    }

    fun changeSortMethod(method: ParkingSort) {
        savedStateHandle["sort"] = method
    }

    fun changeSortDirection(ascending: Boolean) {
        viewModelScope.launch {
            sortAscending.emit(ascending)
        }
    }

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.refreshParkingData()
        }
    }
}
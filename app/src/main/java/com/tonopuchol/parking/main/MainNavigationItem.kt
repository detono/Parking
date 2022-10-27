package com.tonopuchol.parking.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tonopuchol.parking.R

sealed class MainNavigationItem(@StringRes val title: Int, @DrawableRes val icon: Int, var route: String) {
    object Map : MainNavigationItem(R.string.view_map, R.drawable.ic_map, "map")
    object List: MainNavigationItem(R.string.view_list, R.drawable.ic_list, "list")
}
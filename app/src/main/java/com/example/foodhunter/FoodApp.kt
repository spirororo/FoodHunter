package com.example.foodhunter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// без этой аннотации хилт просто не запустится
@HiltAndroidApp
class FoodApp : Application()

package com.spendsense

import android.app.Application
import com.spendsense.features.finance.data.AppGraph

class SpendSenseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.initialize(this)
    }
}

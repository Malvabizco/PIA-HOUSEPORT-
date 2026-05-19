package com.example.pia_claseordinaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pia_claseordinaria.ui.screens.GuardAppointmentsScreen
import com.example.pia_claseordinaria.ui.theme.HousePortTheme
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel

class GuardAppointmentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = CondoViewModel()
        setContent {
            HousePortTheme {
                GuardAppointmentsScreen(viewModel) { finish() }
            }
        }
    }
}

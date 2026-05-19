package com.example.pia_claseordinaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pia_claseordinaria.ui.screens.AdminServiceScreen
import com.example.pia_claseordinaria.ui.theme.HousePortTheme
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel

class AdminServiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = CondoViewModel()
        setContent {
            HousePortTheme {
                AdminServiceScreen(viewModel) { finish() }
            }
        }
    }
}

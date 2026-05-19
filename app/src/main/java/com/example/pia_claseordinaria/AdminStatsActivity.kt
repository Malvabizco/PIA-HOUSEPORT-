package com.example.pia_claseordinaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pia_claseordinaria.ui.screens.AdminStatsScreen
import com.example.pia_claseordinaria.ui.theme.HousePortTheme
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel

class AdminStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = CondoViewModel()
        // Cargamos todas las facturas del condominio para el Admin
        viewModel.loadAllFacturas()
        setContent {
            HousePortTheme {
                AdminStatsScreen(viewModel) { finish() }
            }
        }
    }
}

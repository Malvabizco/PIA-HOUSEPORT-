package com.example.pia_claseordinaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pia_claseordinaria.ui.screens.FacturasScreen
import com.example.pia_claseordinaria.ui.theme.HousePortTheme
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel
import com.google.firebase.auth.FirebaseAuth

class PaymentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = CondoViewModel()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        setContent {
            HousePortTheme {
                FacturasScreen(viewModel, userId) { finish() }
            }
        }
    }
}

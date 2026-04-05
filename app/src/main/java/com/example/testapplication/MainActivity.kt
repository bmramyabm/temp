package com.example.testapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.testapplication.ui.ShopApp
import com.example.testapplication.ui.theme.TestApplicationTheme

/**
 * MainActivity.kt
 *
 * The main entry point for the Fake Store application.
 * This activity hosts the Compose UI and sets up the application theme.
 *
 * Architecture Overview:
 * - MVVM (Model-View-ViewModel) pattern is used throughout
 * - Model: Data classes in the model package (Product, CartItem, ShopUiState)
 * - View: Composable functions in the ui package (ShopScreen.kt)
 * - ViewModel: ShopViewModel handles all business logic and state management
 *
 * Navigation:
 * - No navigation library is used
 * - Navigation is managed by the ViewModel using a sealed class (NavigationState)
 * - Two screens: Shopping (product list) and Cart (checkout)
 *
 * Data:
 * - FakeRepository generates 1000 products across three categories
 * - Products include Food, House items, and Technology
 * - Data is emitted via Flow to simulate network behavior
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestApplicationTheme {
                ShopApp()
            }
        }
    }
}

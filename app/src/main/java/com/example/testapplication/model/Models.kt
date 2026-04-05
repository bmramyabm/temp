package com.example.testapplication.model

/**
 * Models.kt
 *
 * This file contains all the data models used throughout the fake store application.
 * It includes the Product model, CartItem model, and navigation state definitions.
 */

/**
 * Represents a product in the store.
 *
 * @property id Unique identifier for the product
 * @property title The display name of the product
 * @property description A brief description of the product
 * @property price The cost of the product in dollars
 * @property imageUrl URL to the product image (uses placeholder images)
 * @property category The category this product belongs to (FOOD, HOUSE, TECHNOLOGY)
 */
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: ProductCategory
)

/**
 * Enum representing the available product categories in the store.
 */
enum class ProductCategory {
    FOOD,
    HOUSE,
    TECHNOLOGY
}

/**
 * Represents an item in the shopping cart.
 *
 * @property product The product that was added to the cart
 * @property quantity The number of units of this product in the cart
 */
data class CartItem(
    val product: Product,
    val quantity: Int = 1
)

/**
 * Sealed class representing the navigation state of the application.
 * The ViewModel uses this to determine which screen to display.
 * No navigation library is used; instead, the ViewModel manages screen transitions.
 */
sealed class NavigationState {
    /**
     * The main shopping screen showing the product list and search functionality.
     */
    data object Shopping : NavigationState()

    /**
     * The cart screen showing items added to the cart and checkout option.
     */
    data object Cart : NavigationState()
}

/**
 * Enum representing the available sorting options for products.
 *
 * @property displayName Human-readable name for the sort option
 */
enum class SortOption(val displayName: String) {
    /**
     * Sort products alphabetically by title (A-Z).
     */
    ALPHABETICAL("A-Z"),

    /**
     * Sort products by price from lowest to highest.
     */
    PRICE_LOW_TO_HIGH("Price: Low to High"),

    /**
     * Sort products by price from highest to lowest.
     */
    PRICE_HIGH_TO_LOW("Price: High to Low")
}

/**
 * Represents the complete UI state for the shop application.
 *
 * @property products The full list of products from the repository
 * @property filteredProducts Products filtered by the current search query and sorted
 * @property cartItems Items currently in the shopping cart
 * @property searchQuery The current search text entered by the user
 * @property sortOption Current sort option applied to products
 * @property navigationState Current navigation state (Shopping or Cart)
 * @property isLoading Whether products are currently being loaded
 */
data class ShopUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.ALPHABETICAL,
    val navigationState: NavigationState = NavigationState.Shopping,
    val isLoading: Boolean = true
) {
    /**
     * Calculates the total price of all items in the cart.
     */
    val cartTotal: Double
        get() = cartItems.sumOf { it.product.price * it.quantity }

    /**
     * Returns the total number of items in the cart.
     */
    val cartItemCount: Int
        get() = cartItems.sumOf { it.quantity }
}
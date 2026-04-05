package com.example.testapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapplication.model.NavigationState
import com.example.testapplication.model.Product
import com.example.testapplication.model.ShopUiState
import com.example.testapplication.model.SortOption
import com.example.testapplication.repository.CartRepository
import com.example.testapplication.repository.ProductRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ShopViewModel.kt
 *
 * The single ViewModel for the fake store application.
 * Manages all UI state including products, cart, search filtering, and navigation.
 * Follows MVVM architecture pattern with unidirectional data flow.
 */

/**
 * ViewModel responsible for managing the shop's UI state and business logic.
 * Handles product loading, cart operations, search filtering, and navigation.
 *
 * @property productRepository The data repository for fetching products (defaults to ProductRepository)
 * @property cartRepository The data repository for cart operations (defaults to CartRepository)
 */
class ShopViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
    private val cartRepository: CartRepository = CartRepository()
) : ViewModel() {

    /**
     * Private mutable shared flow for internal state updates.
     */
    private val _uiState: MutableSharedFlow<ShopUiState> = MutableSharedFlow(replay = 1)

    /**
     * Current state holder for atomic updates.
     */
    private var currentState = ShopUiState()


    /**
     * Public immutable shared flow exposed to the UI layer.
     * UI components should collect this flow to observe state changes.
     */
    val uiState: SharedFlow<ShopUiState> = _uiState.asSharedFlow()

    /**
     * Updates the current state and emits it to the shared flow.
     */
    private fun updateState(transform: (ShopUiState) -> ShopUiState) {
        currentState = transform(currentState)
        _uiState.tryEmit(currentState)
    }

    /**
     * Loads products from the repository.
     * Should be called from a LaunchedEffect in the UI layer to trigger initial data loading.
     * Updates the UI state with loading status and product data.
     * Applies the default sort option after loading.
     */
    fun loadProducts() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            productRepository.getProducts().collect { products ->
                updateState { state ->
                    val sortedProducts = applySorting(products, state.sortOption)
                    state.copy(
                        products = products,
                        filteredProducts = sortedProducts,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Applies the specified sorting option to a list of products.
     *
     * @param products The list of products to sort
     * @param sortOption The sorting option to apply
     * @return Sorted list of products
     */
    private fun applySorting(products: List<Product>, sortOption: SortOption): List<Product> {
        return when (sortOption) {
            SortOption.ALPHABETICAL -> products.sortedBy { it.title.lowercase() }
            SortOption.PRICE_LOW_TO_HIGH -> products.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> products.sortedByDescending { it.price }
        }
    }

    /**
     * Updates the search query and filters products accordingly.
     * Implements a simple fuzzy search that matches characters sequentially
     * in the product title (case-insensitive).
     * Maintains the current sort order after filtering.
     *
     * @param query The search text entered by the user
     */
    fun onSearchQueryChanged(query: String) {
        updateState { state ->
            val filtered = if (query.isBlank()) {
                state.products
            } else {
                state.products.filter { product ->
                    fuzzyMatch(product.title, query)
                }
            }
            val sortedFiltered = applySorting(filtered, state.sortOption)
            state.copy(
                searchQuery = query,
                filteredProducts = sortedFiltered
            )
        }
    }

    /**
     * Updates the sort option and re-sorts the current filtered products.
     *
     * @param sortOption The new sort option to apply
     */
    fun onSortOptionChanged(sortOption: SortOption) {
        updateState { state ->
            val sortedProducts = applySorting(state.filteredProducts, sortOption)
            state.copy(
                sortOption = sortOption,
                filteredProducts = sortedProducts
            )
        }
    }

    /**
     * Performs a simple fuzzy search matching.
     * Checks if all characters in the query appear in the target string
     * in the same order (not necessarily consecutive).
     *
     * Example: "cmp" matches "Computer" because c, m, p appear in order.
     *
     * @param target The string to search within (product title)
     * @param query The search query from the user
     * @return True if the query fuzzy-matches the target
     */
    private fun fuzzyMatch(target: String, query: String): Boolean {
        val lowerTarget = target.lowercase()
        val lowerQuery = query.lowercase()

        var targetIndex = 0
        var queryIndex = 0

        while (targetIndex < lowerTarget.length && queryIndex < lowerQuery.length) {
            if (lowerTarget[targetIndex] == lowerQuery[queryIndex]) {
                queryIndex++
            }
            targetIndex++
        }

        return queryIndex == lowerQuery.length
    }

    /**
     * Adds a product to the shopping cart.
     * If the product already exists in the cart, increments its quantity.
     * Operation is asynchronous with artificial delay.
     *
     * @param product The product to add to the cart
     * I m adding comments
     */
    fun addToCart(product: Product) {
        viewModelScope.launch {
            val updatedCart = cartRepository.addToCart(product)
            updateState { state ->
                state.copy(cartItems = updatedCart)
            }
        }
    }

    /**
     * Removes one unit of a product from the cart.
     * If the quantity becomes zero, the item is removed entirely.
     * Operation is asynchronous with artificial delay.
     *
     * @param product The product to remove from the cart
     */
    fun removeFromCart(product: Product) {
        viewModelScope.launch {
            val updatedCart = cartRepository.removeFromCart(product)
            updateState { state ->
                state.copy(cartItems = updatedCart)
            }
        }
    }

    /**
     * Removes all units of a specific product from the cart.
     * Operation is asynchronous with artificial delay.
     *
     * @param product The product to completely remove from the cart
     */
    fun removeAllFromCart(product: Product) {
        viewModelScope.launch {
            val updatedCart = cartRepository.removeAllFromCart(product)
            updateState { state ->
                state.copy(cartItems = updatedCart)
            }
        }
    }

    /**
     * Navigates to the cart screen.
     * Updates the navigation state to Cart.
     */
    fun navigateToCart() {
        updateState { it.copy(navigationState = NavigationState.Cart) }
    }

    /**
     * Navigates back to the shopping screen.
     * Updates the navigation state to Shopping.
     */
    fun navigateToShopping() {
        updateState { it.copy(navigationState = NavigationState.Shopping) }
    }

    /**
     * Processes the payment (simulated).
     * Clears the cart and navigates back to the shopping screen.
     * In a real application, this would integrate with a payment processor.
     * Operation is asynchronous with artificial delay.
     */
    fun processPayment() {
        viewModelScope.launch {
            val updatedCart = cartRepository.clearCart()
            updateState { state ->
                state.copy(
                    cartItems = updatedCart,
                    navigationState = NavigationState.Shopping
                )
            }
        }
    }

    /**
     * Clears the search query and shows all products.
     */
    fun clearSearch() {
        onSearchQueryChanged("")
    }
}

package com.example.testapplication.viewmodel

import com.example.testapplication.model.CartItem
import com.example.testapplication.model.NavigationState
import com.example.testapplication.model.Product
import com.example.testapplication.model.ProductCategory
import com.example.testapplication.model.SortOption
import com.example.testapplication.repository.CartRepository
import com.example.testapplication.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * ShopViewModelTest.kt
 *
 * Comprehensive unit tests for ShopViewModel using JUnit5.
 * Tests cover all ViewModel functionality including:
 * - Initial state and product loading
 * - Search and fuzzy filtering
 * - Cart operations (add, remove, quantity management)
 * - Navigation state management
 * - Payment processing
 *
 * Uses:
 * - JUnit5 for test framework with nested classes and parameterized tests
 * - Turbine for Flow testing
 * - kotlinx-coroutines-test for coroutine testing
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    /**
     * Test repository that returns a controlled set of products.
     * Allows for predictable testing without the 1000 item overhead.
     */
    private class TestProductRepository(
        private val testProducts: List<Product> = createTestProducts()
    ) : ProductRepository() {
        override fun getProducts(): Flow<List<Product>> = flow {
            emit(testProducts)
        }

        companion object {
            fun createTestProducts(): List<Product> = listOf(
                Product(
                    id = 1,
                    title = "Computer 8GB RAM",
                    description = "Desktop computer with 8GB RAM",
                    price = 499.99,
                    imageUrl = "https://example.com/computer1.jpg",
                    category = ProductCategory.TECHNOLOGY
                ),
                Product(
                    id = 2,
                    title = "Laptop 16GB RAM",
                    description = "Portable laptop with 16GB RAM",
                    price = 899.99,
                    imageUrl = "https://example.com/laptop.jpg",
                    category = ProductCategory.TECHNOLOGY
                ),
                Product(
                    id = 3,
                    title = "Organic Apples",
                    description = "Fresh organic apples",
                    price = 4.99,
                    imageUrl = "https://example.com/apples.jpg",
                    category = ProductCategory.FOOD
                ),
                Product(
                    id = 4,
                    title = "Bath Towel",
                    description = "Soft cotton bath towel",
                    price = 19.99,
                    imageUrl = "https://example.com/towel.jpg",
                    category = ProductCategory.HOUSE
                ),
                Product(
                    id = 5,
                    title = "Smartphone 128GB",
                    description = "Modern smartphone",
                    price = 699.99,
                    imageUrl = "https://example.com/phone.jpg",
                    category = ProductCategory.TECHNOLOGY
                )
            )
        }
    }

    /**
     * Test cart repository that operates synchronously without delays.
     * Allows for predictable testing of cart operations.
     */
    private class TestCartRepository : CartRepository() {
        private val cartItems = mutableListOf<CartItem>()

        override suspend fun getCartItems(): List<CartItem> = cartItems.toList()

        override suspend fun addToCart(product: Product): List<CartItem> {
            val existingItem = cartItems.find { it.product.id == product.id }
            if (existingItem != null) {
                val index = cartItems.indexOf(existingItem)
                cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
            } else {
                cartItems.add(CartItem(product = product, quantity = 1))
            }
            return cartItems.toList()
        }

        override suspend fun removeFromCart(product: Product): List<CartItem> {
            val existingItem = cartItems.find { it.product.id == product.id }
            if (existingItem != null) {
                if (existingItem.quantity > 1) {
                    val index = cartItems.indexOf(existingItem)
                    cartItems[index] = existingItem.copy(quantity = existingItem.quantity - 1)
                } else {
                    cartItems.remove(existingItem)
                }
            }
            return cartItems.toList()
        }

        override suspend fun removeAllFromCart(product: Product): List<CartItem> {
            cartItems.removeAll { it.product.id == product.id }
            return cartItems.toList()
        }

        override suspend fun clearCart(): List<CartItem> {
            cartItems.clear()
            return cartItems.toList()
        }
    }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Creates a ViewModel instance with test repositories for testing.
     * Optionally triggers loadProducts() to simulate the LaunchedEffect behavior.
     *
     * @param productRepository The product repository to use
     * @param cartRepository The cart repository to use
     * @param loadProducts Whether to call loadProducts() (default true to match UI behavior)
     */
    private fun createViewModel(
        productRepository: ProductRepository = TestProductRepository(),
        cartRepository: CartRepository = TestCartRepository(),
        loadProducts: Boolean = true
    ): ShopViewModel {
        return ShopViewModel(productRepository, cartRepository).also {
            if (loadProducts) {
                it.loadProducts()
            }
        }
    }

    @Nested
    @DisplayName("Initial State Tests")
    inner class InitialStateTests {

        @Test
        @DisplayName("Initial state should have loading true before loadProducts is called")
        fun initialStateShouldHaveLoadingTrueBeforeLoad() = runTest {
            val viewModel = createViewModel(loadProducts = false)
            viewModel.loadProducts()
            val initialState = viewModel.uiState.first()
            assertTrue(initialState.isLoading)
        }

        @Test
        @DisplayName("Initial state should have empty cart")
        fun initialStateShouldHaveEmptyCart() = runTest {
            val viewModel = createViewModel()

            val initialState = viewModel.uiState.first()
            assertTrue(initialState.cartItems.isEmpty())
            assertEquals(0, initialState.cartItemCount)
            assertEquals(0.0, initialState.cartTotal)
        }

        @Test
        @DisplayName("Initial navigation state should be Shopping")
        fun initialNavigationStateShouldBeShopping() = runTest {
            val viewModel = createViewModel()

            val initialState = viewModel.uiState.first()
            assertEquals(NavigationState.Shopping, initialState.navigationState)
        }

        @Test
        @DisplayName("Initial search query should be empty")
        fun initialSearchQueryShouldBeEmpty() = runTest {
            val viewModel = createViewModel()

            val initialState = viewModel.uiState.first()
            assertEquals("", initialState.searchQuery)
        }
    }

    @Nested
    @DisplayName("Product Loading Tests")
    inner class ProductLoadingTests {

        @Test
        @DisplayName("Products should be loaded after initialization")
        fun productsShouldBeLoadedAfterInit() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val loadedState = viewModel.uiState.first()
            assertFalse(loadedState.isLoading)
            assertEquals(5, loadedState.products.size)
            assertEquals(5, loadedState.filteredProducts.size)
        }

        @Test
        @DisplayName("filteredProducts should contain same items as products but sorted")
        fun filteredProductsShouldContainSameItemsAsProductsButSorted() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val loadedState = viewModel.uiState.first()
            // Same count
            assertEquals(loadedState.products.size, loadedState.filteredProducts.size)
            // Same items (regardless of order)
            assertEquals(
                loadedState.products.sortedBy { it.id }.map { it.id },
                loadedState.filteredProducts.sortedBy { it.id }.map { it.id }
            )
            // filteredProducts should be sorted alphabetically by default
            val titles = loadedState.filteredProducts.map { it.title }
            assertEquals(titles.sortedBy { it.lowercase() }, titles)
        }

        @Test
        @DisplayName("Should handle empty product list")
        fun shouldHandleEmptyProductList() = runTest {
            val emptyRepository = TestProductRepository(emptyList())
            val viewModel = createViewModel(productRepository = emptyRepository)
            testScheduler.advanceUntilIdle()

            val loadedState = viewModel.uiState.first()
            assertTrue(loadedState.products.isEmpty())
            assertFalse(loadedState.isLoading)
        }
    }

    @Nested
    @DisplayName("Search and Filter Tests")
    inner class SearchAndFilterTests {

        @Test
        @DisplayName("Search query should update state")
        fun searchQueryShouldUpdateState() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSearchQueryChanged("Computer")
            testScheduler.advanceUntilIdle()

            val searchState = viewModel.uiState.first()
            assertEquals("Computer", searchState.searchQuery)
        }

        @Test
        @DisplayName("Search should filter products by title")
        fun searchShouldFilterProductsByTitle() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSearchQueryChanged("Computer")
            testScheduler.advanceUntilIdle()

            val searchState = viewModel.uiState.first()
            assertEquals(1, searchState.filteredProducts.size)
            assertEquals("Computer 8GB RAM", searchState.filteredProducts[0].title)
        }

        @Test
        @DisplayName("Fuzzy search 'cmp' should match Computer only")
        fun fuzzySearchCmpShouldMatchComputerOnly() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSearchQueryChanged("cmp")
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(1, state.filteredProducts.size)
            assertTrue(state.filteredProducts[0].title.contains("Computer"))
        }

        @Test
        @DisplayName("Fuzzy search 'ltp' should match Laptop only")
        fun fuzzySearchLtpShouldMatchLaptopOnly() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSearchQueryChanged("ltp")
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(1, state.filteredProducts.size)
            assertTrue(state.filteredProducts[0].title.contains("Laptop"))
        }

        @Test
        @DisplayName("Fuzzy search 'RAM' should match Computer and Laptop")
        fun fuzzySearchRamShouldMatchComputerAndLaptop() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSearchQueryChanged("RAM")
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(2, state.filteredProducts.size)
        }

        @Test
        @DisplayName("Fuzzy search 'xyz' should match nothing")
        fun fuzzySearchXyzShouldMatchNothing() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSearchQueryChanged("xyz")
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(0, state.filteredProducts.size)
        }

        @Test
        @DisplayName("Search should be case insensitive")
        fun searchShouldBeCaseInsensitive() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSearchQueryChanged("COMPUTER")
            testScheduler.advanceUntilIdle()
            val upperCaseCount = viewModel.uiState.first().filteredProducts.size

            viewModel.onSearchQueryChanged("computer")
            testScheduler.advanceUntilIdle()
            val lowerCaseCount = viewModel.uiState.first().filteredProducts.size

            assertEquals(upperCaseCount, lowerCaseCount)
        }

        @Test
        @DisplayName("Clear search should show all products")
        fun clearSearchShouldShowAllProducts() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSearchQueryChanged("Computer")
            testScheduler.advanceUntilIdle()

            viewModel.clearSearch()
            testScheduler.advanceUntilIdle()

            val clearedState = viewModel.uiState.first()
            assertEquals("", clearedState.searchQuery)
            assertEquals(5, clearedState.filteredProducts.size)
        }

        @Test
        @DisplayName("Empty search query should show all products")
        fun emptySearchQueryShouldShowAllProducts() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            // First filter to a subset
            viewModel.onSearchQueryChanged("Computer")
            testScheduler.advanceUntilIdle()
            assertEquals(1, viewModel.uiState.first().filteredProducts.size)

            // Then clear search - should show all products
            viewModel.onSearchQueryChanged("")
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(5, state.filteredProducts.size)
            assertEquals("", state.searchQuery)
        }

        @Test
        @DisplayName("Whitespace-only search should show all products")
        fun whitespaceOnlySearchShouldShowAllProducts() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            // First filter to a subset
            viewModel.onSearchQueryChanged("Computer")
            testScheduler.advanceUntilIdle()

            // Whitespace-only query should show all products (treated as blank)
            viewModel.onSearchQueryChanged("   ")
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(5, state.filteredProducts.size)
        }
    }

    @Nested
    @DisplayName("Cart Operations Tests")
    inner class CartOperationsTests {

        @Test
        @DisplayName("Add to cart should add product")
        fun addToCartShouldAddProduct() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val product = TestProductRepository.createTestProducts()[0]
            viewModel.addToCart(product)
            testScheduler.advanceUntilIdle()

            val cartState = viewModel.uiState.first()
            assertEquals(1, cartState.cartItems.size)
            assertEquals(product.id, cartState.cartItems[0].product.id)
            assertEquals(1, cartState.cartItems[0].quantity)
        }

        @Test
        @DisplayName("Add same product twice should increase quantity")
        fun addSameProductTwiceShouldIncreaseQuantity() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val product = TestProductRepository.createTestProducts()[0]
            viewModel.addToCart(product)
            testScheduler.advanceUntilIdle()
            viewModel.addToCart(product)
            testScheduler.advanceUntilIdle()

            val cartState = viewModel.uiState.first()
            assertEquals(1, cartState.cartItems.size)
            assertEquals(2, cartState.cartItems[0].quantity)
        }

        @Test
        @DisplayName("Add different products should create separate cart items")
        fun addDifferentProductsShouldCreateSeparateCartItems() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val products = TestProductRepository.createTestProducts()
            viewModel.addToCart(products[0])
            testScheduler.advanceUntilIdle()
            viewModel.addToCart(products[1])
            testScheduler.advanceUntilIdle()

            val cartState = viewModel.uiState.first()
            assertEquals(2, cartState.cartItems.size)
        }

        @Test
        @DisplayName("Remove from cart should decrease quantity")
        fun removeFromCartShouldDecreaseQuantity() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val product = TestProductRepository.createTestProducts()[0]
            viewModel.addToCart(product)
            testScheduler.advanceUntilIdle()
            viewModel.addToCart(product)
            testScheduler.advanceUntilIdle()
            viewModel.removeFromCart(product)
            testScheduler.advanceUntilIdle()

            val cartState = viewModel.uiState.first()
            assertEquals(1, cartState.cartItems.size)
            assertEquals(1, cartState.cartItems[0].quantity)
        }

        @Test
        @DisplayName("Remove last item should remove from cart")
        fun removeLastItemShouldRemoveFromCart() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val product = TestProductRepository.createTestProducts()[0]
            viewModel.addToCart(product)
            testScheduler.advanceUntilIdle()
            viewModel.removeFromCart(product)
            testScheduler.advanceUntilIdle()

            val cartState = viewModel.uiState.first()
            assertTrue(cartState.cartItems.isEmpty())
        }

        @Test
        @DisplayName("Remove all from cart should remove entire item")
        fun removeAllFromCartShouldRemoveEntireItem() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val product = TestProductRepository.createTestProducts()[0]
            repeat(5) {
                viewModel.addToCart(product)
                testScheduler.advanceUntilIdle()
            }
            viewModel.removeAllFromCart(product)
            testScheduler.advanceUntilIdle()

            val cartState = viewModel.uiState.first()
            assertTrue(cartState.cartItems.isEmpty())
        }

        @Test
        @DisplayName("Cart total should calculate correctly")
        fun cartTotalShouldCalculateCorrectly() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val products = TestProductRepository.createTestProducts()
            // Add Computer ($499.99)
            viewModel.addToCart(products[0])
            testScheduler.advanceUntilIdle()
            // Add Computer again ($499.99 x 2 = $999.98)
            viewModel.addToCart(products[0])
            testScheduler.advanceUntilIdle()
            // Add Apples ($4.99)
            viewModel.addToCart(products[2])
            testScheduler.advanceUntilIdle()

            val cartState = viewModel.uiState.first()
            // Total: $999.98 + $4.99 = $1004.97
            assertEquals(1004.97, cartState.cartTotal, 0.01)
        }

        @Test
        @DisplayName("Cart item count should sum quantities")
        fun cartItemCountShouldSumQuantities() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val products = TestProductRepository.createTestProducts()
            viewModel.addToCart(products[0])
            testScheduler.advanceUntilIdle()
            viewModel.addToCart(products[0])
            testScheduler.advanceUntilIdle()
            viewModel.addToCart(products[1])
            testScheduler.advanceUntilIdle()

            val cartState = viewModel.uiState.first()
            assertEquals(3, cartState.cartItemCount)
        }
    }

    @Nested
    @DisplayName("Navigation Tests")
    inner class NavigationTests {

        @Test
        @DisplayName("Navigate to cart should change state to Cart")
        fun navigateToCartShouldChangeStateToCart() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.navigateToCart()

            val navState = viewModel.uiState.first()
            assertEquals(NavigationState.Cart, navState.navigationState)
        }

        @Test
        @DisplayName("Navigate to shopping should change state to Shopping")
        fun navigateToShoppingShouldChangeStateToShopping() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.navigateToCart()
            viewModel.navigateToShopping()

            val navState = viewModel.uiState.first()
            assertEquals(NavigationState.Shopping, navState.navigationState)
        }
    }

    @Nested
    @DisplayName("Payment Tests")
    inner class PaymentTests {

        @Test
        @DisplayName("Process payment should clear cart")
        fun processPaymentShouldClearCart() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val product = TestProductRepository.createTestProducts()[0]
            viewModel.addToCart(product)
            testScheduler.advanceUntilIdle()
            viewModel.navigateToCart()
            viewModel.processPayment()
            testScheduler.advanceUntilIdle()

            val paymentState = viewModel.uiState.first()
            assertTrue(paymentState.cartItems.isEmpty())
            assertEquals(0, paymentState.cartItemCount)
            assertEquals(0.0, paymentState.cartTotal)
        }

        @Test
        @DisplayName("Process payment should navigate to shopping")
        fun processPaymentShouldNavigateToShopping() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val product = TestProductRepository.createTestProducts()[0]
            viewModel.addToCart(product)
            testScheduler.advanceUntilIdle()
            viewModel.navigateToCart()
            viewModel.processPayment()
            testScheduler.advanceUntilIdle()

            val paymentState = viewModel.uiState.first()
            assertEquals(NavigationState.Shopping, paymentState.navigationState)
        }

        @Test
        @DisplayName("Process payment with empty cart should still work")
        fun processPaymentWithEmptyCartShouldStillWork() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.navigateToCart()
            viewModel.processPayment()
            testScheduler.advanceUntilIdle()

            val paymentState = viewModel.uiState.first()
            assertTrue(paymentState.cartItems.isEmpty())
            assertEquals(NavigationState.Shopping, paymentState.navigationState)
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    inner class EdgeCasesTests {

        @Test
        @DisplayName("Remove from cart with non-existent product should not crash")
        fun removeFromCartWithNonExistentProductShouldNotCrash() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val nonExistentProduct = Product(
                id = 999,
                title = "Non-existent",
                description = "Does not exist",
                price = 0.0,
                imageUrl = "",
                category = ProductCategory.FOOD
            )

            // Should not throw - no state change expected since product doesn't exist
            viewModel.removeFromCart(nonExistentProduct)
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(state.cartItems.isEmpty())
        }

        @Test
        @DisplayName("Multiple rapid state changes should be handled correctly")
        fun multipleRapidStateChangesShouldBeHandledCorrectly() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val products = TestProductRepository.createTestProducts()

            // Rapid operations
            viewModel.addToCart(products[0])
            testScheduler.advanceUntilIdle()
            viewModel.addToCart(products[1])
            testScheduler.advanceUntilIdle()
            viewModel.addToCart(products[2])
            testScheduler.advanceUntilIdle()
            viewModel.onSearchQueryChanged("test")
            viewModel.navigateToCart()

            // Verify final state
            val finalState = viewModel.uiState.first()
            assertEquals(3, finalState.cartItems.size)
            assertEquals("test", finalState.searchQuery)
            assertEquals(NavigationState.Cart, finalState.navigationState)
        }
    }

    @Nested
    @DisplayName("Sorting Tests")
    inner class SortingTests {

        @Test
        @DisplayName("Initial sort option should be ALPHABETICAL")
        fun initialSortOptionShouldBeAlphabetical() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(SortOption.ALPHABETICAL, state.sortOption)
        }

        @Test
        @DisplayName("Products should be sorted alphabetically by default")
        fun productsShouldBeSortedAlphabeticallyByDefault() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            val titles = state.filteredProducts.map { it.title }

            // Verify alphabetical order
            assertEquals(titles.sortedBy { it.lowercase() }, titles)
        }

        @Test
        @DisplayName("Sort by price low to high should order correctly")
        fun sortByPriceLowToHighShouldOrderCorrectly() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSortOptionChanged(SortOption.PRICE_LOW_TO_HIGH)
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(SortOption.PRICE_LOW_TO_HIGH, state.sortOption)

            val prices = state.filteredProducts.map { it.price }
            assertEquals(prices.sorted(), prices)
        }

        @Test
        @DisplayName("Sort by price high to low should order correctly")
        fun sortByPriceHighToLowShouldOrderCorrectly() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.onSortOptionChanged(SortOption.PRICE_HIGH_TO_LOW)
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(SortOption.PRICE_HIGH_TO_LOW, state.sortOption)

            val prices = state.filteredProducts.map { it.price }
            assertEquals(prices.sortedDescending(), prices)
        }

        @Test
        @DisplayName("Sort option should persist after search")
        fun sortOptionShouldPersistAfterSearch() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            // Set sort to price high to low
            viewModel.onSortOptionChanged(SortOption.PRICE_HIGH_TO_LOW)
            testScheduler.advanceUntilIdle()

            // Perform a search
            viewModel.onSearchQueryChanged("a")
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(SortOption.PRICE_HIGH_TO_LOW, state.sortOption)

            // Verify filtered products are still sorted by price descending
            val prices = state.filteredProducts.map { it.price }
            assertEquals(prices.sortedDescending(), prices)
        }

        @Test
        @DisplayName("Changing sort option should re-sort filtered products")
        fun changingSortOptionShouldReSortFilteredProducts() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            // First filter
            viewModel.onSearchQueryChanged("a")
            testScheduler.advanceUntilIdle()

            val filteredCount = viewModel.uiState.first().filteredProducts.size

            // Then change sort
            viewModel.onSortOptionChanged(SortOption.PRICE_LOW_TO_HIGH)
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()

            // Count should remain the same
            assertEquals(filteredCount, state.filteredProducts.size)

            // But order should change
            val prices = state.filteredProducts.map { it.price }
            assertEquals(prices.sorted(), prices)
        }

        @Test
        @DisplayName("Switching between all sort options should work correctly")
        fun switchingBetweenAllSortOptionsShouldWork() = runTest {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            // Test each sort option
            SortOption.entries.forEach { sortOption ->
                viewModel.onSortOptionChanged(sortOption)
                testScheduler.advanceUntilIdle()

                val state = viewModel.uiState.first()
                assertEquals(sortOption, state.sortOption)

                // Verify correct ordering
                when (sortOption) {
                    SortOption.ALPHABETICAL -> {
                        val titles = state.filteredProducts.map { it.title }
                        assertEquals(titles.sortedBy { it.lowercase() }, titles)
                    }
                    SortOption.PRICE_LOW_TO_HIGH -> {
                        val prices = state.filteredProducts.map { it.price }
                        assertEquals(prices.sorted(), prices)
                    }
                    SortOption.PRICE_HIGH_TO_LOW -> {
                        val prices = state.filteredProducts.map { it.price }
                        assertEquals(prices.sortedDescending(), prices)
                    }
                }
            }
        }
    }
}

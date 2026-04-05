package com.example.testapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapplication.model.CartItem
import com.example.testapplication.model.NavigationState
import com.example.testapplication.model.Product
import com.example.testapplication.model.ShopUiState
import com.example.testapplication.model.SortOption
import com.example.testapplication.viewmodel.ShopViewModel


@Composable
fun ShopApp(
    viewModel: ShopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState(initial = ShopUiState())
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    when (uiState.navigationState) {
        is NavigationState.Shopping -> {
            ShoppingScreen(
                uiState = uiState,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onClearSearch = viewModel::clearSearch,
                onSortOptionChanged = viewModel::onSortOptionChanged,
                onAddToCart = viewModel::addToCart,
                onNavigateToCart = viewModel::navigateToCart
            )
        }

        is NavigationState.Cart -> {
            CartScreen(
                uiState = uiState,
                onRemoveFromCart = viewModel::removeFromCart,
                onRemoveAllFromCart = viewModel::removeAllFromCart,
                onAddToCart = viewModel::addToCart,
                onNavigateBack = viewModel::navigateToShopping,
                onPayment = viewModel::processPayment
            )
        }
    }
}

/**
 * The main shopping screen displaying a search bar, sort selector, and product list.
 *
 * @param uiState Current UI state containing products, search query, and sort option
 * @param onSearchQueryChanged Callback when search text changes
 * @param onClearSearch Callback to clear the search query
 * @param onSortOptionChanged Callback when sort option changes
 * @param onAddToCart Callback when user adds a product to cart
 * @param onNavigateToCart Callback to navigate to the cart screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    uiState: ShopUiState,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSortOptionChanged: (SortOption) -> Unit,
    onAddToCart: (Product) -> Unit,
    onNavigateToCart: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fake Store") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    CartIconWithBadge(
                        itemCount = uiState.cartItemCount,
                        onClick = onNavigateToCart
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged,
                onClearQuery = onClearSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                SortSelector(
                    currentSortOption = uiState.sortOption,
                    onSortOptionChanged = onSortOptionChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                if (uiState.isLoading) {
                    LoadingIndicator()
                } else {
                    ProductList(
                        products = uiState.filteredProducts,
                        onAddToCart = onAddToCart
                    )
                }
            }
        }
    }
}

/**
 * The cart screen displaying all items added to the cart.
 *
 * @param uiState Current UI state containing cart items
 * @param onRemoveFromCart Callback to decrease quantity of an item
 * @param onRemoveAllFromCart Callback to remove an item completely
 * @param onAddToCart Callback to increase quantity of an item
 * @param onNavigateBack Callback to return to shopping screen
 * @param onPayment Callback to process payment
 * this is a new comment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    uiState: ShopUiState,
    onRemoveFromCart: (Product) -> Unit,
    onRemoveAllFromCart: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onNavigateBack: () -> Unit,
    onPayment: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to shopping"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.cartItems.isEmpty()) {
                EmptyCartMessage(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = uiState.cartItems,
                        key = { index, cartItem -> "${cartItem.product.id}-${cartItem.quantity}-$index" }
                    ) { _, cartItem ->
                        CartItemRow(
                            cartItem = cartItem,
                            onIncreaseQuantity = { onAddToCart(cartItem.product) },
                            onDecreaseQuantity = { onRemoveFromCart(cartItem.product) },
                            onRemoveAll = { onRemoveAllFromCart(cartItem.product) }
                        )
                    }
                }

                CartSummary(
                    total = uiState.cartTotal,
                    itemCount = uiState.cartItemCount,
                    onPayment = onPayment,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Search bar component for filtering products.
 *
 * @param query Current search query text
 * @param onQueryChanged Callback when search text changes
 * @param onClearQuery Callback to clear the search
 * @param modifier Modifier for the search bar
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = { Text("Search products...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Sort selector component displaying a dropdown menu with sorting options.
 *
 * @param currentSortOption The currently selected sort option
 * @param onSortOptionChanged Callback when a new sort option is selected
 * @param modifier Modifier for the sort selector
 */
@Composable
fun SortSelector(
    currentSortOption: SortOption,
    onSortOptionChanged: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
            .padding(bottom = 16.dp, top = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort by:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box {
            FilterChip(
                selected = true,
                onClick = { expanded = true },
                label = { Text(currentSortOption.displayName) }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SortOption.entries.forEach { sortOption ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = sortOption.displayName,
                                fontWeight = if (sortOption == currentSortOption) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        },
                        onClick = {
                            onSortOptionChanged(sortOption)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Displays the list of products in a scrollable lazy column.
 *
 * @param products List of products to display
 * @param onAddToCart Callback when user adds a product to cart
 */
@Composable
fun ProductList(
    products: List<Product>,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            NoResultsMessage()
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = products,
                key = { index, product -> "${product.id}-${product.title}-$index" }
            ) { _, product ->
                ProductItem(
                    product = product,
                    onAddToCart = { onAddToCart(product) }
                )
            }
        }
    }
}

/**
 * Individual product card displaying product information.
 *
 * @param product The product to display
 * @param onAddToCart Callback when user adds this product to cart
 */
@Composable
fun ProductItem(
    product: Product,
    onAddToCart: () -> Unit
) {
    Card(
        onClick = onAddToCart,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product emoji
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.imageUrl,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.title,
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier.clearAndSetSemantics { }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to cart",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual row in the cart displaying a cart item with quantity controls.
 *
 * @param cartItem The cart item to display
 * @param onIncreaseQuantity Callback to increase quantity
 * @param onDecreaseQuantity Callback to decrease quantity
 * @param onRemoveAll Callback to remove all units of this item
 */
@Composable
fun CartItemRow(
    cartItem: CartItem,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onRemoveAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product emoji
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cartItem.product.imageUrl,
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product info and quantity controls
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cartItem.product.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$${String.format("%.2f", cartItem.product.price)} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecreaseQuantity,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }

                    Text(
                        text = "${cartItem.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = onIncreaseQuantity,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity"
                        )
                    }
                }
            }

            // Subtotal and delete button
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%.2f", cartItem.product.price * cartItem.quantity)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onRemoveAll) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from cart",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Cart summary section showing total and pay button.
 *
 * @param total Total price of all cart items
 * @param itemCount Total number of items in cart
 * @param onPayment Callback when pay button is clicked
 * @param modifier Modifier for the summary section
 */
@Composable
fun CartSummary(
    total: Double,
    itemCount: Int,
    onPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total ($itemCount items)",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pay Now",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Cart icon button with badge showing the number of items.
 *
 * @param itemCount Number of items in the cart
 * @param onClick Callback when the cart icon is clicked
 */
@Composable
fun CartIconWithBadge(
    itemCount: Int,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (itemCount > 0) {
                    Badge {
                        Text(
                            text = if (itemCount > 99) "99+" else itemCount.toString()
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Shopping cart"
            )
        }
    }
}

/**
 * Loading indicator displayed while products are being fetched.
 */
@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading products...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Message displayed when no search results are found.
 */
@Composable
fun NoResultsMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No products found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Try a different search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Message displayed when the cart is empty.
 *
 * @param modifier Modifier for the empty cart message
 */
@Composable
fun EmptyCartMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your cart is empty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Add some items to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

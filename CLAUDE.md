# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build the project
./gradlew build

# Run unit tests (JUnit5)
./gradlew testDebugUnitTest

# Run a specific test class
./gradlew testDebugUnitTest --tests "com.example.testapplication.viewmodel.ShopViewModelTest"

# Run a specific test method
./gradlew testDebugUnitTest --tests "com.example.testapplication.viewmodel.ShopViewModelTest.SortingTests.sortByPriceLowToHighShouldOrderCorrectly"

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean build
```

## Architecture

This is a fake online store Android application using **MVVM architecture** with Jetpack Compose.

### Layer Structure

- **Model** (`model/Models.kt`): All data classes in a single file - `Product`, `CartItem`, `ShopUiState`, `NavigationState` (sealed class), `SortOption` (enum)
- **View** (`ui/ShopScreen.kt`): All Compose UI in a single file - shopping screen, cart screen, product items, search bar, sort selector
- **ViewModel** (`viewmodel/ShopViewModel.kt`): Single ViewModel managing all state including products, cart, search, sort, and navigation
- **Repository** (`repository/ProductRepository.kt`): Generates 1000 products via Flow (simulated network)

### Key Patterns

- **Navigation**: Managed by ViewModel via `NavigationState` sealed class (SHOPPING/CART) - no navigation library
- **Data Loading**: Triggered by `LaunchedEffect(Unit)` in `ShopApp` composable calling `viewModel.loadProducts()`
- **Search**: Fuzzy character-sequence matching in product titles
- **State**: Single `ShopUiState` data class with derived properties (`cartTotal`, `cartItemCount`)

### Testing

- JUnit5 with `@Nested` classes for test organization
- Turbine for Flow testing
- `kotlinx-coroutines-test` with `StandardTestDispatcher`
- Test helper `createViewModel(loadProducts: Boolean)` to control initialization

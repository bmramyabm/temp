# TestApplication - Fake Store App

A simple Android shopping application built with Jetpack Compose following MVVM architecture.

## Overview

This app displays a list of products that users can browse, search, sort, add to cart, and checkout.

## Project Structure

```
app/src/main/java/com/example/testapplication/
- MainActivity.kt              # Entry point, sets up Compose
- model/
---- Models.kt                # Data classes: Product, CartItem, ShopUiState, NavigationState, SortOption
- repository/
---- ProductRepository.kt     # Generates product data via Flow
- ui/
---- ShopScreen.kt            # All Compose UI components (Shopping, Cart, Product items)
- viewmodel/
---- ShopViewModel.kt         # State management for products, cart, search, sort, navigation

app/src/test/java/
- viewmodel/
---- ShopViewModelTest.kt     # Unit tests using JUnit5 and Coroutines Test
```

## Success Criteria

All of the following must be working:

- [ ] App compiles and runs without crashes
- [ ] Product list displays correctly
- [ ] Search functionality filters products
- [ ] Sort functionality orders products (A-Z, Price Low-High, Price High-Low)
- [ ] Add to cart works correctly
- [ ] Cart displays items with correct quantities and totals
- [ ] Checkout clears cart and returns to shopping
- [ ] All unit tests pass (`./gradlew testDebugUnitTest`)

## What is Allowed

- Asking clarifying questions
- Asking about APIs you don't remember
- Using logs for debugging
- Using the debugger
- Using Layout Inspector
- Running existing tests
- Writing additional tests

## What is NOT Allowed

- Google search
- Using any LLM or AI assistant
## Notes
The repository must not be changed, assume is working as expected.

package com.example.testapplication.repository

import com.example.testapplication.model.CartItem
import com.example.testapplication.model.Product
import kotlinx.coroutines.delay

/**
 *  ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️
 *  DO NOT MODIFY
 *   ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️
 * Repository responsible for managing cart operations.
 * All operations include artificial delays between 100-500ms to simulate network latency.
 */
open class CartRepository {

    private val cartItems = mutableListOf<CartItem>()

    /**
     * Adds a random delay between 100-500ms to simulate network latency.
     */
    private suspend fun simulateNetworkDelay() {
        delay((900..1200).random().toLong())
    }

    /**
     * Gets all items currently in the cart.
     * @return List of cart items
     */
    open suspend fun getCartItems(): List<CartItem> {
        simulateNetworkDelay()
        return cartItems.toList()
    }

    /**
     * Adds a product to the cart. If the product already exists, increments its quantity.
     * @param product The product to add
     * @return Updated list of cart items
     */
    open suspend fun addToCart(product: Product): List<CartItem> {
        simulateNetworkDelay()
        val existingItem = cartItems.find { it.product.id == product.id }
        simulateNetworkDelay()
        if (existingItem != null) {
            val index = cartItems.indexOf(existingItem)
            cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            cartItems.add(CartItem(product = product, quantity = 1))
        }
        return cartItems.toList()
    }

    /**
     * Removes one unit of a product from the cart.
     * If quantity becomes zero, the item is removed entirely.
     * @param product The product to remove
     * @return Updated list of cart items
     */
    open suspend fun removeFromCart(product: Product): List<CartItem> {
        simulateNetworkDelay()
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

    /**
     * Removes all units of a specific product from the cart.
     * @param product The product to completely remove
     * @return Updated list of cart items
     */
    open suspend fun removeAllFromCart(product: Product): List<CartItem> {
        simulateNetworkDelay()
        // Iterating with for-each while another coroutine modifies = ConcurrentModificationException
        for (item in cartItems) {
            if (item.product.id == product.id) {
                simulateNetworkDelay() // Window for another coroutine to modify
                cartItems.remove(item)
            }
        }
        return cartItems.toList()
    }

    /**
     * Clears the entire cart.
     * @return Empty list
     */
    open suspend fun clearCart(): List<CartItem> {
        simulateNetworkDelay()
        cartItems.clear()
        return cartItems.toList()
    }
}

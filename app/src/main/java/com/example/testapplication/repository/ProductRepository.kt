package com.example.testapplication.repository

import com.example.testapplication.model.Product
import com.example.testapplication.model.ProductCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

/**
 *
 *  *  ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️
 *  *  DO NOT MODIFY
 *  *   ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️ ⚠️️️️️
 * ProductRepository.kt
 *
 * This repository simulates a network data source by generating 1000 products
 * across three categories: Food, House items, and Technology.
 * It emits data through a Flow to simulate asynchronous network behavior.
 */

/**
 * Repository that provides product data simulating a network response.
 * Generates 1000 products distributed across Food, House, and Technology categories.
 * Marked as open to allow test implementations to override behavior.
 */
open class ProductRepository {

    /**
     * Fetches all products from the "network" (simulated data).
     * Includes a simulated network delay of 500ms.
     * Marked as open for testing purposes.
     *
     * @return A Flow emitting the list of 1000 products
     */
    open fun getProducts(): Flow<List<Product>> = flow {
        // Simulate network delay
        delay(500)
        emit(generateProducts())
    }

    /**
     * Generates 5000 products distributed across categories.
     * - Products 0-1666: Food items
     * - Products 1667-3333: House items
     * - Products 3334-4999: Technology items
     */
    private fun generateProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val random = Random(42) // Fixed seed for consistent data

        // Generate Food items (1667 items)
        foodItems.forEachIndexed { index, (name, description) ->
            repeat(getRepeatCount(index, foodItems.size, 1667)) { repeatIndex ->
                val productIndex = products.size
                products.add(
                    Product(
                        id = productIndex,
                        title = "$name ${productIndex + 1}",
                        description = description,
                        price = generatePrice(random, ProductCategory.FOOD),
                        imageUrl = generateImageUrl(productIndex, ProductCategory.FOOD),
                        category = ProductCategory.FOOD
                    )
                )
            }
        }

        // Generate House items (1667 items)
        houseItems.forEachIndexed { index, (name, description) ->
            repeat(getRepeatCount(index, houseItems.size, 1667)) { repeatIndex ->
                val productIndex = products.size
                products.add(
                    Product(
                        id = productIndex,
                        title = "$name ${productIndex + 1}",
                        description = description,
                        price = generatePrice(random, ProductCategory.HOUSE),
                        imageUrl = generateImageUrl(productIndex, ProductCategory.HOUSE),
                        category = ProductCategory.HOUSE
                    )
                )
            }
        }

        // Generate Technology items (1666 items)
        techItems.forEachIndexed { index, (name, description) ->
            repeat(getRepeatCount(index, techItems.size, 1666)) { repeatIndex ->
                val productIndex = products.size
                products.add(
                    Product(
                        id = productIndex,
                        title = "$name ${productIndex + 1}",
                        description = description,
                        price = generatePrice(random, ProductCategory.TECHNOLOGY),
                        imageUrl = generateImageUrl(productIndex, ProductCategory.TECHNOLOGY),
                        category = ProductCategory.TECHNOLOGY
                    )
                )
            }
        }

        return products.take(5000)
    }

    /**
     * Calculates how many times to repeat each item type to fill the target count.
     */
    private fun getRepeatCount(index: Int, totalTypes: Int, targetCount: Int): Int {
        val baseCount = targetCount / totalTypes
        val remainder = targetCount % totalTypes
        return if (index < remainder) baseCount + 1 else baseCount
    }

    /**
     * Generates a realistic price based on product category.
     */
    private fun generatePrice(random: Random, category: ProductCategory): Double {
        val price = when (category) {
            ProductCategory.FOOD -> random.nextDouble(0.99, 29.99)
            ProductCategory.HOUSE -> random.nextDouble(4.99, 199.99)
            ProductCategory.TECHNOLOGY -> random.nextDouble(9.99, 999.99)
        }
        return (price * 100).toInt() / 100.0 // Round to 2 decimal places
    }

    /**
     * Returns an emoji representing the product category.
     */
    private fun generateImageUrl(
        @Suppress("UNUSED_PARAMETER") productId: Int,
        category: ProductCategory
    ): String {
        return when (category) {
            ProductCategory.FOOD -> "🍎"
            ProductCategory.HOUSE -> "🏠"
            ProductCategory.TECHNOLOGY -> "💻"
        }
    }

    companion object {
        /**
         * Food item templates with generic names and descriptions.
         */
        private val foodItems = listOf(
            "Organic Apples" to "Fresh organic apples, perfect for snacking or baking",
            "Whole Grain Bread" to "Nutritious whole grain bread, freshly baked daily",
            "Fresh Milk" to "Farm-fresh whole milk, pasteurized and homogenized",
            "Free Range Eggs" to "Free range chicken eggs, pack of 12",
            "Ground Coffee" to "Premium ground coffee beans, medium roast",
            "Green Tea" to "Natural green tea leaves, antioxidant rich",
            "Pasta" to "Traditional Italian-style pasta, made from durum wheat",
            "Rice" to "Long grain white rice, perfect for any meal",
            "Olive Oil" to "Extra virgin olive oil, cold pressed",
            "Honey" to "Pure natural honey, locally sourced",
            "Cheese" to "Aged cheddar cheese, sharp and flavorful",
            "Yogurt" to "Creamy Greek yogurt, high in protein",
            "Orange Juice" to "Fresh squeezed orange juice, no pulp",
            "Chicken Breast" to "Boneless skinless chicken breast, lean protein",
            "Salmon Fillet" to "Wild caught salmon fillet, omega-3 rich",
            "Broccoli" to "Fresh broccoli crowns, vitamin packed",
            "Carrots" to "Organic carrots, sweet and crunchy",
            "Tomatoes" to "Vine ripened tomatoes, locally grown",
            "Potatoes" to "Russet potatoes, ideal for baking or mashing",
            "Bananas" to "Ripe yellow bananas, great source of potassium"
        )

        /**
         * House item templates with generic names and descriptions.
         */
        private val houseItems = listOf(
            "Bath Towel" to "Soft cotton bath towel, highly absorbent",
            "Bed Sheets" to "Premium cotton bed sheets, breathable and comfortable",
            "Throw Pillow" to "Decorative throw pillow, adds style to any room",
            "Table Lamp" to "Modern table lamp, adjustable brightness",
            "Wall Clock" to "Minimalist wall clock, silent movement",
            "Picture Frame" to "Wooden picture frame, fits standard photos",
            "Candle Set" to "Scented candle set, long burning time",
            "Storage Box" to "Stackable storage box, durable plastic",
            "Door Mat" to "Heavy duty door mat, weather resistant",
            "Curtains" to "Blackout curtains, energy efficient",
            "Bathroom Rug" to "Non-slip bathroom rug, machine washable",
            "Kitchen Towel" to "Absorbent kitchen towel, pack of 3",
            "Trash Can" to "Stainless steel trash can, step pedal",
            "Laundry Basket" to "Collapsible laundry basket, large capacity",
            "Clothes Hanger" to "Wooden clothes hanger, set of 10",
            "Shoe Rack" to "Multi-tier shoe rack, space saving design",
            "Plant Pot" to "Ceramic plant pot, drainage hole included",
            "Vase" to "Glass flower vase, elegant design",
            "Mirror" to "Wall mounted mirror, frameless edge",
            "Blanket" to "Fleece throw blanket, soft and warm"
        )

        /**
         * Technology item templates with generic names and descriptions.
         */
        private val techItems = listOf(
            "Computer [8GB RAM, 256GB SSD]" to "Desktop computer with solid state drive and ample memory",
            "Computer [16GB RAM, 512GB SSD]" to "High performance desktop with fast storage",
            "Computer [32GB RAM, 1TB SSD]" to "Professional workstation grade desktop computer",
            "Laptop [8GB RAM, 256GB SSD]" to "Portable laptop for everyday computing tasks",
            "Laptop [16GB RAM, 512GB SSD]" to "Business laptop with enhanced performance",
            "Laptop [32GB RAM, 1TB SSD]" to "Premium laptop for demanding applications",
            "Tablet [64GB]" to "Compact tablet for browsing and media consumption",
            "Tablet [128GB]" to "Mid-range tablet with expandable storage",
            "Tablet [256GB]" to "High capacity tablet for professionals",
            "Smartphone [64GB]" to "Modern smartphone with quality camera",
            "Smartphone [128GB]" to "Feature-rich smartphone with extended storage",
            "Smartphone [256GB]" to "Premium smartphone with top specifications",
            "Wireless Earbuds" to "True wireless earbuds with noise cancellation",
            "Bluetooth Speaker" to "Portable bluetooth speaker, water resistant",
            "USB Cable" to "High speed USB charging cable, 6 feet",
            "Power Bank" to "Portable power bank, 10000mAh capacity",
            "Wireless Mouse" to "Ergonomic wireless mouse, long battery life",
            "Mechanical Keyboard" to "Mechanical gaming keyboard, RGB backlit",
            "Monitor [24 inch]" to "Full HD monitor, wide viewing angles",
            "Monitor [27 inch]" to "4K resolution monitor, color accurate display"
        )
    }
}
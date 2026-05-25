package dev.ml.portablepos.presentation.pos

import dev.ml.portablepos.domain.model.CartItem
import dev.ml.portablepos.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartManager @Inject constructor() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    fun addItem(product: Product) {
        _items.update { current ->
            val existing = current.indexOfFirst { it.product.id == product.id }
            if (existing >= 0) {
                current.toMutableList().apply {
                    set(existing, current[existing].copy(quantity = current[existing].quantity + 1))
                }
            } else {
                current + CartItem(product = product, quantity = 1)
            }
        }
    }

    fun updateQuantity(productId: Long, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }
        _items.update { current ->
            current.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
        }
    }

    fun removeItem(productId: Long) {
        _items.update { current -> current.filter { it.product.id != productId } }
    }

    fun setDiscount(discount: Double) {
        _discount.value = discount
    }

    val subtotal: Double get() = _items.value.sumOf { it.subtotal }
    val itemCount: Int get() = _items.value.sumOf { it.quantity }

    fun clear() {
        _items.value = emptyList()
        _discount.value = 0.0
    }
}

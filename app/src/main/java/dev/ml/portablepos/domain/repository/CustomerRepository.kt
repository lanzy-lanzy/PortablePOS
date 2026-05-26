package dev.ml.portablepos.domain.repository

import dev.ml.portablepos.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(id: Long): Customer?
    suspend fun getCustomerByPhone(phone: String): Customer?
    fun searchCustomers(query: String): Flow<List<Customer>>
    suspend fun addCustomer(customer: Customer): Long
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)
    suspend fun recordPurchase(customerId: Long, amount: Double)
}
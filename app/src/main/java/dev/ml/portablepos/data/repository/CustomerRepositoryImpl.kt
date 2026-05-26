package dev.ml.portablepos.data.repository

import dev.ml.portablepos.data.local.dao.CustomerDao
import dev.ml.portablepos.data.mapper.toDomainModel
import dev.ml.portablepos.data.mapper.toEntity
import dev.ml.portablepos.domain.model.Customer
import dev.ml.portablepos.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun getAllCustomers(): Flow<List<Customer>> =
        customerDao.getAllCustomers().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getCustomerById(id: Long): Customer? =
        customerDao.getCustomerById(id)?.toDomainModel()

    override suspend fun getCustomerByPhone(phone: String): Customer? =
        customerDao.getCustomerByPhone(phone)?.toDomainModel()

    override fun searchCustomers(query: String): Flow<List<Customer>> =
        customerDao.searchCustomers(query).map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun addCustomer(customer: Customer): Long =
        customerDao.insertCustomer(customer.toEntity())

    override suspend fun updateCustomer(customer: Customer) =
        customerDao.updateCustomer(customer.toEntity())

    override suspend fun deleteCustomer(customer: Customer) =
        customerDao.deleteCustomer(customer.toEntity())

    override suspend fun recordPurchase(customerId: Long, amount: Double) {
        val customer = customerDao.getCustomerById(customerId) ?: return
        customerDao.recordPurchase(customerId, amount)
    }
}
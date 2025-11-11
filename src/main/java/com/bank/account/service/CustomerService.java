package com.bank.account.service;

import com.bank.account.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Optional<Customer> getCustomerById(Long id);

    List<Customer> getAllCustomers();

    Customer updateCustomer(Long id, Customer customerDetails);

    void deleteCustomer(Long id);

    Customer updateKyc(Long id, boolean kycCompleted);
}

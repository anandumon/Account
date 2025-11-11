package com.bank.account.service;

import com.bank.account.entity.Customer;
import com.bank.account.exception.ResourceNotFoundException;
import com.bank.account.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id).filter(Customer::isActive);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findByActive(true);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        customer.setName(customerDetails.getName());
        customer.setDob(customerDetails.getDob());
        customer.setAddress(customerDetails.getAddress());
        customer.setMobileNumber(customerDetails.getMobileNumber());

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Override
    public Customer updateKyc(Long id, boolean kycCompleted) {
        Customer customer = getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        customer.setKycCompleted(kycCompleted);
        return customerRepository.save(customer);
    }
}

package com.bank.account.service;

import com.bank.account.dto.CardStatus;
import com.bank.account.dto.CardType;
import com.bank.account.entity.Account;
import com.bank.account.entity.Card;
import com.bank.account.entity.Customer;
import com.bank.account.entity.Transaction;
import com.bank.account.exception.InvalidInputException;
import com.bank.account.exception.InvalidPinException;
import com.bank.account.exception.ResourceNotFoundException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.CardRepository;
import com.bank.account.repository.CustomerRepository;
import com.bank.account.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final Random random = new Random();

    @Override
    @Transactional
    public Card issueCard(Long customerId, Long accountId, CardType cardType, double limit) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        Card card = new Card();
        card.setCardNumber(generateUniqueCardNumber());
        card.setCardType(cardType);
        card.setStatus(CardStatus.ACTIVE);
        card.setPin(generateRandomPin()); // PIN is generated and stored (in a real app, this would be hashed)
        card.setIssueDate(LocalDate.now());
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setCvv(generateRandomCvv());
        card.setCustomer(customer);
        card.setAccount(account);

        if (cardType == CardType.CREDIT) {
            card.setCreditLimit(limit);
            card.setCurrentCreditUsed(0.0);
        } else {
            card.setDailyWithdrawalLimit(limit);
        }

        return cardRepository.save(card);
    }

    @Override
    public Card getCardDetails(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with number: " + cardNumber));
    }

    @Override
    @Transactional
    public Card blockCard(String cardNumber) {
        Card card = getCardDetails(cardNumber);
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card unblockCard(String cardNumber) {
        Card card = getCardDetails(cardNumber);
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card updateCreditLimit(String cardNumber, double newLimit) {
        Card card = getCardDetails(cardNumber);
        if (card.getCardType() != CardType.CREDIT) {
            throw new InvalidInputException("Cannot update credit limit for a non-credit card: " + cardNumber);
        }
        card.setCreditLimit(newLimit);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card updateDailyWithdrawalLimit(String cardNumber, double newLimit) {
        Card card = getCardDetails(cardNumber);
        if (card.getCardType() != CardType.DEBIT) {
            throw new InvalidInputException("Cannot update daily withdrawal limit for a non-debit card: " + cardNumber);
        }
        card.setDailyWithdrawalLimit(newLimit);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public String generatePin(String cardNumber) {
        Card card = getCardDetails(cardNumber);
        String newPin = generateRandomPin();
        card.setPin(newPin); // In a real app, hash this PIN
        cardRepository.save(card);
        return newPin;
    }

    @Override
    @Transactional
    public String updatePin(String cardNumber, String oldPin, String newPin) {
        Card card = getCardDetails(cardNumber);
        if (!card.getPin().equals(oldPin)) { // In a real app, compare hashed PINs
            throw new InvalidPinException("Invalid old PIN for card: " + cardNumber);
        }
        card.setPin(newPin); // In a real app, hash this PIN
        cardRepository.save(card);
        return "PIN updated successfully";
    }

    @Override
    public boolean validatePin(String cardNumber, String pin) {
        Card card = getCardDetails(cardNumber);
        // In a real app, compare hashed PINs
        return card.getPin().equals(pin);
    }

    @Override
    public boolean isCardExpired(String cardNumber) {
        Card card = getCardDetails(cardNumber);
        return LocalDate.now().isAfter(card.getExpiryDate());
    }

    @Override
    public List<Transaction> getCardTransactionHistory(String cardNumber) {
        Card card = getCardDetails(cardNumber);
        // Assuming transactions are linked to accounts, and cards are linked to accounts
        // This might need refinement based on how card transactions are specifically recorded
        return transactionRepository.findByAccount_AccountNumber(card.getAccount().getAccountNumber())
                .stream()
                .filter(t -> t.getDescription() != null && t.getDescription().contains(cardNumber)) // Simple filter, needs robust implementation
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> getMonthlyCardTransactionHistory(String cardNumber, int year, int month) {
        Card card = getCardDetails(cardNumber);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        return transactionRepository.findByAccount_AccountNumber(card.getAccount().getAccountNumber())
                .stream()
                .filter(t -> t.getDate().toLocalDate().isAfter(startOfMonth.minusDays(1)) &&
                             t.getDate().toLocalDate().isBefore(endOfMonth.plusDays(1)))
                .filter(t -> t.getDescription() != null && t.getDescription().contains(cardNumber))
                .collect(Collectors.toList());
    }

    private String generateUniqueCardNumber() {
        String cardNumber;
        do {
            cardNumber = String.format("%04d%04d%04d%04d",
                    random.nextInt(10000),
                    random.nextInt(10000),
                    random.nextInt(10000),
                    random.nextInt(10000));
        } while (cardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    private String generateRandomPin() {
        return String.format("%04d", random.nextInt(10000));
    }

    private String generateRandomCvv() {
        return String.format("%03d", random.nextInt(1000));
    }
}

package com.bank.account.service;

import com.bank.account.dto.CardStatus;
import com.bank.account.dto.CardType;
import com.bank.account.entity.Card;
import com.bank.account.entity.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface CardService {

    Card issueCard(Long customerId, Long accountId, CardType cardType, double limit);
    Card getCardDetails(String cardNumber);
    Card blockCard(String cardNumber);
    Card unblockCard(String cardNumber);
    Card updateCreditLimit(String cardNumber, double newLimit);
    Card updateDailyWithdrawalLimit(String cardNumber, double newLimit);
    String generatePin(String cardNumber); // Returns the generated PIN
    String updatePin(String cardNumber, String oldPin, String newPin);
    boolean validatePin(String cardNumber, String pin);
    boolean isCardExpired(String cardNumber);
    List<Transaction> getCardTransactionHistory(String cardNumber);
    List<Transaction> getMonthlyCardTransactionHistory(String cardNumber, int year, int month);
}

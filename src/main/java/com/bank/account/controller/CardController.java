package com.bank.account.controller;

import com.bank.account.dto.IssueCardRequest;
import com.bank.account.dto.UpdateCardLimitRequest;
import com.bank.account.dto.UpdatePinRequest;
import com.bank.account.entity.Card;
import com.bank.account.entity.Transaction;
import com.bank.account.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @PostMapping
    public Card issueCard(@RequestBody IssueCardRequest request) {
        return cardService.issueCard(request.getCustomerId(), request.getAccountId(), request.getCardType(), request.getLimit());
    }

    @GetMapping("/{cardNumber}")
    public Card getCardDetails(@PathVariable String cardNumber) {
        return cardService.getCardDetails(cardNumber);
    }

    @PatchMapping("/{cardNumber}/block")
    public Card blockCard(@PathVariable String cardNumber) {
        return cardService.blockCard(cardNumber);
    }

    @PatchMapping("/{cardNumber}/unblock")
    public Card unblockCard(@PathVariable String cardNumber) {
        return cardService.unblockCard(cardNumber);
    }

    @PatchMapping("/{cardNumber}/limit/credit")
    public Card updateCreditLimit(@PathVariable String cardNumber, @RequestBody UpdateCardLimitRequest request) {
        return cardService.updateCreditLimit(cardNumber, request.getNewLimit());
    }

    @PatchMapping("/{cardNumber}/limit/withdrawal")
    public Card updateDailyWithdrawalLimit(@PathVariable String cardNumber, @RequestBody UpdateCardLimitRequest request) {
        return cardService.updateDailyWithdrawalLimit(cardNumber, request.getNewLimit());
    }

    @PostMapping("/{cardNumber}/pin/generate")
    public ResponseEntity<String> generatePin(@PathVariable String cardNumber) {
        String newPin = cardService.generatePin(cardNumber);
        return ResponseEntity.ok("New PIN generated: " + newPin + ". Please change it immediately.");
    }

    @PatchMapping("/{cardNumber}/pin/update")
    public ResponseEntity<String> updatePin(@PathVariable String cardNumber, @RequestBody UpdatePinRequest request) {
        String message = cardService.updatePin(cardNumber, request.getOldPin(), request.getNewPin());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{cardNumber}/expired")
    public ResponseEntity<Boolean> isCardExpired(@PathVariable String cardNumber) {
        return ResponseEntity.ok(cardService.isCardExpired(cardNumber));
    }

    @GetMapping("/{cardNumber}/transactions")
    public List<Transaction> getCardTransactionHistory(@PathVariable String cardNumber) {
        return cardService.getCardTransactionHistory(cardNumber);
    }

    @GetMapping("/{cardNumber}/transactions/monthly")
    public List<Transaction> getMonthlyCardTransactionHistory(
            @PathVariable String cardNumber,
            @RequestParam int year,
            @RequestParam int month) {
        return cardService.getMonthlyCardTransactionHistory(cardNumber, year, month);
    }
}

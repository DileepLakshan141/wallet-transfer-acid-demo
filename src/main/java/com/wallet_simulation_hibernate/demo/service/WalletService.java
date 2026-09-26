package com.wallet_simulation_hibernate.demo.service;

import com.wallet_simulation_hibernate.demo.dto.CreateWalletRequest;
import com.wallet_simulation_hibernate.demo.entity.Wallet;
import com.wallet_simulation_hibernate.demo.entity.WalletTransaction;
import com.wallet_simulation_hibernate.demo.enums.TransactionStatus;
import com.wallet_simulation_hibernate.demo.enums.TransactionType;
import com.wallet_simulation_hibernate.demo.repositories.WalletRepository;
import com.wallet_simulation_hibernate.demo.repositories.WalletTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final int MAX_RETRIES = 3;

    public WalletService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public Wallet updateOwnerNameBroken(UUID id, String name){
        Wallet target = walletRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Wallet not found!"));
        target.setOwnerName(name);
        return target;
    }

    @Transactional
    public Wallet updateOwnerNameFixed(UUID id, String name){
        Wallet target = walletRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Wallet not found!"));
        target.setOwnerName(name);
        return target;
    }

    public Wallet createWallet(CreateWalletRequest request){
        Wallet wallet = Wallet.builder()
                .ownerName(request.getOwnerName())
                .balance(request.getBalance())
                .build();
        return walletRepository.save(wallet);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferMoneyNaive(UUID fromWalletId, UUID toWalletId, BigDecimal amount){
        Wallet fromWallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Transferring Wallet not found!"));

        BigDecimal remainingBalance = fromWallet.getBalance().subtract(amount);
        if( remainingBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds! Failed to proceed!");
        }

        fromWallet.setBalance(remainingBalance);
        this.walletRepository.save(fromWallet);

        Wallet toWallet = walletRepository.findById(toWalletId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Receiving Wallet not found!"));
        toWallet.setBalance(toWallet.getBalance().add(amount));
        this.walletRepository.save(toWallet);
    }


    public void transferMoneyWithRetries(
            UUID fromId,
            UUID toId,
            BigDecimal amount
    ){
        int attempts = 0;
        while(attempts < MAX_RETRIES){
            try{
                transferMoneyNaive(fromId,toId,amount);
                return;
            }
            catch (ObjectOptimisticLockingFailureException e){
                attempts++;
                if(attempts >= MAX_RETRIES){
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfer failed after " + MAX_RETRIES + " due to concurrency modification");
                }
            }
        }
    }

    @Transactional
    public void transferMoneyPessimistic(
            UUID fromId,
            UUID toId,
            BigDecimal amount
    ){
        Wallet fromWallet = this.walletRepository.findByIdForUpdate(fromId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer wallet not found"));

        BigDecimal remainingBalance = fromWallet.getBalance().subtract(amount);

        if(remainingBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfer account not have sufficient balance");
        }

        fromWallet.setBalance(remainingBalance);
        this.walletRepository.save(fromWallet);

        Wallet toWallet = this.walletRepository.findByIdForUpdate(toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver wallet not found"));

        toWallet.setBalance(toWallet.getBalance().add(amount));
        this.walletRepository.save(toWallet);
    }

    @Transactional
    public void transferMoneyFinal(UUID fromId, UUID toId, BigDecimal amount){

        List<UUID> walletIdsLockOrder = new ArrayList<>(List.of(fromId, toId));
        walletIdsLockOrder.sort(UUID::compareTo);

        Map<UUID, Wallet> lockedWalletsMap = new HashMap<>();
        for(UUID id: walletIdsLockOrder){
            Wallet wallet = walletRepository.findByIdForUpdate(id)
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found")
                    );
            lockedWalletsMap.put(id, wallet);
        }

        Wallet fromWallet = lockedWalletsMap.get(fromId);
        Wallet toWallet = lockedWalletsMap.get(toId);

        BigDecimal remainingBalance = fromWallet.getBalance().subtract(amount);
        if(remainingBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient balance");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        WalletTransaction withdrawal = new WalletTransaction();
        withdrawal.setWallet(fromWallet);
        withdrawal.setAmount(amount);
        withdrawal.setType(TransactionType.TRANSFER);
        withdrawal.setStatus(TransactionStatus.COMPLETED);
        walletTransactionRepository.save(withdrawal);

        WalletTransaction deposit = new WalletTransaction();
        deposit.setWallet(toWallet);
        deposit.setAmount(amount);
        deposit.setType(TransactionType.TRANSFER);
        deposit.setStatus(TransactionStatus.COMPLETED);
        walletTransactionRepository.save(deposit);

    }

}

package com.wallet_simulation_hibernate.demo.service;

import com.wallet_simulation_hibernate.demo.dto.CreateWalletRequest;
import com.wallet_simulation_hibernate.demo.entity.Wallet;
import com.wallet_simulation_hibernate.demo.repositories.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
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
}

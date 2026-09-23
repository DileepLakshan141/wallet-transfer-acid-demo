package com.wallet_simulation_hibernate.demo.controller;

import com.wallet_simulation_hibernate.demo.dto.CreateWalletRequest;
import com.wallet_simulation_hibernate.demo.dto.MoneyTransferRequest;
import com.wallet_simulation_hibernate.demo.dto.UpdateOwnerNameRequest;
import com.wallet_simulation_hibernate.demo.entity.Wallet;
import com.wallet_simulation_hibernate.demo.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PatchMapping("{id}/update-broken")
    public Wallet updateOwnerNameBroken(
            @PathVariable("id") UUID id, @RequestBody @Valid UpdateOwnerNameRequest request
    ){
        return this.walletService.updateOwnerNameBroken(id, request.getUsername());
    }

    @PatchMapping("{id}/update-fixed")
    public Wallet updateOwnerNameFixed(
            @PathVariable("id") UUID id, @RequestBody @Valid UpdateOwnerNameRequest request
    ){
        return this.walletService.updateOwnerNameFixed(id, request.getUsername());
    }

    @PostMapping("/")
    public Wallet createWallet(@RequestBody @Valid CreateWalletRequest request){
        return this.walletService.createWallet(request);
    }

    @PatchMapping("/transfer")
    public void executeMoneyTransfer(@RequestBody @Valid MoneyTransferRequest request){
        this.walletService.transferMoneyNaive(
                request.getFromWalletId() ,
                request.getToWalletId() ,
                request.getAmount());
    }
}

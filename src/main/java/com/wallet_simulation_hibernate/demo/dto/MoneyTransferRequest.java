package com.wallet_simulation_hibernate.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MoneyTransferRequest {

    private UUID fromWalletId;

    private UUID toWalletId;

    private BigDecimal amount = BigDecimal.ZERO;
}

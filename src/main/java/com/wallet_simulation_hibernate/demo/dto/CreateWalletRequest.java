package com.wallet_simulation_hibernate.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateWalletRequest {

    @NotEmpty
    private String ownerName;

    private BigDecimal balance;

}

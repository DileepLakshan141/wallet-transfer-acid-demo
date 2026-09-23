package com.wallet_simulation_hibernate.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
public class UpdateOwnerNameRequest {

    @NotEmpty
    private String username;

}

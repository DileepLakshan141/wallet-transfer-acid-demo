package com.wallet_simulation_hibernate.demo.repositories;

import com.wallet_simulation_hibernate.demo.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
}

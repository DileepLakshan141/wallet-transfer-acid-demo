package com.wallet_simulation_hibernate.demo.repositories;

import com.wallet_simulation_hibernate.demo.entity.Wallet;
import com.wallet_simulation_hibernate.demo.entity.WalletTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT w FROM Wallet w WHERE w.ownerName LIKE %:name%")
    List<Wallet> findByOwnerNameContaining(@Param("name") String name);

    @Query("SELECT COUNT(t) FROM WalletTransaction t WHERE t.wallet.id = :id")
    long countTransactionsByWalletId(@Param("id") UUID id);

    @Query("SELECT t FROM WalletTransaction t JOIN FETCH t.wallet WHERE t.wallet.id = :id")
    List<WalletTransaction> findTransactionHistoryWithWallet(@Param("id") UUID id);
}

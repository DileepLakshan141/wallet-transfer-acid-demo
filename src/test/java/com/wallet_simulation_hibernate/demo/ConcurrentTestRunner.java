package com.wallet_simulation_hibernate.demo;

import com.wallet_simulation_hibernate.demo.entity.Wallet;
import com.wallet_simulation_hibernate.demo.repositories.WalletRepository;
import com.wallet_simulation_hibernate.demo.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class ConcurrentTestRunner {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void concurrentTransfersFromSameWallet_causeLostUpdate() throws InterruptedException {
        Wallet walletA = Wallet.builder()
                .ownerName("Wallet A")
                .balance(new BigDecimal("1000.00"))
                .build();
        walletA = walletRepository.save(walletA);

        Wallet walletB = Wallet.builder()
                .ownerName("Wallet B")
                .balance(new BigDecimal("700.00"))
                .build();
        walletB = walletRepository.save(walletB);

        CountDownLatch startLatch = new CountDownLatch(2);

        UUID walletAId = walletA.getId();
        UUID walletBId = walletB.getId();


        Thread thread1 = new Thread(() -> {
            try {
                startLatch.countDown();
                startLatch.await();

                walletService.transferMoneyFinal(walletAId, walletBId , new BigDecimal("400.00"));
                System.out.println("Thread 1 (A -> B) completed");

            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }catch (Exception e){
                System.out.println("Thread 1 failed! " + e.getMessage());
            }
        });

        Thread thread2 = new Thread(() -> {
            try{
                startLatch.countDown();
                startLatch.await();

                walletService.transferMoneyFinal(walletBId, walletAId, new BigDecimal("500.00"));
                System.out.println("Thread 2 (B -> A) completed");

            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            } catch (ObjectOptimisticLockingFailureException e){
                System.out.println("Object optimistic exception detected " + e.getMessage());
            } catch (Exception e){
                System.out.println("Thread 2 failed " + e.getMessage());
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        Wallet finalWalletA = walletRepository.findById(walletAId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));
        Wallet finalWalletB = walletRepository.findById(walletBId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));
        System.out.println("Final Wallet A Balance: " + finalWalletA.getBalance());
        System.out.println("Final Wallet B Balance: " + finalWalletB.getBalance());
        System.out.println("If you see 1100.00 and 600.00 → deadlock prevented");
    }
}

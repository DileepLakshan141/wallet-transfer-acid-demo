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
                .balance(BigDecimal.ZERO)
                .build();
        walletB = walletRepository.save(walletB);

        Wallet walletC = Wallet.builder()
                .ownerName("Wallet C")
                .balance(BigDecimal.ZERO)
                .build();
        walletC = walletRepository.save(walletC);

        UUID fromWalletId = walletA.getId();
        UUID toIdB = walletB.getId();
        UUID toIdC = walletC.getId();

        CountDownLatch startLatch = new CountDownLatch(2);


        Thread thread1 = new Thread(() -> {
            try {
                startLatch.countDown();
                startLatch.await();

                walletService.transferMoneyWithRetries(fromWalletId, toIdB , new BigDecimal("500.00"));
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

                walletService.transferMoneyWithRetries(fromWalletId, toIdC, new BigDecimal("500.00"));
                System.out.println("Thread 2 (A -> C) completed");

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

        Wallet finalWallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));
        System.out.println("Final Wallet Balance: " + finalWallet.getBalance());
        System.out.println("Expected (correct): 0.00");
        System.out.println("If you see 500.00 → Lost Update occurred (race condition confirmed)");
    }
}

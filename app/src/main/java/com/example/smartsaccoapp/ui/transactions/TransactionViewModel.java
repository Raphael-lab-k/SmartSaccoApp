package com.example.smartsaccoapp.ui.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Account;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransactionViewModel extends ViewModel {
    private final SaccoRepository repository;

    @Inject
    public TransactionViewModel(SaccoRepository repository) {
        this.repository = repository;
    }

    public LiveData<Account> getAccount() {
        return repository.getAccount();
    }

    public void deposit(double amount) {
        repository.deposit(amount);
    }

    public void withdraw(double amount) {
        repository.withdraw(amount);
    }
}
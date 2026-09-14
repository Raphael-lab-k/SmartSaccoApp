package com.example.smartsaccoapp.ui.savings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Transaction;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SavingsViewModel extends ViewModel {

    private final SaccoRepository repository;

    @Inject
    public SavingsViewModel(SaccoRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return repository.getAllTransactions();
    }
}
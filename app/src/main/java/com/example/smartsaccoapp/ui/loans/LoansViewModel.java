package com.example.smartsaccoapp.ui.loans;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Loan;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoansViewModel extends ViewModel {

    private final SaccoRepository repository;

    @Inject
    public LoansViewModel(SaccoRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Loan>> getAllLoans() {
        return repository.getAllLoans();
    }
}
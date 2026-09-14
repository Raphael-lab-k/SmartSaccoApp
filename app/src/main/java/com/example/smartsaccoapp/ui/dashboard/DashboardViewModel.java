package com.example.smartsaccoapp.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Account;
import com.example.smartsaccoapp.data.db.Loan;
import com.example.smartsaccoapp.data.db.Member;
import com.example.smartsaccoapp.data.db.SavingPot;
import com.example.smartsaccoapp.data.db.Transaction;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DashboardViewModel extends ViewModel {

    private final SaccoRepository repository;

    @Inject
    public DashboardViewModel(SaccoRepository repository) {
        this.repository = repository;
    }

    public LiveData<Account> getAccount() {
        return repository.getAccount();
    }

    public LiveData<List<Loan>> getMemberLoans(String email) {
        return repository.getLoansByMember(email);
    }

    public LiveData<List<SavingPot>> getMemberPots(String email) {
        return repository.getPotsByMember(email);
    }

    public Member getMember(String email) {
        return repository.getMemberByEmail(email);
    }

    public LiveData<List<Transaction>> getLastFiveTransactions() {
        return repository.getLastFiveTransactions();
    }
}

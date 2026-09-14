package com.example.smartsaccoapp.ui.guarantors;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Guarantor;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GuarantorViewModel extends ViewModel {
    private final SaccoRepository repository;
    private final LiveData<List<Guarantor>> allGuarantors;

    @Inject
    public GuarantorViewModel(SaccoRepository repository) {
        this.repository = repository;
        allGuarantors = repository.getAllGuarantors();
    }

    public LiveData<List<Guarantor>> getAllGuarantors() {
        return allGuarantors;
    }

    public void insert(Guarantor guarantor) {
        repository.insertGuarantor(guarantor);
    }
    
    public void update(Guarantor guarantor) {
        repository.updateGuarantor(guarantor);
    }
}
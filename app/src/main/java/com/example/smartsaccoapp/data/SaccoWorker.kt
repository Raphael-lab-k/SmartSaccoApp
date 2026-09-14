package com.example.smartsaccoapp.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.smartsaccoapp.data.db.SaccoDatabase

class SaccoWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = SaccoDatabase.getDatabase(applicationContext)
        
        // 1. Process Overdue Loans
        val loans = db.loanDao().allLoansSync
        val now = System.currentTimeMillis()
        
        if (loans != null) {
            for (loan in loans) {
                if ("APPROVED" == loan.status && now > loan.dueDate) {
                    loan.status = "OVERDUE"
                    // Apply 5% penalty
                    loan.amount = loan.amount * 1.05
                    db.loanDao().update(loan)
                }
            }
        }

        return Result.success()
    }
}

package com.example.smartsaccoapp.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smartsaccoapp.data.db.SavingPot;
import com.example.smartsaccoapp.data.db.SavingPotDao;
import com.example.smartsaccoapp.data.db.PotContribution;
import com.example.smartsaccoapp.data.db.PotContributionDao;

@Database(entities = {Account.class, Transaction.class, Loan.class, Guarantor.class, Member.class, Branch.class, AuditLog.class, SavingPot.class, PotContribution.class, Vouch.class, MarketItem.class}, version = 7)
public abstract class SaccoDatabase extends RoomDatabase {
    public abstract AccountDao accountDao();
    public abstract TransactionDao transactionDao();
    public abstract LoanDao loanDao();
    public abstract GuarantorDao guarantorDao();
    public abstract MemberDao memberDao();
    public abstract BranchDao branchDao();
    public abstract AuditLogDao auditLogDao();
    public abstract SavingPotDao savingPotDao();
    public abstract PotContributionDao potContributionDao();
    public abstract VouchDao vouchDao();
    public abstract MarketItemDao marketItemDao();

    private static volatile SaccoDatabase INSTANCE;

    public static SaccoDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SaccoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    SaccoDatabase.class, "sacco_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
package com.example.smartsaccoapp.di

import android.content.Context
import com.example.smartsaccoapp.data.db.SaccoDatabase
import com.example.smartsaccoapp.data.db.SavingPotDao
import com.example.smartsaccoapp.data.db.PotContributionDao
import com.example.smartsaccoapp.data.db.VouchDao
import com.example.smartsaccoapp.data.db.MarketItemDao
import com.example.smartsaccoapp.data.db.AuditLogDao
import com.example.smartsaccoapp.data.db.AccountDao
import com.example.smartsaccoapp.data.db.GuarantorDao
import com.example.smartsaccoapp.data.db.LoanDao
import com.example.smartsaccoapp.data.db.BranchDao
import com.example.smartsaccoapp.data.db.MemberDao
import com.example.smartsaccoapp.data.db.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SaccoDatabase {
        return SaccoDatabase.getDatabase(context)
    }

    @Provides
    fun provideAccountDao(db: SaccoDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideTransactionDao(db: SaccoDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideLoanDao(db: SaccoDatabase): LoanDao = db.loanDao()

    @Provides
    fun provideGuarantorDao(db: SaccoDatabase): GuarantorDao = db.guarantorDao()

    @Provides
    fun provideMemberDao(db: SaccoDatabase): MemberDao = db.memberDao()

    @Provides
    fun provideBranchDao(db: SaccoDatabase): BranchDao = db.branchDao()

    @Provides
    fun provideAuditLogDao(db: SaccoDatabase): AuditLogDao = db.auditLogDao()

    @Provides
    fun provideSavingPotDao(db: SaccoDatabase): SavingPotDao = db.savingPotDao()

    @Provides
    fun providePotContributionDao(db: SaccoDatabase): PotContributionDao = db.potContributionDao()

    @Provides
    fun provideVouchDao(db: SaccoDatabase): VouchDao = db.vouchDao()

    @Provides
    fun provideMarketItemDao(db: SaccoDatabase): MarketItemDao = db.marketItemDao()
}

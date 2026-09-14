package com.example.smartsaccoapp.data

import androidx.lifecycle.LiveData
import com.example.smartsaccoapp.data.db.*
import java.util.Locale
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaccoRepository @Inject constructor(
    private val db: SaccoDatabase,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val loanDao: LoanDao,
    private val guarantorDao: GuarantorDao,
    private val memberDao: MemberDao,
    private val branchDao: BranchDao,
    private val auditLogDao: AuditLogDao,
    private val savingPotDao: SavingPotDao,
    private val potContributionDao: PotContributionDao,
    private val vouchDao: VouchDao,
    private val marketItemDao: MarketItemDao,
    private val firebaseManager: FirebaseManager,
    private val authManager: AuthManager
) {
    private val executorService = Executors.newFixedThreadPool(4)

    fun getAccount(): LiveData<Account> = accountDao.getAccount()

    fun getAllTransactions(): LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    fun getLastFiveTransactions(): LiveData<List<Transaction>> = transactionDao.getLastFiveTransactions()

    fun getAllLoans(): LiveData<List<Loan>> = loanDao.getAllLoans()

    fun getLoansByMember(email: String): LiveData<List<Loan>> = loanDao.getLoansByMember(email)

    fun getAllLoansSync(): List<Loan>? {
        val future = executorService.submit<List<Loan>> { loanDao.getAllLoansSync() }
        return try {
            future.get()
        } catch (e: Exception) {
            null
        }
    }

    fun getAllGuarantors(): LiveData<List<Guarantor>> = guarantorDao.getAllGuarantors()

    fun getAllAuditLogs(): LiveData<List<AuditLog>> = auditLogDao.getAllLogs()

    fun getActivePots(): LiveData<List<SavingPot>> = savingPotDao.getActivePots()

    fun getPotsByMember(email: String): LiveData<List<SavingPot>> = savingPotDao.getPotsByMember(email)

    fun getContributionsForPot(potId: Int): LiveData<List<PotContribution>> = 
        potContributionDao.getContributionsForPot(potId)

    fun createSavingPot(pot: SavingPot) {
        executorService.execute {
            val id = savingPotDao.insert(pot)
            pot.id = id.toInt()
            firebaseManager.syncSavingPot(pot)
        }
    }

    fun contributeToPot(potId: Int, memberEmail: String, amount: Double) {
        executorService.execute {
            db.runInTransaction {
                val account = accountDao.getAccountSync()
                if (account != null && account.balance >= amount) {
                    account.balance -= amount
                    accountDao.update(account)

                    val pot = savingPotDao.getPotSync(potId)
                    if (pot != null) {
                        pot.currentAmount += amount
                        if (pot.currentAmount >= pot.targetAmount) {
                            pot.status = "ACHIEVED"
                        }
                        savingPotDao.update(pot)
                        firebaseManager.syncSavingPot(pot)

                        val contrib = PotContribution(potId, memberEmail, amount, System.currentTimeMillis())
                        potContributionDao.insert(contrib)
                        firebaseManager.recordPotContribution(contrib)

                        transactionDao.insert(Transaction("POT_CONTRIBUTION", amount, System.currentTimeMillis(), "Contributed to ${pot.name}"))
                        auditLogDao.insert(AuditLog(System.currentTimeMillis(), memberEmail, "POT_CONTRIBUTION", "Added UGX $amount to pot ${pot.name}"))
                    }
                }
            }
        }
    }

    fun insertTransaction(transaction: Transaction) {
        executorService.execute { transactionDao.insert(transaction) }
    }

    fun insertAccount(account: Account) {
        executorService.execute { accountDao.insert(account) }
    }

    fun insertLoan(loan: Loan, callback: LoanCallback? = null) {
        executorService.execute {
            try {
                db.runInTransaction {
                    val member = memberDao.getMemberByEmail(loan.memberEmail)
                    
                    // Auto-approval logic
                    if (loan.riskLevel == "LOW" && loan.amount <= 1000000 && member?.kycStatus == "VERIFIED") {
                        loan.status = "APPROVED"
                        
                        val account = accountDao.getAccountSync()
                        if (account != null) {
                            account.balance += loan.amount
                            accountDao.update(account)
                            
                            transactionDao.insert(Transaction("LOAN_DISBURSEMENT", loan.amount, System.currentTimeMillis(), "Auto-Approved: ${loan.type}"))
                            auditLogDao.insert(AuditLog(System.currentTimeMillis(), "SYSTEM", "AUTO_APPROVE", "Instantly approved loan for ${loan.memberEmail}"))
                        }
                    } else {
                        loan.status = "PENDING"
                    }

                    loanDao.insert(loan)
                    firebaseManager.syncLoan(loan)
                }
                callback?.onSuccess(loan)
            } catch (e: Exception) {
                callback?.onError(e.message)
            }
        }
    }

    interface LoanCallback {
        fun onSuccess(loan: Loan)
        fun onError(message: String?)
    }

    fun insertGuarantor(guarantor: Guarantor) {
        executorService.execute { guarantorDao.insert(guarantor) }
    }

    fun getAllBranches(): List<Branch>? {
        val future = executorService.submit<List<Branch>> { branchDao.getAllBranches() }
        return try {
            future.get()
        } catch (e: Exception) {
            null
        }
    }

    fun updateGuarantor(guarantor: Guarantor) {
        executorService.execute { guarantorDao.update(guarantor) }
    }

    fun getAllMembers(): LiveData<List<Member>> = memberDao.getAllMembers()

    fun getMembersSync(): List<Member> {
        val future = executorService.submit<List<Member>> { memberDao.getMembersSync() }
        return try {
            future.get() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun insertMember(member: Member) {
        executorService.execute { memberDao.insert(member) }
    }

    fun insertBranch(branch: Branch) {
        executorService.execute { branchDao.insert(branch) }
    }

    fun updateMember(member: Member) {
        executorService.execute { memberDao.update(member) }
    }

    fun insertAuditLog(actor: String, action: String, details: String) {
        executorService.execute { 
            auditLogDao.insert(AuditLog(System.currentTimeMillis(), actor, action, details)) 
        }
    }

    fun getMemberByEmail(email: String): Member? {
        val future = executorService.submit<Member> { memberDao.getMemberByEmail(email) }
        return try {
            future.get()
        } catch (e: Exception) {
            null
        }
    }

    fun getVouchesForMember(email: String): LiveData<List<Vouch>> {
        return vouchDao.getVouchesForMember(email)
    }

    fun getMemberCount(): Int {
        val future = executorService.submit<Int> { memberDao.getMemberCount() }
        return try {
            future.get()
        } catch (e: Exception) {
            0
        }
    }

    fun getPendingKycCount(): Int {
        val future = executorService.submit<Int> { memberDao.getPendingKycCount() }
        return try {
            future.get()
        } catch (e: Exception) {
            0
        }
    }

    fun getLoanCount(): Int {
        val future = executorService.submit<Int> { loanDao.getLoanCount() }
        return try {
            future.get()
        } catch (e: Exception) {
            0
        }
    }

    fun getTotalDeposits(): Double {
        val future = executorService.submit<Double> { transactionDao.getTotalDeposits() }
        return try {
            future.get()
        } catch (e: Exception) {
            0.0
        }
    }

    fun getTotalWithdrawals(): Double {
        val future = executorService.submit<Double> { transactionDao.getTotalWithdrawals() }
        return try {
            future.get()
        } catch (e: Exception) {
            0.0
        }
    }

    fun updateLoan(loan: Loan) {
        executorService.execute { loanDao.update(loan) }
    }

    fun approveLoan(loan: Loan, adminEmail: String) {
        executorService.execute {
            db.runInTransaction {
                loan.status = "APPROVED"
                loanDao.update(loan)

                val account = accountDao.getAccountSync()
                if (account != null) {
                    account.balance += loan.amount
                    accountDao.update(account)
                    transactionDao.insert(Transaction("LOAN_DISBURSEMENT", loan.amount, System.currentTimeMillis(), "Approved: ${loan.type}"))
                    auditLogDao.insert(AuditLog(System.currentTimeMillis(), adminEmail, "LOAN_APPROVE", "Approved loan ID ${loan.id} for ${loan.memberEmail}"))
                }
            }
        }
    }

    fun repayLoan(loan: Loan, amount: Double) {
        executorService.execute {
            db.runInTransaction {
                val account = accountDao.getAccountSync()
                if (account != null && account.balance >= amount) {
                    account.balance -= amount
                    accountDao.update(account)

                    loan.repaidAmount += amount
                    if (loan.repaidAmount >= (loan.amount * (1 + loan.interestRate))) {
                        loan.status = "PAID"
                    }
                    loanDao.update(loan)

                    transactionDao.insert(Transaction("LOAN_REPAYMENT", amount, System.currentTimeMillis(), "Repayment for ${loan.type}"))
                    auditLogDao.insert(AuditLog(System.currentTimeMillis(), loan.memberEmail, "LOAN_REPAYMENT", "Repaid UGX ${String.format(Locale.getDefault(), "%,.0f", amount)} for loan ${loan.id}"))
                }
            }
        }
    }

    fun getAvailableMarketItems(): LiveData<List<MarketItem>> = marketItemDao.getAvailableItems()

    fun listMarketItem(item: MarketItem) {
        executorService.execute {
            marketItemDao.insert(item)
            firebaseManager.syncMarketItem(item)
        }
    }

    fun buyMarketItem(itemId: Int, buyerEmail: String) {
        executorService.execute {
            db.runInTransaction {
                val item = marketItemDao.getItemSync(itemId)
                val buyerAccount = accountDao.getAccountSync()
                
                if (item != null && buyerAccount != null && buyerAccount.balance >= item.price) {
                    buyerAccount.balance -= item.price
                    accountDao.update(buyerAccount)
                    
                    item.status = "ESCROW"
                    marketItemDao.update(item)
                    firebaseManager.syncMarketItem(item)

                    transactionDao.insert(Transaction("MARKET_PURCHASE", item.price, System.currentTimeMillis(), "Bought: ${item.title}"))
                    auditLogDao.insert(AuditLog(System.currentTimeMillis(), buyerEmail, "MARKET_BUY", "Purchased ${item.title} for UGX ${item.price}"))
                }
            }
        }
    }

    fun calculateRiskLevel(email: String, loanAmount: Double): String {
        val account = accountDao.getAccountSync()
        val member = memberDao.getMemberByEmail(email)
        val loans = loanDao.getLoansByMemberSync(email)
        
        if (account == null || member == null) return "HIGH"
        
        // 1. Balance Score (40%)
        val financialRatio = (account.balance / loanAmount).coerceAtMost(1.0)
        val balanceScore = financialRatio * 0.4
        
        // 2. Trust Score (30%)
        val trustFactor = member.trustScore / 1000.0
        val trustScore = trustFactor * 0.3
        
        // 3. History Score (20%)
        var historyFactor = 1.0
        if (loans != null) {
            for (loan in loans) {
                if (loan.status == "OVERDUE") historyFactor -= 0.5
                if (loan.status == "PAID") historyFactor += 0.1
            }
        }
        historyFactor = historyFactor.coerceIn(0.0, 1.0)
        val historyScore = historyFactor * 0.2
        
        // 4. KYC Status (10%)
        val kycFactor = if (member.kycStatus == "VERIFIED") 1.0 else 0.0
        val kycScore = kycFactor * 0.1
        
        val totalRiskScore = balanceScore + trustScore + historyScore + kycScore
        
        return when {
            totalRiskScore > 0.7 -> "LOW"
            totalRiskScore > 0.4 -> "MEDIUM"
            else -> "HIGH"
        }
    }

    fun getSafeLoanLimit(email: String): Double {
        val account = accountDao.getAccountSync()
        val member = memberDao.getMemberByEmail(email)
        if (account == null || member == null) return 0.0
        
        return (account.balance * 3) + (member.trustScore * 100.0)
    }

    fun deposit(amount: Double) {
        executorService.execute {
            db.runInTransaction {
                var account = accountDao.getAccountSync()
                if (account == null) {
                    account = Account(0.0, "ACC-DEMO")
                    accountDao.insert(account)
                    // Re-fetch to ensure we have the one from DB if ID is needed, 
                    // but for a single entry LIMIT 1 it might not matter much if we update it.
                    // However, update() needs the primary key ID.
                    account = accountDao.getAccountSync()
                }
                
                if (account != null) {
                    account.balance += amount
                    accountDao.update(account)
                    val transaction = Transaction("DEPOSIT", amount, System.currentTimeMillis(), "Digital Deposit")
                    transactionDao.insert(transaction)
                    
                    val email = authManager.getUserEmail() ?: "unknown@demo.com"
                    firebaseManager.recordTransaction(transaction, email)
                    
                    auditLogDao.insert(AuditLog(System.currentTimeMillis(), email, "DEPOSIT", "Amount: UGX ${String.format(Locale.getDefault(), "%,.0f", amount)}"))
                }
            }
        }
    }

    fun withdraw(amount: Double) {
        executorService.execute {
            db.runInTransaction {
                val account = accountDao.getAccountSync()
                if (account != null && account.balance >= amount) {
                    account.balance -= amount
                    accountDao.update(account)
                    val transaction = Transaction("WITHDRAWAL", amount, System.currentTimeMillis(), "Digital Withdrawal")
                    transactionDao.insert(transaction)
                    
                    val email = authManager.getUserEmail() ?: "unknown@demo.com"
                    firebaseManager.recordTransaction(transaction, email)
                    
                    auditLogDao.insert(AuditLog(System.currentTimeMillis(), email, "WITHDRAWAL", "Amount: UGX ${String.format(Locale.getDefault(), "%,.0f", amount)}"))
                }
            }
        }
    }

    fun transferFunds(senderEmail: String, recipientEmail: String, amount: Double, callback: TransferCallback?) {
        executorService.execute {
            try {
                db.runInTransaction {
                    val recipient = memberDao.getMemberByEmail(recipientEmail) 
                        ?: throw RuntimeException("RECIPIENT_NOT_FOUND")
                    
                    if (senderEmail == recipientEmail) {
                        throw RuntimeException("SELF_TRANSFER")
                    }

                    val senderAccount = accountDao.getAccountSync()
                    if (senderAccount == null || senderAccount.balance < amount) {
                        throw RuntimeException("INSUFFICIENT_BALANCE")
                    }

                    senderAccount.balance -= amount
                    accountDao.update(senderAccount)
                    
                    transactionDao.insert(Transaction("TRANSFER_OUT", amount, System.currentTimeMillis(), "Transfer to $recipientEmail"))
                    
                    auditLogDao.insert(AuditLog(System.currentTimeMillis(), senderEmail, "TRANSFER", "Sent UGX ${String.format(Locale.getDefault(), "%,.0f", amount)} to $recipientEmail"))
                }
                callback?.onSuccess()
            } catch (e: Exception) {
                callback?.onError(e.message)
            }
        }
    }

    fun vouchMember(voucherEmail: String, voucheeEmail: String, callback: VouchCallback?) {
        executorService.execute {
            try {
                db.runInTransaction {
                    val existingVouch = vouchDao.getVouch(voucherEmail, voucheeEmail)
                    if (existingVouch != null) {
                        throw RuntimeException("ALREADY_VOUCHED")
                    }

                    if (voucherEmail == voucheeEmail) {
                        throw RuntimeException("SELF_VOUCH")
                    }

                    val vouchee = memberDao.getMemberByEmail(voucheeEmail)
                        ?: throw RuntimeException("MEMBER_NOT_FOUND")

                    // Insert Vouch
                    vouchDao.insert(Vouch(voucherEmail, voucheeEmail, System.currentTimeMillis()))

                    // Update Score (+50, cap 1000)
                    vouchee.trustScore = (vouchee.trustScore + 50).coerceAtMost(1000)
                    memberDao.update(vouchee)
                    firebaseManager.saveMemberProfile(vouchee)

                    auditLogDao.insert(AuditLog(System.currentTimeMillis(), voucherEmail, "MEMBER_VOUCH", "Vouched for $voucheeEmail. New Score: ${vouchee.trustScore}"))
                }
                callback?.onSuccess()
            } catch (e: Exception) {
                callback?.onError(e.message)
            }
        }
    }

    interface VouchCallback {
        fun onSuccess()
        fun onError(message: String?)
    }

    interface TransferCallback {
        fun onSuccess()
        fun onError(message: String?)
    }
}

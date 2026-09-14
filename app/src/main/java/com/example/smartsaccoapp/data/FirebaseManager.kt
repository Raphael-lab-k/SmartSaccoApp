package com.example.smartsaccoapp.data

import com.example.smartsaccoapp.data.db.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var instance: FirebaseManager? = null

        @JvmStatic
        fun getInstance(): FirebaseManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseManager().also { instance = it }
            }
        }
    }

    fun registerUser(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun loginUser(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun saveMemberProfile(member: Member?) {
        if (member == null) return
        db.collection("members").document(member.email).set(member)
    }

    fun getMemberProfile(email: String): Task<DocumentSnapshot> {
        return db.collection("members").document(email).get()
    }

    fun syncLoan(loan: Loan?) {
        if (loan == null) return
        db.collection("loans").document(loan.id.toString()).set(loan)
    }

    fun syncSavingPot(pot: SavingPot?) {
        if (pot == null) return
        db.collection("saving_pots").document(pot.id.toString()).set(pot)
    }

    fun syncMarketItem(item: MarketItem?) {
        if (item == null) return
        db.collection("marketplace").document(item.id.toString()).set(item)
    }

    fun recordPotContribution(contribution: PotContribution?) {
        if (contribution == null) return
        db.collection("saving_pots").document(contribution.potId.toString())
            .collection("contributions").add(contribution)
    }

    fun recordTransaction(transaction: Transaction?, userEmail: String) {
        if (transaction == null) return
        db.collection("members").document(userEmail)
            .collection("transactions").add(transaction)
    }

    fun getAuth(): FirebaseAuth = auth

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun logout() {
        auth.signOut()
    }
}

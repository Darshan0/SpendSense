package com.spendsense.features.finance.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spendsense.features.finance.domain.PaymentMethod
import com.spendsense.features.finance.domain.SpendingGoal
import com.spendsense.features.finance.domain.SpendingGoalRepository
import com.spendsense.features.finance.domain.Transaction
import com.spendsense.features.finance.domain.TransactionCategory
import com.spendsense.features.finance.domain.TransactionRepository
import com.spendsense.features.finance.domain.TransactionStatus
import com.spendsense.features.finance.domain.TransactionType
import com.spendsense.features.finance.domain.VerificationStatus
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val status: String,
    val amountMinor: Long,
    val currency: String,
    val merchantName: String?,
    val category: String,
    val paymentMethod: String?,
    val accountLast4: String?,
    val transactionTimeEpochMillis: Long?,
    val sourcePackage: String,
    val confidence: Float,
    val verificationStatus: String,
    val createdAtEpochMillis: Long,
)

@Entity(tableName = "spending_goals")
data class SpendingGoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long,
    val currency: String,
    val targetDateEpochMillis: Long?,
    val createdAtEpochMillis: Long,
)

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        ORDER BY COALESCE(transactionTimeEpochMillis, createdAtEpochMillis) DESC
        """,
    )
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE sourcePackage = 'dev.seed'")
    suspend fun seedCount(): Int

    @Query("DELETE FROM transactions WHERE sourcePackage = 'dev.seed'")
    suspend fun deleteSeedData()
}

@Dao
interface SpendingGoalDao {
    @Query("SELECT * FROM spending_goals ORDER BY createdAtEpochMillis DESC")
    fun observeGoals(): Flow<List<SpendingGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SpendingGoalEntity)

    @Query("DELETE FROM spending_goals WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM spending_goals")
    suspend fun count(): Int
}

@Database(
    entities = [TransactionEntity::class, SpendingGoalEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class SpendSenseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun spendingGoalDao(): SpendingGoalDao

    companion object {
        fun create(context: Context): SpendSenseDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SpendSenseDatabase::class.java,
                "spendsense.db",
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}

class RoomTransactionRepository(
    private val dao: TransactionDao,
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>> {
        return dao.observeTransactions().map { entities ->
            entities.map(TransactionEntity::toDomain)
        }
    }

    override suspend fun insert(transaction: Transaction) {
        dao.insert(transaction.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }

    suspend fun seedDevelopmentData() {
        val now = Instant.now()
        dao.deleteSeedData()

        listOf(
            Transaction(
                id = "seed-salary",
                type = TransactionType.CREDIT,
                amountMinor = 27500000,
                merchantName = "Salary",
                category = TransactionCategory.SALARY,
                paymentMethod = null,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 2),
                sourcePackage = "dev.seed",
                confidence = 0.95f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-rent",
                type = TransactionType.DEBIT,
                amountMinor = 4200000,
                merchantName = "Prestige Lakeside",
                category = TransactionCategory.RENT,
                paymentMethod = PaymentMethod.NET_BANKING,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 9),
                sourcePackage = "dev.seed",
                confidence = 0.96f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-grocery-bigbasket",
                type = TransactionType.DEBIT,
                amountMinor = 385000,
                merchantName = "BigBasket",
                category = TransactionCategory.GROCERIES,
                paymentMethod = PaymentMethod.UPI,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 3),
                sourcePackage = "dev.seed",
                confidence = 0.94f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-grocery-dmart",
                type = TransactionType.DEBIT,
                amountMinor = 246000,
                merchantName = "DMart",
                category = TransactionCategory.GROCERIES,
                paymentMethod = PaymentMethod.CARD,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 13),
                sourcePackage = "dev.seed",
                confidence = 0.93f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-food-swiggy-1",
                type = TransactionType.DEBIT,
                amountMinor = 42800,
                merchantName = "Swiggy",
                category = TransactionCategory.FOOD,
                paymentMethod = PaymentMethod.UPI,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(3600),
                sourcePackage = "dev.seed",
                confidence = 0.98f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-food-swiggy-2",
                type = TransactionType.DEBIT,
                amountMinor = 61200,
                merchantName = "Swiggy",
                category = TransactionCategory.FOOD,
                paymentMethod = PaymentMethod.UPI,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 4),
                sourcePackage = "dev.seed",
                confidence = 0.97f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-food-zomato",
                type = TransactionType.DEBIT,
                amountMinor = 87500,
                merchantName = "Zomato",
                category = TransactionCategory.FOOD,
                paymentMethod = PaymentMethod.UPI,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 8),
                sourcePackage = "dev.seed",
                confidence = 0.96f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-fuel-shell",
                type = TransactionType.DEBIT,
                amountMinor = 68000,
                merchantName = "Shell Petroleum",
                category = TransactionCategory.FUEL,
                paymentMethod = PaymentMethod.UPI,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 5),
                sourcePackage = "dev.seed",
                confidence = 0.98f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-transport-uber",
                type = TransactionType.DEBIT,
                amountMinor = 126000,
                merchantName = "Uber",
                category = TransactionCategory.TRANSPORT,
                paymentMethod = PaymentMethod.UPI,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 7),
                sourcePackage = "dev.seed",
                confidence = 0.94f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-shopping-amazon",
                type = TransactionType.DEBIT,
                amountMinor = 319900,
                merchantName = "Amazon",
                category = TransactionCategory.SHOPPING,
                paymentMethod = PaymentMethod.CARD,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 12),
                sourcePackage = "dev.seed",
                confidence = 0.93f,
                verificationStatus = VerificationStatus.NEEDS_REVIEW,
            ),
            Transaction(
                id = "seed-subscriptions",
                type = TransactionType.DEBIT,
                amountMinor = 149900,
                merchantName = "Netflix",
                category = TransactionCategory.SUBSCRIPTION,
                paymentMethod = PaymentMethod.CARD,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 16),
                sourcePackage = "dev.seed",
                confidence = 0.96f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-utilities-bescom",
                type = TransactionType.DEBIT,
                amountMinor = 214000,
                merchantName = "BESCOM",
                category = TransactionCategory.UTILITIES,
                paymentMethod = PaymentMethod.UPI,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 19),
                sourcePackage = "dev.seed",
                confidence = 0.95f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-healthcare",
                type = TransactionType.DEBIT,
                amountMinor = 145000,
                merchantName = "Apollo Pharmacy",
                category = TransactionCategory.HEALTHCARE,
                paymentMethod = PaymentMethod.UPI,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 21),
                sourcePackage = "dev.seed",
                confidence = 0.92f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
            Transaction(
                id = "seed-mutual-fund",
                type = TransactionType.DEBIT,
                amountMinor = 500000,
                merchantName = "Groww SIP",
                category = TransactionCategory.INVESTMENT,
                paymentMethod = PaymentMethod.NET_BANKING,
                accountLast4 = "8831",
                transactionTime = now.minusSeconds(86_400 * 23),
                sourcePackage = "dev.seed",
                confidence = 0.96f,
                verificationStatus = VerificationStatus.AUTO_VERIFIED,
            ),
        ).forEach { insert(it) }
    }
}

class RoomSpendingGoalRepository(
    private val dao: SpendingGoalDao,
) : SpendingGoalRepository {
    override fun observeGoals(): Flow<List<SpendingGoal>> {
        return dao.observeGoals().map { goals -> goals.map(SpendingGoalEntity::toDomain) }
    }

    override suspend fun insert(goal: SpendingGoal) {
        dao.insert(goal.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }

    suspend fun seedDevelopmentData() {
        if (dao.count() > 0) return

        insert(
            SpendingGoal(
                name = "Emergency Fund",
                targetAmountMinor = 10000000,
                currentAmountMinor = 2750000,
                targetDateEpochMillis = null,
            ),
        )
        insert(
            SpendingGoal(
                name = "New Laptop",
                targetAmountMinor = 18000000,
                currentAmountMinor = 3200000,
                targetDateEpochMillis = null,
            ),
        )
    }
}

private fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        type = type.name,
        status = status.name,
        amountMinor = amountMinor,
        currency = currency,
        merchantName = merchantName,
        category = category.name,
        paymentMethod = paymentMethod?.name,
        accountLast4 = accountLast4,
        transactionTimeEpochMillis = transactionTime?.toEpochMilli(),
        sourcePackage = sourcePackage,
        confidence = confidence,
        verificationStatus = verificationStatus.name,
        createdAtEpochMillis = createdAt.toEpochMilli(),
    )
}

private fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        type = enumValueOf(type),
        status = enumValueOf(status),
        amountMinor = amountMinor,
        currency = currency,
        merchantName = merchantName,
        category = enumValueOf(category),
        paymentMethod = paymentMethod?.let { enumValueOf<PaymentMethod>(it) },
        accountLast4 = accountLast4,
        transactionTime = transactionTimeEpochMillis?.let(Instant::ofEpochMilli),
        sourcePackage = sourcePackage,
        confidence = confidence,
        verificationStatus = enumValueOf(verificationStatus),
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    )
}

private fun SpendingGoal.toEntity(): SpendingGoalEntity {
    return SpendingGoalEntity(
        id = id,
        name = name,
        targetAmountMinor = targetAmountMinor,
        currentAmountMinor = currentAmountMinor,
        currency = currency,
        targetDateEpochMillis = targetDateEpochMillis,
        createdAtEpochMillis = createdAt.toEpochMilli(),
    )
}

private fun SpendingGoalEntity.toDomain(): SpendingGoal {
    return SpendingGoal(
        id = id,
        name = name,
        targetAmountMinor = targetAmountMinor,
        currentAmountMinor = currentAmountMinor,
        currency = currency,
        targetDateEpochMillis = targetDateEpochMillis,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    )
}

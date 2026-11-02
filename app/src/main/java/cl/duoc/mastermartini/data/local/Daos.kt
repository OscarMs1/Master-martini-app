package cl.duoc.mastermartini.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>
}

@Dao
interface QuoteDao {
    @Transaction
    suspend fun replaceProduct(item: QuoteItemEntity) {
        removeProduct(item.userId, item.productId)
        upsertItem(item)
    }

    @Transaction
    suspend fun saveAndClear(quote: QuoteEntity): Long {
        val id = insertQuote(quote)
        clearItems(quote.userId)
        return id
    }

    @Query("SELECT * FROM quote_items WHERE userId = :userId ORDER BY productName")
    fun observeItems(userId: Long): Flow<List<QuoteItemEntity>>

    @Query("SELECT * FROM quote_items WHERE userId = :userId")
    suspend fun getItems(userId: Long): List<QuoteItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: QuoteItemEntity)

    @Query("DELETE FROM quote_items WHERE userId = :userId AND productId = :productId")
    suspend fun removeProduct(userId: Long, productId: String)

    @Query("DELETE FROM quote_items WHERE userId = :userId")
    suspend fun clearItems(userId: Long)

    @Insert
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Query("SELECT * FROM quotes WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeQuotes(userId: Long): Flow<List<QuoteEntity>>
}

@Dao
interface ReservationDao {
    @Insert
    suspend fun insert(reservation: ReservationEntity): Long

    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeAll(userId: Long): Flow<List<ReservationEntity>>

    @Query("SELECT COUNT(*) FROM reservations WHERE userId = :userId AND serviceId = :serviceId")
    suspend fun countFor(userId: Long, serviceId: String): Int

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun delete(id: Long)
}

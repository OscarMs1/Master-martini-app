package cl.duoc.mastermartini.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, QuoteItemEntity::class, QuoteEntity::class, ReservationEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun quoteDao(): QuoteDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "master_martini.db",
            )
                // Si cambia el esquema se recrea la BD. En la versión 2 las cuentas pasaron al
                // backend, así que las cuentas locales antiguas ya no sirven.
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}

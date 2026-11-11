package com.shanawaz.tvlauncher.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey val packageName: String,
    val isFavourite: Boolean = false,
    val favouriteOrder: Int = Int.MAX_VALUE,
    val customOrder: Int = Int.MAX_VALUE,
    val isHidden: Boolean = false,
    val lastLaunchedTimestamp: Long = 0L
)

@Dao
interface FavouriteAppDao {
    @Query("SELECT * FROM app_preferences")
    fun observeAllAppPreferences(): Flow<List<AppPreferenceEntity>>

    @Query("SELECT * FROM app_preferences WHERE packageName = :packageName LIMIT 1")
    suspend fun getPreferenceForPackage(packageName: String): AppPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreference(entity: AppPreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AppPreferenceEntity>)

    @Query("UPDATE app_preferences SET isFavourite = 0, favouriteOrder = :maxOrder")
    suspend fun clearAllFavourites(maxOrder: Int = Int.MAX_VALUE)

    @Query("UPDATE app_preferences SET isHidden = 0")
    suspend fun unhideAllApps()

    @Query("DELETE FROM app_preferences")
    suspend fun deleteAll()
}

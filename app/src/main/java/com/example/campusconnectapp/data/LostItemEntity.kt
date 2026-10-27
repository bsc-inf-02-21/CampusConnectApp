package com.example.campusconnectapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "lost_found_items")
data class LostItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val location: String,
    val date: Long,
    val type: ItemType,
    val contact: String,
    val imageUrl: String? = null,
    val isHidden: Boolean = false,
    val postedByUid: String = ""
) {
    fun toLostItem(): LostItem {
        return LostItem(
            id = id,
            title = title,
            category = category,
            description = description,
            location = location,
            date = date,
            type = type,
            contact = contact,
            imageUrl = imageUrl,
            isHidden = isHidden,
            postedByUid = postedByUid
        )
    }

    companion object {
        fun fromLostItem(item: LostItem): LostItemEntity {
            return LostItemEntity(
                id = item.id,
                title = item.title,
                category = item.category,
                description = item.description,
                location = item.location,
                date = item.date,
                type = item.type,
                contact = item.contact,
                imageUrl = item.imageUrl,
                isHidden = item.isHidden,
                postedByUid = item.postedByUid
            )
        }
    }
}

@Dao
interface LostFoundDao {
    @Query("SELECT * FROM lost_found_items ORDER BY date DESC")
    suspend fun getAllLostItems(): List<LostItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLostItems(items: List<LostItemEntity>)

    @Query("DELETE FROM lost_found_items WHERE id = :id")
    suspend fun deleteLostItemById(id: String)

    @Query("DELETE FROM lost_found_items")
    suspend fun clearAllLostItems()
}

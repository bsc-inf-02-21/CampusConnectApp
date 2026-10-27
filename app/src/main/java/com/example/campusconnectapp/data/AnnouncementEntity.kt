package com.example.campusconnectapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val date: Long,
    val authorName: String,
    val authorRole: UserRole,
    val authorId: String
) {
    fun toAnnouncement(): Announcement {
        return Announcement(
            id = id,
            title = title,
            content = content,
            date = date,
            authorName = authorName,
            authorRole = authorRole,
            authorId = authorId
        )
    }

    companion object {
        fun fromAnnouncement(item: Announcement): AnnouncementEntity {
            return AnnouncementEntity(
                id = item.id,
                title = item.title,
                content = item.content,
                date = item.date,
                authorName = item.authorName,
                authorRole = item.authorRole,
                authorId = item.authorId
            )
        }
    }
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY date DESC")
    suspend fun getAllAnnouncements(): List<AnnouncementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(items: List<AnnouncementEntity>)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncementById(id: String)

    @Query("DELETE FROM announcements")
    suspend fun clearAllAnnouncements()
}

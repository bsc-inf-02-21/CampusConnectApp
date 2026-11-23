package com.example.campusconnectapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnectapp.data.Announcement
import com.example.campusconnectapp.data.AnnouncementEntity
import com.example.campusconnectapp.data.AppDatabase
import com.example.campusconnectapp.data.UserProfile
import com.example.campusconnectapp.data.UserRole
import com.example.campusconnectapp.utils.AppPreferencesManager
import com.example.campusconnectapp.utils.NotificationUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AnnouncementViewModel(application: Application) : AndroidViewModel(application) {

    private val announcementDao = AppDatabase.getDatabase(application).announcementDao()
    private val db = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val announcementsCollection = db?.collection("announcements")
    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    private var snapshotListenerRegistration: ListenerRegistration? = null
    private var isInitialLoad = true

    private val authStateListener = FirebaseAuth.AuthStateListener {
        if (AppPreferencesManager.getInstance(getApplication()).isRealtimeSyncEnabled()) {
            listenToAnnouncementsRealtime()
        }
    }

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    init {
        // Load offline cached announcements from Room DB first
        loadOfflineCachedAnnouncements()
        // Listen to auth state changes to ensure instant sync when user signs in/out
        try { auth?.addAuthStateListener(authStateListener) } catch (e: Exception) {}
        // Listen to user setting preference for real-time background sync
        viewModelScope.launch {
            AppPreferencesManager.getInstance(getApplication()).realtimeSyncEnabled.collect { isEnabled ->
                if (isEnabled) {
                    ensureAuthAndListen()
                } else {
                    snapshotListenerRegistration?.remove()
                    snapshotListenerRegistration = null
                    loadOfflineCachedAnnouncements()
                }
            }
        }
    }

    private fun ensureAuthAndListen() {
        if (!AppPreferencesManager.getInstance(getApplication()).isRealtimeSyncEnabled()) {
            snapshotListenerRegistration?.remove()
            snapshotListenerRegistration = null
            loadOfflineCachedAnnouncements()
            return
        }
        if (auth == null) {
            loadOfflineCachedAnnouncements()
            return
        }
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    listenToAnnouncementsRealtime()
                }
                .addOnFailureListener {
                    listenToAnnouncementsRealtime()
                }
        } else {
            listenToAnnouncementsRealtime()
        }
    }

    private fun loadOfflineCachedAnnouncements() {
        viewModelScope.launch {
            try {
                val cachedEntities = announcementDao.getAllAnnouncements()
                val realEntities = cachedEntities.filter { entity ->
                    entity.id !in listOf("1", "2", "3")
                }
                if (realEntities.size != cachedEntities.size) {
                    announcementDao.clearAllAnnouncements()
                    if (realEntities.isNotEmpty()) {
                        announcementDao.insertAnnouncements(realEntities)
                    }
                }
                _announcements.value = realEntities.map { it.toAnnouncement() }.sortedByDescending { it.date }
            } catch (e: Exception) {
                Log.e("AnnouncementViewModel", "Error loading offline announcements", e)
            }
        }
    }

    private fun listenToAnnouncementsRealtime() {
        snapshotListenerRegistration?.remove()
        if (announcementsCollection == null) return
        snapshotListenerRegistration = announcementsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AnnouncementViewModel", "Error listening to Firestore announcements: ${error.localizedMessage}", error)
                    loadOfflineCachedAnnouncements()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val title = doc.getString("title") ?: ""
                            val content = doc.getString("content") ?: ""
                            val date = when (val rawDate = doc.get("date")) {
                                is Long -> rawDate
                                is Number -> rawDate.toLong()
                                is Timestamp -> rawDate.seconds * 1000
                                is String -> rawDate.toLongOrNull() ?: System.currentTimeMillis()
                                else -> System.currentTimeMillis()
                            }
                            val authorName = doc.getString("authorName") ?: ""
                            val authorRoleStr = doc.getString("authorRole") ?: UserRole.STUDENT.name
                            val authorRole = try {
                                UserRole.valueOf(authorRoleStr)
                            } catch (e: Exception) {
                                UserRole.STUDENT
                            }
                            val authorId = doc.getString("authorId") ?: ""

                            val isMock = id in listOf("1", "2", "3")

                            if (isMock) {
                                return@mapNotNull null
                            }

                            Announcement(
                                id = id,
                                title = title,
                                content = content,
                                date = date,
                                authorName = authorName,
                                authorRole = authorRole,
                                authorId = authorId
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.date }

                    if (!isInitialLoad && list.isNotEmpty()) {
                        val currentFirstId = _announcements.value.firstOrNull()?.id
                        val newFirst = list.firstOrNull()
                        if (newFirst != null && newFirst.id != currentFirstId) {
                            NotificationUtils.showAnnouncementNotification(
                                getApplication(),
                                newFirst.title,
                                newFirst.content
                            )
                        }
                    }
                    isInitialLoad = false

                    _announcements.value = list

                    // Cache real announcements to local Room database
                    viewModelScope.launch {
                        try {
                            announcementDao.clearAllAnnouncements()
                            if (list.isNotEmpty()) {
                                val entities = list.map { AnnouncementEntity.fromAnnouncement(it) }
                                announcementDao.insertAnnouncements(entities)
                            }
                        } catch (e: Exception) {
                            Log.e("AnnouncementViewModel", "Error caching announcements to Room DB", e)
                        }
                    }
                }
            }
    }

    fun canPost(role: UserRole?): Boolean {
        if (role == null) return true
        return role == UserRole.LECTURER || role == UserRole.ADMINISTRATOR
    }

    fun canDelete(announcement: Announcement, role: UserRole?): Boolean {
        if (role == null) return false
        return when (role) {
            UserRole.STUDENT -> false
            UserRole.LECTURER -> announcement.authorRole != UserRole.ADMINISTRATOR
            UserRole.ADMINISTRATOR -> true
        }
    }

    fun addAnnouncement(title: String, content: String, userProfile: UserProfile): Boolean {
        if (!canPost(userProfile.role)) {
            return false
        }

        val newId = UUID.randomUUID().toString()
        val newAnnouncement = Announcement(
            id = newId,
            title = title,
            content = content,
            date = System.currentTimeMillis(),
            authorName = userProfile.fullName,
            authorRole = userProfile.role,
            authorId = userProfile.uid
        )

        // Instant local update for immediate UI responsiveness
        _announcements.value = listOf(newAnnouncement) + _announcements.value

        viewModelScope.launch {
            try {
                announcementDao.insertAnnouncements(listOf(AnnouncementEntity.fromAnnouncement(newAnnouncement)))
            } catch (e: Exception) {}
        }

        val data = hashMapOf(
            "title" to newAnnouncement.title,
            "content" to newAnnouncement.content,
            "date" to newAnnouncement.date,
            "authorName" to newAnnouncement.authorName,
            "authorRole" to newAnnouncement.authorRole.name,
            "authorId" to newAnnouncement.authorId
        )

        fun uploadToFirestore() {
            if (announcementsCollection == null) return
            announcementsCollection.document(newId).set(data)
                .addOnSuccessListener {
                    Log.d("AnnouncementViewModel", "Successfully posted announcement to Firestore: $newId")
                }
                .addOnFailureListener { e ->
                    Log.e("AnnouncementViewModel", "Failed to upload announcement: ${e.localizedMessage}")
                }
        }

        if (auth == null || auth.currentUser == null) {
            try {
                auth?.signInAnonymously()?.addOnCompleteListener {
                    uploadToFirestore()
                } ?: uploadToFirestore()
            } catch (e: Exception) {
                uploadToFirestore()
            }
        } else {
            uploadToFirestore()
        }

        return true
    }

    fun deleteAnnouncement(announcement: Announcement, userProfile: UserProfile): Boolean {
        if (!canDelete(announcement, userProfile.role)) {
            return false
        }

        announcementsCollection?.document(announcement.id)?.delete()

        viewModelScope.launch {
            try {
                announcementDao.deleteAnnouncementById(announcement.id)
                _announcements.value = _announcements.value.filter { it.id != announcement.id }
            } catch (e: Exception) {
                Log.e("AnnouncementViewModel", "Error deleting announcement from local DB", e)
            }
        }

        return true
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListenerRegistration?.remove()
        try { auth?.removeAuthStateListener(authStateListener) } catch (e: Exception) {}
    }
}

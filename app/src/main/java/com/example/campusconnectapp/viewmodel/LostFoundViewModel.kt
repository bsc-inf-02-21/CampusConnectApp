package com.example.campusconnectapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnectapp.data.AppDatabase
import com.example.campusconnectapp.data.ItemType
import com.example.campusconnectapp.data.LostItem
import com.example.campusconnectapp.data.LostItemEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class LostFoundViewModel(application: Application) : AndroidViewModel(application) {

    private val lostFoundDao = AppDatabase.getDatabase(application).lostFoundDao()
    private val db = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val collection = db?.collection("lost_found_items")
    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    private var snapshotListenerRegistration: ListenerRegistration? = null

    private val _itemsList = MutableStateFlow<List<LostItem>>(emptyList())
    val itemsList: StateFlow<List<LostItem>> = _itemsList.asStateFlow()

    init {
        // Load offline cached items from Room DB first
        loadOfflineCachedItems()
        // Ensure active Firebase session, then listen to Firestore real-time
        ensureAuthAndListen()
    }

    private fun ensureAuthAndListen() {
        if (auth == null) {
            loadOfflineCachedItems()
            return
        }
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener {
                listenToLostFoundItemsRealtime()
            }
        } else {
            listenToLostFoundItemsRealtime()
        }
    }

    private fun loadOfflineCachedItems() {
        viewModelScope.launch {
            try {
                val cachedEntities = lostFoundDao.getAllLostItems()
                val realEntities = cachedEntities.filter { entity ->
                    entity.id !in listOf("1", "2", "3") && !entity.isHidden
                }
                if (realEntities.size != cachedEntities.size) {
                    lostFoundDao.clearAllLostItems()
                    if (realEntities.isNotEmpty()) {
                        lostFoundDao.insertLostItems(realEntities)
                    }
                }
                _itemsList.value = realEntities.map { it.toLostItem() }
            } catch (e: Exception) {
                Log.e("LostFoundViewModel", "Error loading offline lost & found items", e)
            }
        }
    }

    private fun listenToLostFoundItemsRealtime() {
        snapshotListenerRegistration?.remove()
        if (collection == null) return
        snapshotListenerRegistration = collection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("LostFoundViewModel", "Error listening to Firestore lost/found items: ${error.localizedMessage}", error)
                    if (auth == null || auth.currentUser == null) {
                        try {
                            auth?.signInAnonymously()?.addOnCompleteListener {
                                listenToLostFoundItemsRealtime()
                            } ?: loadOfflineCachedItems()
                        } catch (e: Exception) {
                            loadOfflineCachedItems()
                        }
                    } else {
                        loadOfflineCachedItems()
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val title = doc.getString("title") ?: ""
                            val category = doc.getString("category") ?: "General"
                            val description = doc.getString("description") ?: ""
                            val location = doc.getString("location") ?: ""
                            val date = doc.getLong("date") ?: System.currentTimeMillis()
                            val typeStr = doc.getString("type") ?: ItemType.LOST.name
                            val type = try { ItemType.valueOf(typeStr) } catch (e: Exception) { ItemType.LOST }
                            val contact = doc.getString("contact") ?: ""
                            val imageUrl = doc.getString("imageUrl")
                            val isHidden = doc.getBoolean("isHidden") ?: false
                            val postedByUid = doc.getString("postedByUid") ?: ""

                            val isMock = id in listOf("1", "2", "3")

                            if (isMock || isHidden) {
                                return@mapNotNull null
                            }

                            LostItem(id, title, category, description, location, date, type, contact, imageUrl, isHidden, postedByUid)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    _itemsList.value = list

                    // Cache real items to local Room database
                    viewModelScope.launch {
                        try {
                            lostFoundDao.clearAllLostItems()
                            if (list.isNotEmpty()) {
                                val entities = list.map { LostItemEntity.fromLostItem(it) }
                                lostFoundDao.insertLostItems(entities)
                            }
                        } catch (e: Exception) {
                            Log.e("LostFoundViewModel", "Error caching lost & found items to Room DB", e)
                        }
                    }
                }
            }
    }

    fun addItem(
        title: String,
        category: String,
        description: String,
        location: String,
        contact: String,
        type: ItemType,
        imageUrl: String? = null,
        postedByUid: String = ""
    ) {
        val newId = UUID.randomUUID().toString()
        val newItem = LostItem(
            id = newId,
            title = title,
            category = category,
            description = description,
            location = location,
            date = System.currentTimeMillis(),
            type = type,
            contact = contact,
            imageUrl = imageUrl,
            isHidden = false,
            postedByUid = postedByUid
        )

        // Instant local update for immediate UI feedback
        _itemsList.value = listOf(newItem) + _itemsList.value

        viewModelScope.launch {
            try {
                lostFoundDao.insertLostItems(listOf(LostItemEntity.fromLostItem(newItem)))
            } catch (e: Exception) {
                Log.e("LostFoundViewModel", "Error saving item locally", e)
            }
        }

        val data = hashMapOf(
            "title" to newItem.title,
            "category" to newItem.category,
            "description" to newItem.description,
            "location" to newItem.location,
            "date" to newItem.date,
            "type" to newItem.type.name,
            "contact" to newItem.contact,
            "imageUrl" to newItem.imageUrl,
            "isHidden" to false,
            "postedByUid" to newItem.postedByUid
        )

        fun uploadToFirestore() {
            if (collection == null) return
            collection.document(newId).set(data)
                .addOnSuccessListener {
                    Log.d("LostFoundViewModel", "Successfully posted lost item to Firestore: $newId")
                }
                .addOnFailureListener { err ->
                    Log.w("LostFoundViewModel", "Failed to upload lost item to Firestore: ${err.localizedMessage}")
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
    }

    /**
     * Hides / Archives an item (puts it in trash/hidden state) without permanently deleting it.
     * Only the user who posted the item has permission to perform this action.
     */
    fun hideItem(item: LostItem, currentUserId: String) {
        if (currentUserId.isBlank() || currentUserId != item.postedByUid) {
            Log.w("LostFoundViewModel", "Unauthorized attempt to hide item by user: $currentUserId")
            return
        }

        val updatedItem = item.copy(isHidden = true)
        val data = hashMapOf(
            "title" to updatedItem.title,
            "category" to updatedItem.category,
            "description" to updatedItem.description,
            "location" to updatedItem.location,
            "date" to updatedItem.date,
            "type" to updatedItem.type.name,
            "contact" to updatedItem.contact,
            "imageUrl" to updatedItem.imageUrl,
            "isHidden" to true,
            "postedByUid" to updatedItem.postedByUid
        )

        collection?.document(item.id)?.set(data)

        viewModelScope.launch {
            try {
                lostFoundDao.insertLostItems(listOf(LostItemEntity.fromLostItem(updatedItem)))
                _itemsList.value = _itemsList.value.filter { it.id != item.id }
            } catch (e: Exception) {
                Log.e("LostFoundViewModel", "Error hiding item in local DB", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListenerRegistration?.remove()
    }
}

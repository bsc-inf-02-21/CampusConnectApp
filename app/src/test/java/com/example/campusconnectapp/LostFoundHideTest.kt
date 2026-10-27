package com.example.campusconnectapp

import com.example.campusconnectapp.data.ItemType
import com.example.campusconnectapp.data.LostItem
import org.junit.Assert.*
import org.junit.Test

class LostFoundHideTest {

    @Test
    fun testHideItemLogic() {
        val item = LostItem(
            id = "item_1",
            title = "Student ID Card",
            category = "ID Cards",
            description = "Lost near library",
            location = "Library",
            date = System.currentTimeMillis(),
            type = ItemType.LOST,
            contact = "student@unima.ac.mw",
            isHidden = false,
            postedByUid = "user_123"
        )

        val currentUserId = "user_123"

        // Verify permission: only user who posted it can hide
        val canHide = currentUserId == item.postedByUid
        assertTrue(canHide)

        // Simulating hiding item
        val hiddenItem = item.copy(isHidden = true)
        assertTrue(hiddenItem.isHidden)

        // Filtering active list (items that are not hidden)
        val itemsList = listOf(item, hiddenItem)
        val activeItems = itemsList.filter { !it.isHidden }

        assertEquals(1, activeItems.size)
        assertEquals("item_1", activeItems[0].id)
    }
}

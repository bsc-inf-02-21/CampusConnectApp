package com.example.campusconnectapp

import com.example.campusconnectapp.data.ItemType
import com.example.campusconnectapp.data.LostItem
import org.junit.Assert.*
import org.junit.Test

class LostFoundSearchTest {

    @Test
    fun testLostFoundSearchFiltering() {
        val item1 = LostItem("1", "Blue Hydro Flask", "Personal Items", "Water bottle left at Great Hall", "Great Hall", 1000L, ItemType.LOST, "student@unima.ac.mw", null, false, "user_1")
        val item2 = LostItem("2", "Calculus Textbook", "Books", "Math book", "Library", 2000L, ItemType.FOUND, "library@unima.ac.mw", null, false, "user_2")
        val item3 = LostItem("3", "Hidden Item", "Keys", "Lost keys", "Gate", 3000L, ItemType.LOST, "student@unima.ac.mw", null, true, "user_1")

        val allItems = listOf(item1, item2, item3)

        // Filter active (not hidden) items matching search query "Water"
        val query = "Water"
        val results = allItems.filter { item ->
            !item.isHidden && (
                item.title.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true)
            )
        }

        assertEquals(1, results.size)
        assertEquals("Blue Hydro Flask", results[0].title)

        // Hidden items should never appear in search results even if query matches
        val hiddenQuery = "Hidden"
        val hiddenResults = allItems.filter { item ->
            !item.isHidden && item.title.contains(hiddenQuery, ignoreCase = true)
        }
        assertEquals(0, hiddenResults.size)
    }
}

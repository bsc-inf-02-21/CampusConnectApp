package com.example.campusconnectapp

import com.example.campusconnectapp.data.CampusInfo
import com.example.campusconnectapp.data.InfoType
import org.junit.Assert.*
import org.junit.Test

class CampusInfoTest {

    @Test
    fun testCampusInfoContactValidation() {
        val infoItem = CampusInfo(
            id = "1",
            title = "Campus Security Emergency",
            detail = "24/7 Hotline",
            type = InfoType.EMERGENCY,
            phone = "+265991894726",
            email = "security@unima.ac.mw"
        )

        assertNotNull(infoItem.phone)
        assertTrue(infoItem.phone!!.startsWith("+265"))
        assertTrue(infoItem.email!!.endsWith("@unima.ac.mw"))
        assertEquals(InfoType.EMERGENCY, infoItem.type)
    }
}

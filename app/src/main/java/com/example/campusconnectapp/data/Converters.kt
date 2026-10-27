package com.example.campusconnectapp.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }

    @TypeConverter
    fun fromItemType(type: ItemType): String {
        return type.name
    }

    @TypeConverter
    fun toItemType(value: String): ItemType {
        return ItemType.valueOf(value)
    }
}

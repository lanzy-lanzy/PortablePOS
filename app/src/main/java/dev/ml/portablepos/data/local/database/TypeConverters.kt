package dev.ml.portablepos.data.local.database

import androidx.room.TypeConverter

class TypeConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Long? = value

    @TypeConverter
    fun toTimestamp(value: Long?): Long? = value
}

package com.example.pawfectplanner.data.local.converters

import androidx.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object LocalDateConverter {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? =
        date?.format(dateFormatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, dateFormatter) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? =
        dateTime?.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
}

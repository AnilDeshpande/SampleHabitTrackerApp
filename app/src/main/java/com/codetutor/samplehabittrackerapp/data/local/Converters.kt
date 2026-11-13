package com.codetutor.samplehabittrackerapp.data.local

import androidx.room.TypeConverter
import com.codetutor.samplehabittrackerapp.domain.model.parseStorageText
import com.codetutor.samplehabittrackerapp.domain.model.toStorageText
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Production converters, shared by every database builder. Dates are ISO-8601 text. Decimals are
 * normalized plain text, and a database value that is not already normalized is rejected rather
 * than silently reinterpreted. Enums use Room's built-in by-name mapping.
 */
class Converters {
    @TypeConverter
    fun localDateToText(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun textToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun decimalToText(value: BigDecimal?): String? = value?.toStorageText()

    @TypeConverter
    fun textToDecimal(value: String?): BigDecimal? = value?.let(::parseStorageText)
}

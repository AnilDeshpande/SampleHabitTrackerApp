package com.codetutor.samplehabittrackerapp.data.local

import androidx.room.TypeConverter
import com.codetutor.samplehabittrackerapp.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

class StorageConverters {
    @TypeConverter fun decimalToText(value: BigDecimal): String = value.stripTrailingZeros().toPlainString()
    @TypeConverter fun textToDecimal(value: String): BigDecimal {
        val decimal = BigDecimal(value)
        require(decimalToText(decimal) == value) { "Non-canonical decimal in database: $value" }
        return decimal
    }
    @TypeConverter fun dateToText(value: LocalDate): String = value.toString()
    @TypeConverter fun textToDate(value: String): LocalDate = LocalDate.parse(value)
    @TypeConverter fun frequencyToText(value: Frequency): String = value.name
    @TypeConverter fun textToFrequency(value: String): Frequency = Frequency.valueOf(value)
    @TypeConverter fun habitStatusToText(value: HabitStatus): String = value.name
    @TypeConverter fun textToHabitStatus(value: String): HabitStatus = HabitStatus.valueOf(value)
    @TypeConverter fun occurrenceStatusToText(value: OccurrenceStatus): String = value.name
    @TypeConverter fun textToOccurrenceStatus(value: String): OccurrenceStatus = OccurrenceStatus.valueOf(value)
    @TypeConverter fun occurrenceResultToText(value: OccurrenceResult): String = value.name
    @TypeConverter fun textToOccurrenceResult(value: String): OccurrenceResult = OccurrenceResult.valueOf(value)
    @TypeConverter fun deliveryStateToText(value: DeliveryState): String = value.name
    @TypeConverter fun textToDeliveryState(value: String): DeliveryState = DeliveryState.valueOf(value)
    @TypeConverter fun badgeTypeToText(value: BadgeType): String = value.name
    @TypeConverter fun textToBadgeType(value: String): BadgeType = BadgeType.valueOf(value)
}

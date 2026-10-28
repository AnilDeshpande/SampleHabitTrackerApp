package com.codetutor.samplehabittrackerapp.domain.model

enum class Frequency { DAILY, WEEKLY, FORTNIGHTLY, MONTHLY, CUSTOM }
enum class HabitStatus { ACTIVE, PENDING_RETIREMENT, RETIRED }
enum class OccurrenceStatus { OPEN, FINALIZED, CANCELLED }
enum class OccurrenceResult { SUCCESS, MISS, CANCELLED }
enum class DeliveryState { PENDING, POSTED, RETRYABLE, UNAVAILABLE, SUPPRESSED }
enum class BadgeType { CONSISTENCY_21_DAY }

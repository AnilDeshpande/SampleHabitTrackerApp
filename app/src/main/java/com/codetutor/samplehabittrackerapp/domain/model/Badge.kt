package com.codetutor.samplehabittrackerapp.domain.model

/** V1 has exactly one badge. Stored by enum name. */
enum class BadgeType { CONSISTENCY_21 }

/** Delivery state of a missed-result notification event (the transactional outbox). */
enum class NotificationDeliveryState { PENDING, POSTED, RETRYABLE, UNAVAILABLE, SUPPRESSED }

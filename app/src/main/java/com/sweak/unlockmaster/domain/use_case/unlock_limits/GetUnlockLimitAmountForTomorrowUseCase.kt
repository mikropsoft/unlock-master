package com.sweak.unlockmaster.domain.use_case.unlock_limits

import com.sweak.unlockmaster.domain.DEFAULT_UNLOCK_LIMIT
import com.sweak.unlockmaster.domain.repository.TimeRepository
import com.sweak.unlockmaster.domain.repository.UnlockLimitsRepository
import javax.inject.Inject

class GetUnlockLimitAmountForTomorrowUseCase @Inject constructor(
    private val unlockLimitsRepository: UnlockLimitsRepository,
    private val timeRepository: TimeRepository
) {
    suspend operator fun invoke(): Int {
        return unlockLimitsRepository.getUnlockLimitWithApplianceDay(
            limitApplianceDayTimeInMillis = timeRepository.getTomorrowBeginningTimeInMillis()
        )?.limitAmount ?: DEFAULT_UNLOCK_LIMIT
    }
}
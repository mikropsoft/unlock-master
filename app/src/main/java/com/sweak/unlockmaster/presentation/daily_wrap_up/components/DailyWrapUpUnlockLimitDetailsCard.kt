package com.sweak.unlockmaster.presentation.daily_wrap_up.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.sweak.unlockmaster.R
import com.sweak.unlockmaster.domain.UNLOCK_LIMIT_LOWER_BOUND
import com.sweak.unlockmaster.presentation.common.theme.space

@Composable
fun DailyWrapUpUnlockLimitDetailsCard(
    detailsData: DailyWrapUpUnlockLimitDetailsData,
    onInteraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = MaterialTheme.space.xSmall
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(all = MaterialTheme.space.smallMedium)) {
            Text(
                text = stringResource(R.string.unlock_limit),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.space.small)
            )

            Row {
                Text(
                    text = detailsData.unlockLimit.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.xSmall)
                        .alignByBaseline()
                )

                Text(
                    text = stringResource(R.string.was_your_unlock_limit),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.alignByBaseline()
                )
            }

            detailsData.suggestedUnlockLimit?.let { suggestedUnlockLimit ->
                Text(
                    text = stringResource(R.string.recommended_to_update),
                    style = MaterialTheme.typography.headlineMedium
                )

                Row(modifier = Modifier.padding(bottom = MaterialTheme.space.medium)) {
                    Text(
                        text = suggestedUnlockLimit.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(end = MaterialTheme.space.xSmall)
                            .alignByBaseline()
                    )

                    Text(
                        text = stringResource(R.string.for_more_improvements),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(all = MaterialTheme.space.smallMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TipsAndUpdates,
                            contentDescription = stringResource(R.string.content_description_tips_icon),
                            modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                        )

                        Text(
                            text = stringResource(R.string.apply_suggested_unlock_limit),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = MaterialTheme.space.smallMedium)
                        )

                        AnimatedContent(
                            targetState = detailsData.isSuggestedUnlockLimitApplied,
                            label = "applySuggestedUnlockLimitAnimation"
                        ) {
                            if (it) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .height(MaterialTheme.space.run { large + small })
                                ) {
                                    Text(
                                        text = stringResource(R.string.done),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(end = MaterialTheme.space.small)
                                    )

                                    Icon(
                                        imageVector = Icons.Outlined.Done,
                                        contentDescription = stringResource(
                                            R.string.content_description_done_icon
                                        )
                                    )
                                }
                            } else {
                                Button(
                                    onClick = onInteraction,
                                    modifier = Modifier
                                        .height(MaterialTheme.space.run { large + small })
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(R.string.update),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(end = MaterialTheme.space.small)
                                        )

                                        Icon(
                                            imageVector = Icons.Outlined.NavigateNext,
                                            contentDescription = stringResource(
                                                R.string.content_description_next_icon
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: if (detailsData.unlockLimit != detailsData.tomorrowUnlockLimit) {
                Text(
                    text = stringResource(R.string.you_have_recently_set),
                    style = MaterialTheme.typography.headlineMedium
                )

                Row {
                    Text(
                        text = detailsData.tomorrowUnlockLimit.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 32.sp,
                        modifier = Modifier
                            .padding(end = MaterialTheme.space.xSmall)
                            .alignByBaseline()
                    )

                    Text(
                        text = stringResource(R.string.as_your_new_unlock_limit),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            } else if (detailsData.isLimitSignificantlyExceeded) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.padding(top = MaterialTheme.space.medium)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(all = MaterialTheme.space.smallMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = stringResource(
                                R.string.content_description_warning_icon
                            ),
                            modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = MaterialTheme.space.smallMedium)
                        ) {
                            Text(
                                text = stringResource(R.string.limit_exceeded_significantly),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                            )

                            Text(
                                text = stringResource(R.string.consider_increasing_limit_or_pausing),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.padding(top = MaterialTheme.space.medium)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(all = MaterialTheme.space.smallMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TipsAndUpdates,
                            contentDescription = stringResource(R.string.content_description_tips_icon),
                            modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                        )

                        Text(
                            text = stringResource(
                                if (detailsData.unlockLimit != UNLOCK_LIMIT_LOWER_BOUND)
                                    R.string.keep_improving_for_limit_recommendation
                                else R.string.you_have_reached_the_lowest_unlock_limit
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = MaterialTheme.space.smallMedium)
                        )
                    }
                }
            }
        }
    }
}

data class DailyWrapUpUnlockLimitDetailsData(
    val unlockLimit: Int,
    val tomorrowUnlockLimit: Int,
    val suggestedUnlockLimit: Int?,
    val isSuggestedUnlockLimitApplied: Boolean,
    val isLimitSignificantlyExceeded: Boolean
)
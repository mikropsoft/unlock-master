package com.sweak.unlockmaster.presentation.daily_wrap_up.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.sweak.unlockmaster.R
import com.sweak.unlockmaster.presentation.common.theme.space
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyWrapUpScreenOnEventsDetailsCard(
    detailsData: DailyWrapUpScreenOnEventsDetailsData,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = MaterialTheme.space.small)
            ) {
                Text(
                    text = stringResource(R.string.screen_on_events),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(end = MaterialTheme.space.xSmall)
                )

                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentEnforcement provides false
                ) {
                    IconButton(
                        onClick = onInteraction,
                        modifier = Modifier
                            .padding(start = MaterialTheme.space.small)
                            .size(size = MaterialTheme.space.smallMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = stringResource(
                                R.string.content_description_help_icon
                            ),
                            modifier = Modifier.size(size = MaterialTheme.space.smallMedium)
                        )
                    }
                }
            }

            Row {
                Text(
                    text = detailsData.screenOnEventsCount.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.xSmall)
                        .alignByBaseline()
                )

                Text(
                    text = stringResource(
                        if (detailsData.yesterdayDifference != null)
                            R.string.screen_turn_ons_which_is
                        else R.string.screen_turn_ons_today
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.alignByBaseline()
                )
            }

            detailsData.yesterdayDifference?.let {
                Row {
                    Text(
                        text = if (it == 0) "—" else abs(it).toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 32.sp,
                        color = if (it < 0) MaterialTheme.colorScheme.secondary
                        else if (it > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(end = MaterialTheme.space.xSmall)
                            .alignByBaseline()
                    )

                    Text(
                        text = stringResource(
                            if (detailsData.weekBeforeDifference == null) {
                                if (it < 0) R.string.less_than_yesterday
                                else if (it > 0) R.string.more_than_yesterday
                                else R.string.as_much_as_yesterday
                            } else {
                                if (it < 0) R.string.less_than_yesterday_and
                                else if (it > 0) R.string.more_than_yesterday_and
                                else R.string.as_much_as_yesterday_and
                            }
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }

            detailsData.weekBeforeDifference?.let {
                Row {
                    Text(
                        text = if (it == 0) "—" else abs(it).toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 32.sp,
                        color = if (it < 0) MaterialTheme.colorScheme.secondary
                        else if (it > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(end = MaterialTheme.space.xSmall)
                            .alignByBaseline()
                    )

                    Text(
                        text = stringResource(
                            if (it < 0) R.string.less_than_a_week_before
                            else if (it > 0) R.string.more_than_a_week_before
                            else R.string.as_much_as_a_week_before
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }

            if (detailsData.isManyMoreScreenOnEventsThanUnlocks) {
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
                                text = stringResource(R.string.many_more_screen_ons_than_unlocks),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                            )

                            Text(
                                text = stringResource(R.string.consider_limiting_auto_turn_on),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DailyWrapUpScreenOnEventsDetailsData(
    val screenOnEventsCount: Int,
    val yesterdayDifference: Int?,
    val weekBeforeDifference: Int?,
    val isManyMoreScreenOnEventsThanUnlocks: Boolean
)
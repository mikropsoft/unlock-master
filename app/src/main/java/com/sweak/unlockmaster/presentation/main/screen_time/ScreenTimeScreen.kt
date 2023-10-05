package com.sweak.unlockmaster.presentation.main.screen_time

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sweak.unlockmaster.R
import com.sweak.unlockmaster.presentation.common.components.NavigationBar
import com.sweak.unlockmaster.presentation.common.components.OnResume
import com.sweak.unlockmaster.presentation.common.ui.theme.space
import com.sweak.unlockmaster.presentation.common.util.TimeFormat
import com.sweak.unlockmaster.presentation.common.util.getCompactDurationString
import com.sweak.unlockmaster.presentation.common.util.getFullDateString
import com.sweak.unlockmaster.presentation.common.util.popBackStackThrottled
import com.sweak.unlockmaster.presentation.main.screen_time.components.CounterPauseSeparator
import com.sweak.unlockmaster.presentation.main.screen_time.components.DailyScreenTimeChart
import com.sweak.unlockmaster.presentation.main.screen_time.components.SingleScreenTimeSessionCard

@Composable
fun ScreenTimeScreen(
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(),
    navController: NavController,
    displayedScreenTimeDayTimeInMillis: Long
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    OnResume {
        screenTimeViewModel.refresh()
    }

    val screenTimeScreenState = screenTimeViewModel.state

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        NavigationBar(
            title = getFullDateString(displayedScreenTimeDayTimeInMillis),
            onBackClick = { navController.popBackStackThrottled(lifecycleOwner) }
        )

        AnimatedContent(
            targetState = screenTimeScreenState.isInitializing,
            contentAlignment = Alignment.Center,
            label = "screenTimeScreenContentLoadingAnimation",
            modifier = Modifier.fillMaxWidth()
        ) { isInitializing ->
            if (!isInitializing) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    DailyScreenTimeChart(
                        screenTimeMinutesPerHourEntries =
                        screenTimeScreenState.screenTimeMinutesPerHourEntries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MaterialTheme.space.xxxLarge)
                            .padding(bottom = MaterialTheme.space.mediumLarge)
                    )

                    Card(
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = MaterialTheme.space.xSmall
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.large
                            )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = MaterialTheme.space.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.screen_time_colon),
                                style = MaterialTheme.typography.headlineMedium
                            )

                            screenTimeScreenState.todayScreenTimeDuration?.let {
                                Text(
                                    text = getCompactDurationString(it),
                                    style = MaterialTheme.typography.displayLarge
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.screen_time_history),
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.medium
                        )
                    )

                    if (screenTimeScreenState.UIReadySessionEvents.isEmpty()) {
                        Card(
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = MaterialTheme.space.xSmall
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = MaterialTheme.space.medium,
                                    end = MaterialTheme.space.medium,
                                    bottom = MaterialTheme.space.large,
                                )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = MaterialTheme.space.medium)
                            ) {
                                Text(
                                    text = stringResource(R.string.history_empty_for_that_day),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = MaterialTheme.space.medium,
                                    end = MaterialTheme.space.medium,
                                    bottom = MaterialTheme.space.medium,
                                )
                        ) {
                            val timeFormat =
                                if (DateFormat.is24HourFormat(LocalContext.current)) TimeFormat.MILITARY
                                else TimeFormat.AMPM

                            screenTimeScreenState.UIReadySessionEvents.forEach {
                                if (it is ScreenTimeScreenState.UIReadySessionEvent.ScreenTime) {
                                    SingleScreenTimeSessionCard(
                                        screenSessionStartAndEndTimesInMillis =
                                        it.startAndEndTimesInMillis,
                                        screenSessionDuration = it.screenSessionDuration,
                                        timeFormat = timeFormat,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = MaterialTheme.space.medium)
                                    )
                                } else if (it is ScreenTimeScreenState.UIReadySessionEvent.CounterPaused) {
                                    CounterPauseSeparator(
                                        counterPauseSessionStartAndEndTimesInMillis =
                                        it.startAndEndTimesInMillis,
                                        timeFormat = timeFormat,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = MaterialTheme.space.medium)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxHeight()) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(MaterialTheme.space.xLarge)
                            .align(alignment = Alignment.Center)
                    )
                }
            }
        }
    }
}
package com.simpdev.sahmride.Presentation.StreamChat

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simpdev.sahmride.Presentation.Trip.TripEvents
import com.simpdev.sahmride.Presentation.Trip.TripUserEvents
import com.simpdev.sahmride.Presentation.Trip.TripUserViewModel
import com.simpdev.sahmride.Presentation.Trip.TripViewModel
import com.simpdev.sahmride.R
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Composable
fun StreamChat(tirpuserViewModel: TripUserViewModel? = null, tripDriverViewModel: TripViewModel? = null) {
    val viewModel = viewModel<StreamChatViewModel>()
    val state = viewModel.state
    ChatTheme {
        when(state.currentScreen){
            is CurrentScreen.ChannelScreen -> {
                ChannelsScreen(
                    title = stringResource(id = R.string.app_name),
                    onItemClick = {
                                  viewModel.onEvent(StreamChatEvents.changeChannelId(it.cid))
                    },
                    onHeaderActionClick = {
                        // Header header action clicks
                    },
                    onHeaderAvatarClick = {
                        // Handle header avatar clicks
                    },
                    onBackPressed = {
                        if(tirpuserViewModel != null)
                        {
                            tirpuserViewModel.onEvent(TripUserEvents.tripHomeClicked)
                        }
                        if(tripDriverViewModel != null)
                        {
                            tripDriverViewModel.onEvent(TripEvents.tripHomeClicked)
                        }
                    }
                )
            }
            is CurrentScreen.MessageScreen -> {
                MessagesScreen(
                    channelId = state.channelId.toString(),
                    onBackPressed = {
                        viewModel.onEvent(StreamChatEvents.changeCurrentScreen(CurrentScreen.ChannelScreen))
                    }
                )
            }
        }
    }
}

@Composable
fun ChatMessages(channelId: String){
    ChatTheme {

    }
}
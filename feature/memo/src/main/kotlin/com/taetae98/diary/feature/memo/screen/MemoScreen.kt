package com.taetae98.diary.feature.memo.screen

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.taetae98.diary.feature.common.Const
import com.taetae98.diary.feature.compose.diary.DiaryTopAppBar
import com.taetae98.diary.feature.compose.modifier.draggable
import com.taetae98.diary.feature.memo.compose.MemoPreviewCompose
import com.taetae98.diary.feature.memo.event.MemoEvent
import com.taetae98.diary.feature.memo.viewmodel.MemoViewModel
import com.taetae98.diary.feature.resource.StringResource
import com.taetae98.diary.feature.theme.DiaryTheme
import kotlin.math.abs
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object MemoScreen {
    const val ROUTE = "${Const.DEEP_LINK_PREFIX}/memo-screen"
}

@Composable
fun MemoScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = { MemoTopAppBar(navController = navController) }
    ) {
        MemoLazyColumn(
            modifier = Modifier.padding(it)
        )
    }

    CollectEvent(snackbarHostState = scaffoldState.snackbarHostState)
}

@Composable
private fun CollectEvent(
    snackbarHostState: SnackbarHostState,
    memoViewModel: MemoViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        memoViewModel.event.collect {
            when(it) {
                is MemoEvent.DeleteMemo -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    launch {
                        snackbarHostState.showSnackbar(
                            message = it.relation.memoEntity.title,
                            actionLabel = context.getString(StringResource.restore),
                            duration = SnackbarDuration.Long
                        ).also { result ->
                            if (result == SnackbarResult.ActionPerformed) {
                                it.onRestore()
                            }
                        }
                    }
                }
                is MemoEvent.Error -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = "Error : ${it.throwable.message}"
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoTopAppBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    DiaryTopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(id = StringResource.memo)) },
        actions = {
            IconButton(
                onClick = { navController.navigate(MemoEditScreen.getAction()) }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(id = StringResource.add)
                )
            }
        }
    )
}

@Composable
private fun MemoLazyColumn(
    modifier: Modifier = Modifier,
    memoViewModel: MemoViewModel = hiltViewModel()
) {
    val items = memoViewModel.getMemoByTagIds(emptyList()).collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = items,
            key = { it.id }
        ) {
            MemoPreviewCompose(
                modifier = modifier
                    .fillParentMaxWidth()
                    .draggable(
                        orientation = Orientation.Horizontal,
                        onDragStopped = { velocity ->
                            if (abs(velocity) >= Const.VELOCITY_BOUNDARY) {
                                it?.let { it.onDelete() }
                                true
                            } else {
                                false
                            }
                        }
                    ),
                uiState = it
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    DiaryTheme {
        MemoScreen(
            navController = rememberNavController()
        )
    }
}
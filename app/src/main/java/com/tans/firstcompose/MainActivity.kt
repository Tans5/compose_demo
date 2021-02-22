package com.tans.firstcompose

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.tans.firstcompose.core.Stateable
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await

val defaultList = (0 until 50).map {
    "$it Hello, World"
}.toList()


data class MainState(
    val clickTimes: Int = 0,
    val list: List<String> = defaultList
)

class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main), Stateable<MainState> by Stateable(
    MainState()
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContent {
            AppContent()
        }
    }

    @Composable
    fun AppContent() {
        val list = bindState().map { it.list }.distinctUntilChanged().subscribeAsState(emptyList())
        val clickTimes = bindState().map { it.clickTimes }.distinctUntilChanged().subscribeAsState(0)
        Scaffold(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    launch {
                        updateState {
                            it.copy(clickTimes = it.clickTimes + 1)
                        }.await()
                    }
                }) {
                    Image(
                        imageVector = Icons.Default.Add,
                        contentDescription = null)
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(R.string.app_name), color = Color.White)
                            println("Update Click")
                            Text(text = "Click Times: ${clickTimes.value}", color = Color.White, style = TextStyle(fontSize = TextUnit(10)))
                        }
                    }
                )
            }
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxHeight().fillMaxWidth()
            ) {
                val listRef = createRef()
                LazyColumn(
                    modifier = Modifier.constrainAs(listRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    }
                ) {
                    val listValue = list.value
                    println("Update List")
                    items(listValue.size) { i ->
                        ListItem(i, listValue[i])
                    }
                }
            }
        }
    }

    @Composable
    fun LazyItemScope.ListItem(index: Int, string: String) {
        Column(modifier = Modifier.clickable(
            interactionState = InteractionState(),
            indication = rememberRipple(bounded = true)
        ) {
            launch {
                updateState { state ->
                    state.copy(list = state.list.let {
                        if (index % 2 == 0) {
                            it - string
                        } else {
                            it + "New String: ${System.currentTimeMillis()}"
                        }
                    })
                }.await()
            }
        }) {
            Box(
                modifier = Modifier.fillParentMaxWidth()
                    .height(50.dp)
                    .padding(start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = string)
            }
            Box(modifier = Modifier.padding(start = 20.dp)) { Divider(modifier = Modifier.fillParentMaxWidth().height(1.dp), color = Color(red = 0xCA, green = 0xCA, blue = 0xCA)) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
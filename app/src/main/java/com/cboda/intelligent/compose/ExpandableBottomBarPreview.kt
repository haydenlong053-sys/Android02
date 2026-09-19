package com.cboda.intelligent.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.cboda.intelligent.R

@Preview(showBackground = true)
@Composable
fun ExpandableBottomBarPreview() {
    val selectedIndexState = remember { mutableStateOf(0) }
    val previewItems = listOf(
        ExpandableBottomBarBroadItem(
            icon = painterResource(R.drawable.ic_home_tab_broad_message),
            selectedIcon = painterResource(R.drawable.ic_home_tab_message_broad_pre),
            text = "社交",
            badge = "12"
        ),
        ExpandableBottomBarBroadItem(
            icon = painterResource(R.drawable.ic_home_tab_broad_mine),
            selectedIcon = painterResource(R.drawable.ic_home_tab_mine_broad_pre),
            text = "中心",
            badge = "0"
        ),
    )

    MaterialTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F1FB))
        ) {
            ExpandableBottomBar(
                selectedIndex = selectedIndexState.value,
                onItemSelected = { selectedIndexState.value = it },
                items = previewItems,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

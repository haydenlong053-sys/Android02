package com.cboda.intelligent.compose.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cboda.intelligent.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressScreen(
    navController: NavController,
    initialProfession:String,
    onConfirm: (profession: String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var finalJob by remember { mutableStateOf(initialProfession) }
    // 使用本地状态控制输入框，避免每次打字都触发全局重组，只有离开页面或点击保存时才提交到 VM
    // 或者为了实时性，直接绑定 VM 也可以。这里直接绑定 VM 演示简单版。

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                ),
                expandedHeight = 46.dp,
                navigationIcon = {
                    if (onBack != null) {
                        Icon(
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onBack.invoke()
                            },
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.Black,
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.region),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
//                                navController.popBackStack()
                                if (finalJob.isNotEmpty()){
                                    onConfirm.invoke(finalJob)
                                }


                            }) {
                        Text(
                            text = stringResource(R.string.保存),
                            fontSize = 14.sp,
                            color = Color(0xff3B47DD)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 备注名输入
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.my_region),
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.65f)
                )
                Spacer(Modifier.height(12.dp))
                ProfessionInputField(
                    value = finalJob,
                    onValueChange = {
                        finalJob = if (it.length <= 16) {
                            it
                        } else {
                            it.substring(0, 16)
                        }
                    },
                    hint = initialProfession,
                )
            }
        }
    }
}

@Composable
private fun ProfessionInputField(value: String, onValueChange: (String) -> Unit, hint: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        val fieldTextStyle = TextStyle(
            color = Color.Black.copy(0.9f),
            fontSize = 14.sp
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = fieldTextStyle,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            minLines = 1,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint.ifEmpty { stringResource(R.string.请输入) },
                            style = fieldTextStyle.copy(color = Color.Black.copy(0.35f))
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview
@Composable
fun EditAddressScreenPreview() {
    EditAddressScreen(rememberNavController(), "设计师", {}, {})
}
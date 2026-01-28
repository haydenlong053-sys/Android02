package com.cboda.intelligent.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.model.SocialBroadCircle
import com.cboda.intelligent.utils.goTempTranslateString
import com.cboda.intelligent.viewmodel.SocialViewBroadModel

/**
 * 可复用的备注名 + 圈子选择页面内容。
 * - initialRemark: 默认备注名（从上一个页面传入）
 * - onConfirm: 点击确认返回备注名和选中的圈子（为空表示未选择）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemarkAndCirclePage(
    fromType:Int?=1,
    initialRemark: String,
    vm: SocialViewBroadModel,
    initialSelectedTag: String? = null,
    onConfirm: (remark: String, selectedCircle: SocialBroadCircle?) -> Unit,
    onBack: ((String) -> Unit)? = null
) {
    val uiState by vm.uiState.collectAsState()
    var remark by remember { mutableStateOf(initialRemark) }
    var selectedTag by remember { mutableStateOf<String?>(initialSelectedTag ?: uiState.selectedCircleTag) }
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        // 确保圈子数据已刷新
        vm.refresh()
    }

    Scaffold(
        contentColor = colorResource(com.cboda.chatemoji.R.color.colorF7F8FA),
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
                            ){
                                onBack.invoke(if (fromType==1) "" else remark)
                            },
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.social_back),
                            tint = Color.Black,
                        )
                    }
                },
                title = {
                    Text(
                        text = if (fromType==1) stringResource(R.string.social_friend_verification) else stringResource(R.string.social_persona_tag),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
               Column {

                      Button(
                          onClick = {
//                              val circle = uiState.circles.firstOrNull { it.characterTags == selectedTag }
                              val circle = uiState.circles.firstOrNull { goTempTranslateString(ctx,it.characterTags) == selectedTag }
                              if (circle == null) {
                                  ToastBroadUtil.show(ctx.getString(R.string.social_select_circle))
                              } else {
                                  onConfirm(remark.ifEmpty { initialRemark }, circle)
                              }
                          },
                          shape = RoundedCornerShape(16.dp),
                          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B47DD)),
                          modifier = Modifier
                              .fillMaxWidth()
                              .height(46.dp),
                      ) {
                          Text(text = stringResource(R.string.queding), color = Color.White, fontSize = 16.sp)
                      }
                   if (fromType==1){
                      Spacer(modifier = Modifier.height(24.dp))
                      OutlinedButton(onClick = {onBack?.invoke(if (fromType==1) "" else remark)}, modifier = Modifier
                          .fillMaxWidth()
                          .height(46.dp),
                          shape = RoundedCornerShape(16.dp),
                          colors = ButtonDefaults.outlinedButtonColors(
                              contentColor = Color(0xFF3B47DD),
                          ),
                          border = BorderStroke(1.dp, Color(0xFF3B47DD))) {
                          Text(text = stringResource(R.string.quxiao), color = Color(0xFF3B47DD), fontSize = 16.sp)
                      }
                  }
               }
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 备注名输入
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.social_set_remark_name),
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.65f)
                )
                Spacer(Modifier.height(12.dp))
                RemarkInputField(
                    value = remark,
                    onValueChange = { remark = it },
                    hint=initialRemark,
                )
            }

            Spacer(Modifier.height(24.dp))

            // 圈子选择（无“全部”，单选，可取消）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.social_set_circle_tag),
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.65f)
                )
                Spacer(Modifier.height(0.dp))
                CircleChipsRow(
                    circles = uiState.circles,
                    selectedTag = selectedTag,
                    onSelect = { selectedTag = it },
                    showAllOption = false
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}


@Composable
private fun RemarkInputField(value: String, onValueChange: (String) -> Unit, hint: String) {
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
                            text = hint,
                            style = fieldTextStyle.copy(color = Color.Black.copy(0.35f))
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "备注+圈子页面预览")
@Composable
private fun RemarkAndCirclePagePreview() {
    val vm = SocialViewBroadModel()
    MaterialTheme {
        RemarkAndCirclePage(
            initialRemark = "张三",
            vm = vm,
            initialSelectedTag = null,
            onConfirm = { _, _ -> },
            onBack = {}
        )
    }
}


/**
 * 圈子选择 Chips 行，可选择“全部”（参考 SocialFragment 的样式与圆角）
 */
@Composable
private fun CircleChipsRow(
    circles: List<SocialBroadCircle>,
    selectedTag: String?,
    onSelect: (String?) -> Unit,
    showAllOption: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 两列网格：每行两个
        circles.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val left = pair.getOrNull(0)
                val right = pair.getOrNull(1)
                val context =LocalContext.current
                if (left != null) {
//                    val isSelected = selectedTag == left.characterTags
                    val isSelected = selectedTag == goTempTranslateString(context,left.characterTags)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        SelectableChip(
                            text = left.characterTags,
                            desc = left.characterExplanation,
                            selected = isSelected,
//                            onClick = { onSelect(if (isSelected) null else left.characterTags) }
                            onClick = { onSelect(if (isSelected) null else goTempTranslateString(context,left.characterTags)) }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (right != null) {
//                    val isSelected = selectedTag == right.characterTags
                    val isSelected = selectedTag == goTempTranslateString(context,right.characterTags)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        SelectableChip(
                            text = right.characterTags,
                            desc = right.characterExplanation,
                            selected = isSelected,
//                            onClick = { onSelect(if (isSelected) null else right.characterTags) }
                            onClick = { onSelect(if (isSelected) null else goTempTranslateString(context,right.characterTags)) }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 参考 SocialFragment 的 Chip 样式：
 * - 选中前景色：#3B47DD
 * - 选中边框色：#E2E3FF
 * - 圆角：16dp
 * - 背景：白色
 */
@Composable
private fun SelectableChip(text: String, desc:String,selected: Boolean, onClick: () -> Unit) {
    val bg = Color.White
    val fg = if (selected) Color(0xFF3B47DD) else Color.Black.copy(alpha = 0.65f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg, RoundedCornerShape(16.dp))
            .then(
                if (selected)
                    Modifier.border(1.dp, Color(0xFF3B47DD), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(interactionSource = remember { MutableInteractionSource() },
                indication = null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
       Column(horizontalAlignment = Alignment.CenterHorizontally) {
           Text(
               text = goTempTranslateString(LocalContext.current,text),
               fontSize = 16.sp,
               fontWeight = FontWeight.W400,
               color = fg,
               textAlign = TextAlign.Center,
           )
           Spacer(modifier = Modifier.height(8.dp))
           Text(
               text = goTempTranslateString(LocalContext.current,desc),
               fontSize = 12.sp,
               fontWeight = FontWeight.W400,
               color = fg,
               textAlign = TextAlign.Center
           )

       }
    }
}
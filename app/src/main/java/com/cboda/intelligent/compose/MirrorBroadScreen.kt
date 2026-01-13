package com.cboda.intelligent.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.cboda.intelligent.R
import com.cboda.intelligent.viewmodel.MirrorViewBroadModel

@Preview
@Composable
fun MirrorScreenPreview() {
    MirrorScreen(MirrorViewBroadModel())
}

@Composable
fun MirrorScreen(
    viewModel: MirrorViewBroadModel,
    toQrCodePage: () -> Unit = {},
    toRedPacket: () -> Unit = {},
    toInfoPage:()->Unit={}
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiContext = LocalContext.current
    var showTip by remember { mutableStateOf(false) }
    var isClickClose by remember { mutableStateOf(false) }
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (bg, qr, credit, redPacket, avatar, bottomGrad, bottomLine, tipInfo) = createRefs()

        // 背景图
        Image(
            painter = painterResource(R.drawable.bg_broad_mirror),
            contentDescription = "mirror background",
            modifier = Modifier.constrainAs(bg) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            },
            contentScale = ContentScale.Crop
        )

        // 中间角色形象（默认女生）
        uiState.userInfo?.let {
            val useSex = it.sex?.toIntOrNull()
            val nameTag = TagBroadSpec(it.name?:(it.idno?:""), 0.10f, 97.dp)
            val tagSpecs = listOf(nameTag)
            Image(
                painter = painterResource(if (useSex==1)R.drawable.ic_mirror_broad_male else R.drawable.ic_mirror_broad_female),
                contentDescription = "avatar",
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .constrainAs(avatar) {
                        bottom.linkTo(parent.bottom, margin = 80.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .clickable(
                        indication = null, // 没有点击效果
                        interactionSource = remember { MutableInteractionSource() }) {
                        toInfoPage.invoke()
                    },
                contentScale = ContentScale.Fit
            )

            // 角色周围的标签 Chips（用指引线做百分比横向定位，纵向相对头像顶部的 dp）
//                val tags = defaultTagSpecs()
            val tags = tagSpecs
            tags.forEach { spec ->
                val gx = createGuidelineFromStart(spec.xPercent)
                val tagRef = createRef()
                TagChip(
                    text = spec.text,
                    modifier = Modifier
                        .constrainAs(tagRef) {
                            top.linkTo(avatar.top, margin = spec.yFromAvatarTop)
                            start.linkTo(gx)
                        }
                        .clickable(
                            indication = null, // 没有点击效果
                            interactionSource = remember { MutableInteractionSource() }) {
                            toInfoPage.invoke()
                        }
                )
            }
            if ((useSex==null||useSex<1)&&!isClickClose){
                showTip=true
            }
            if (showTip){
                TipToInfoPopUp(onDismiss = {showTip=false
                    isClickClose=true}, okClick = {
                    showTip=false
                    isClickClose=true
                    toInfoPage.invoke()})
            }

        }

        // 顶部右侧：信用分卡片 + 签到红包（红包在卡片下方12dp）
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .width(85.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color.White, shape = RoundedCornerShape(10.dp))
                .background(Color(0xFF4F6FF3).copy(0.2f))
                .constrainAs(credit) {
                    end.linkTo(parent.end, margin = 16.dp)
                    top.linkTo(parent.top, margin = 16.dp)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    buildAnnotatedString {
                        // 正确：样式 + append 一起
                        withStyle(
                            SpanStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.W700,
                                color = Color(0xff031256)
                            )
                        ) {
                            append("${uiState.userInfo?.grade?:100}")
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W400,
                                color = Color(0xff031256)
                            )
                        ) {
                            append("分")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "信用分", fontSize = 10.sp, color = Color(0xff031256))
            }
        }

        // 顶部左侧：二维码按钮（白色圆角胶囊）
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .clip(RoundedCornerShape(0.dp, 21.dp, 21.dp, 0.dp))
                .border(1.dp, Color.White, shape = RoundedCornerShape(0.dp, 21.dp, 21.dp, 0.dp))
                .background(Color.White.copy(alpha = 0.65f))
                .wrapContentHeight()
                .width(85.dp)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .clickable(
                    enabled = true,
                    indication = null, // 去除点击动画效果
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    toQrCodePage.invoke()

                }
                .constrainAs(qr) {
                    start.linkTo(parent.start, margin = 0.dp)
                    top.linkTo(parent.top, margin = 16.dp)
                },
            contentAlignment = Alignment.Center

        ) {
            Image(
                painter = painterResource(R.drawable.ic_mirror_broad_qrcode),
                contentDescription = "qrcode",
                modifier = Modifier.size(20.dp)
            )
        }

        Image(
            painter = painterResource(if (!uiState.isSign) R.drawable.bg_sign_broad_false else R.drawable.bg_sign_broad_true),
            contentDescription = "sign",
            modifier = Modifier
                .width(50.dp)
                .height(64.dp)
                .constrainAs(redPacket) {
                    top.linkTo(credit.bottom, margin = 12.dp)
                    end.linkTo(credit.end)
                }
                .clickable(enabled = true) {
                    toRedPacket.invoke()
                },
            contentScale = ContentScale.FillBounds
        )

        // 底部渐变区域
        Image(
            painter = painterResource(R.drawable.bg_mirror_broad_bottom),
            contentDescription = "bottom",
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
                .constrainAs(bottomGrad) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            contentScale = ContentScale.FillBounds
        )

        // 底部线条（贴在底部渐变图的顶部）
        Image(
            painter = painterResource(R.drawable.bg_mirror_bottom_broad_line),
            contentDescription = "bottom line",
            modifier = Modifier
                .fillMaxWidth()
                .height(31.dp)
                .constrainAs(bottomLine) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(bottomGrad.top, margin = -(11).dp)
                },
            contentScale = ContentScale.FillBounds
        )
        // 注意：底部圆形导航已在 MainBroadActivity 实现，此处不再绘制
    }
}

@Immutable
private data class TagBroadSpec(
    val text: String,
    val xPercent: Float,
    val yFromAvatarTop: androidx.compose.ui.unit.Dp
)

private fun defaultTagSpecs(): List<TagBroadSpec> {
    return listOf(
        TagBroadSpec("KOILLYRT", 0.10f, 97.dp),
        TagBroadSpec("中国", 0.28f, 215.dp),
        TagBroadSpec("300天", 0.56f, 140.dp),
        TagBroadSpec("喜欢看日出", 0.14f, 280.dp),
        TagBroadSpec("喜欢打游戏", 0.45f, 230.dp),
        TagBroadSpec("旅游巡游世", 0.66f, 310.dp),
        TagBroadSpec("活跃用户", 0.18f, 380.dp),
    )
}

@Composable
private fun TagChip(text: String, modifier: Modifier = Modifier) {
    // 取消固定宽度，背景随文字长度变化；超过 8 个字追加省略号
    val displayText = if (text.length > 8) text.take(8) + "…" else text
    Box(
        modifier = modifier
//            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .wrapContentWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.mipmap.
            bg_mirror_tag_broad_big),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = displayText,
            color = Color.White,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
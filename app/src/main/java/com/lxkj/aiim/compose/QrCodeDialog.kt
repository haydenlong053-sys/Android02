package com.lxkj.aiim.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.lxkj.aiim.R
import com.lxkj.aiim.utils.AppUtil
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.text.font.FontWeight
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@Composable
fun QrCodeDialog(
    userName: String?,
    userId: String?,
    avatarResId: Int? = R.mipmap.ic_head_defaut,
    avatarUrl: String? = null,
    qrContent: String?,
    onDismiss: () -> Unit,
    onSaveToGallery: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .padding(top = 40.dp)
        ) {
            // 内容卡片（裁剪圆角），外层不裁剪，避免头像被截断
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 顶部为头像让出空间：需要总计 48dp（40dp 头像压入 + 8dp 间距）
                    // 注意 Column 有 20dp 的顶部内边距，因此这里 Spacer 设为 28dp
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = userName ?: "",
                        fontSize = 20.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 隐藏账号ID，但保留其高度占位
                    Text(
                        text = "账号ID：${userId ?: ""}",
                        fontSize = 14.sp,
                        color = Color(0xFF6D7DA0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    //二维码
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
                        },
                        update = { imageView ->
                            Glide.with(imageView.context)
                                .load(qrContent)
                                .fitCenter()
//                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(com.lqr.emoji.R.color.gray)
                                .into(imageView)
                        },
                        modifier = Modifier
                            .size(200.dp)
//                            .clip(CircleShape)
                    )

//                    // 二维码
//                    val qrBitmap = remember(qrContent) {
//                        AppUtil.createQRImage(qrContent ?: "", 800, 800)
//                    }
//                    if (qrBitmap != null) {
//                        Image(
//                            bitmap = qrBitmap.asImageBitmap(),
//                            contentDescription = "qrcode",
//                            modifier = Modifier
//                                .size(200.dp)
//                        )
//                    } else {
//                        Box(
//                            modifier = Modifier
//                                .size(200.dp)
//                                .background(Color(0xFF000000))
//                        ) {}
//                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "使用SeekWeb4扫一扫，加入为好友",
                        fontSize = 14.sp,
                        color = Color.Black.copy(0.45f),
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 保存二维码按钮（UI占位，具体保存逻辑可后续接入）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.Black.copy(0.12f), RoundedCornerShape(16.dp))
                            .clickable(enabled = true) {
                                 onDismiss.invoke()
                                if (!qrContent.isNullOrBlank()) {
                                    onSaveToGallery.invoke(qrContent)
                                }


                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "保存二维码",
                            fontSize = 16.sp,
                            color = Color(0XFF3B47DD)
                        )
                    }
                }
            }

            // 顶部悬浮头像（居中覆盖卡片顶部），优先使用 URL，缺省使用资源
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-40).dp)
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .zIndex(1f)
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
                        },
                        update = { imageView ->
                            Glide.with(imageView.context)
                                .load(avatarUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(avatarResId ?: R.mipmap.ic_head_defaut)
                                .into(imageView)
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(avatarResId ?: R.mipmap.ic_head_defaut),
                        contentDescription = "avatar",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}
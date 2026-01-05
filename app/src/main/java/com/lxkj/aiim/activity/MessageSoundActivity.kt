package com.lxkj.aiim.activity

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cboda.baselibrary.utils.SharePrefBroadBroadUtil
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import java.io.IOException

class MessageSoundActivity : BaseSimpleActivity() {
    
    private var mediaPlayer: MediaPlayer? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_base
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun useStatusBarDarkFont(): Boolean {
        return true
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MessageSoundScreen(
                onSave = { selectedSound ->
                    SharePrefBroadBroadUtil.saveString(this, SharePrefBroadBroadUtil.RING_FILE_NAME_KEY, selectedSound)
                    finish()
                },
                onPlaySound = { soundFile ->
                    playSound(soundFile)
                },
                onBack = {
                    finish()
                }
            )
        }
    }
    
    private fun playSound(soundFile: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                val afd: AssetFileDescriptor = assets.openFd(soundFile)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSoundScreen(
    onSave: (String) -> Unit,
    onPlaySound: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // 获取状态栏高度
    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }
    
    // 音频文件列表
    val soundFiles = listOf(
        "tixing.mp3" to "叮当",
        "item_found.mp3" to "清脆",
        "instant_message.mp3" to "叮咚"
    )
    
    // 获取当前选中的音频文件
    val currentSound = SharePrefBroadBroadUtil.getString(context, SharePrefBroadBroadUtil.RING_FILE_NAME_KEY, "item_found.mp3")
    var selectedSound by remember { mutableStateOf(currentSound) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_main))
    ) {
        // 沉浸式图片标题栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp + statusBarHeight)
        ) {
            // 背景图片
            Image(
                painter = painterResource(id = R.mipmap.ic_bg_top_main),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter
            )
            
            // 标题栏内容 - 在50dp高度内垂直居中，但整体位于状态栏下方
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
                    .offset(y = statusBarHeight)
            ) {
                // 返回按钮 - 左对齐
                Image(
                    painter = painterResource(id = R.mipmap.ic_back),
                    contentDescription = "返回",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterStart)
                        .clickable { onBack() },
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                )
                
                // 标题 - 居中
                Text(
                    text = "消息提示音",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // 保存按钮 - 右对齐
                Button(
                    onClick = { onSave(selectedSound) },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.purple_200)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "保存",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // 音频选择列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(soundFiles) { index, (fileName, displayName) ->
                SoundItem(
                    fileName = fileName,
                    displayName = displayName,
                    isSelected = selectedSound == fileName,
                    onSelect = { 
                        selectedSound = fileName
                        onPlaySound(fileName)
                    }
                )
            }
        }
    }
}

@Composable
fun SoundItem(
    fileName: String,
    displayName: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .background(colorResource(id = R.color.bg_main))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                fontSize = 15.sp,
                color = colorResource(id = R.color.color_text0)
            )
            
            if (isSelected) {
                Image(
                    painter = painterResource(id = R.mipmap.btn_checkbox_press),
                    contentDescription = "已选中",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // 分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colorResource(id = com.cboda.chatui.R.color.color_line))
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MessageSoundScreenPreview() {
    MessageSoundScreen(
        onSave = { },
        onPlaySound = { },
        onBack = { }
    )
}

@Preview(showBackground = true)
@Composable
fun SoundItemPreview() {
    Column {
        SoundItem(
            fileName = "instant_message.mp3",
            displayName = "即时消息",
            isSelected = true,
            onSelect = { }
        )
        SoundItem(
            fileName = "item_found.mp3",
            displayName = "物品发现",
            isSelected = false,
            onSelect = { }
        )
    }
}
package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import com.cboda.intelligent.R

import androidx.compose.ui.tooling.preview.Preview
import com.cboda.intelligent.compose.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTBackupScreen(
    mnemonic: String,
    title: String = stringResource(R.string.wt_backup_mnemonic_title),
    buttonText: String = stringResource(R.string.wt_backed_up),
    onBack: () -> Unit,
    onBackupConfirmed: () -> Unit
) {
    val words = mnemonic.split(" ")
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showSecurityTipDialog by remember { mutableStateOf(false) }

    if (showSecurityTipDialog) {
        SecurityTipDialog(
            onDismiss = { showSecurityTipDialog = false },
            onConfirm = {
                showSecurityTipDialog = false
                clipboardManager.setText(AnnotatedString(mnemonic))
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = title, 
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500
                    ) 
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier.size(48.dp).noRippleClickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, 
                            contentDescription = stringResource(R.string.wt_back), 
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF9F9F9)
                )
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.wt_backup_mnemonic_desc_1))
                    withStyle(style = SpanStyle(color = Color(0xFFEB534B))) {
                        append(stringResource(R.string.wt_backup_mnemonic_desc_2))
                    }
                    append(stringResource(R.string.wt_backup_mnemonic_desc_3))
                },
                fontSize = 14.sp,
                color = Color.Black.copy(0.65f),
                lineHeight = 20.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(words) { index, word ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(vertical = 12.dp, horizontal = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = String.format("%02d", index + 1),
                                fontSize = 14.sp,
                                color = Color.Black.copy(0.35f),

                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "|",
                                color  = Color.Black.copy(0.12f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = word,
                                fontSize = 14.sp,
                                color = Color(0xFF3B47DD),
                                fontWeight = FontWeight.W400,
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = Color(0xFF3B47DD),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .noRippleClickable { 
                        showSecurityTipDialog = true
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.mipmap.ic_wt_backup_copy),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.wt_copy_mnemonic),
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.W500,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF3B47DD)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .noRippleClickable(onClick = onBackupConfirmed)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = buttonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        color = Color(0xFF3B47DD)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewWTBackupScreen() {
    WTBackupScreen(
        mnemonic = "apple banana cherry dog elephant frog grape house igloo kite lemon mango",
        onBack = {},
        onBackupConfirmed = {}
    )
}

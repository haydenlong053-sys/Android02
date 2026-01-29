package com.cboda.intelligent.compose.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import com.cboda.intelligent.R

@Composable
fun SecurityTipDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.wt_security_tip_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.wt_security_tip_content),
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SecurityTipItem(
                    index = 1,
                    text = buildAnnotatedString {
                        append(stringResource(R.string.wt_security_tip_1_prefix))
                        withStyle(style = SpanStyle(color = Color(0xFFFF5252))) {
                            append(stringResource(R.string.wt_security_tip_1_highlight))
                        }
                        append(stringResource(R.string.wt_security_tip_1_suffix))
                    }
                )

                SecurityTipItem(
                    index = 2,
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFFFF5252))) {
                            append(stringResource(R.string.wt_security_tip_2_highlight))
                        }
                        append(stringResource(R.string.wt_security_tip_2_suffix))
                    }
                )

                SecurityTipItem(
                    index = 3,
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFFFF5252))) {
                            append(stringResource(R.string.wt_security_tip_3_highlight))
                        }
                        append(stringResource(R.string.wt_security_tip_3_middle))
                        withStyle(style = SpanStyle(color = Color(0xFFFF5252))) {
                            append(stringResource(R.string.wt_security_tip_3_highlight2))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5B6CFF))
                    ) {
                        Text(
                            text = stringResource(R.string.wt_cancel),
                            color = Color(0xFF5B6CFF),
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B6CFF)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.wt_confirm),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityTipItem(index: Int, text: androidx.compose.ui.text.AnnotatedString) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color(0xFF5B6CFF), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF333333),
            lineHeight = 18.sp
        )
    }
}

@Preview
@Composable
fun PreviewSecurityTipDialog() {
    SecurityTipDialog(
        onDismiss = {},
        onConfirm = {}
    )
}

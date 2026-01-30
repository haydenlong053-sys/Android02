package com.cboda.intelligent.compose.wallet

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cboda.intelligent.model.LedgerItem
import com.cboda.intelligent.R
import com.cboda.intelligent.compose.noRippleClickable
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WTTransactionDetailScreen(
    transaction: LedgerItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val amount = transaction.amount

    val fee = transaction.gas_fee?:"0"

    val dateStr = transaction.block_time?.let { time ->
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(time)
            val outputFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            date?.let { outputFormat.format(it) }
        } catch (e: Exception) {
            time
        }
    } ?: ""

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.wt_transaction_detail_title), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", modifier = Modifier.size(32.dp))
                    }
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF9F9F9))
            )
        },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val status = transaction.status?.lowercase() ?: "failed"
                    val (iconRes, textRes) = when {
                        status == "pending" -> R.mipmap.ic_icon_state_broad_ing to R.string.wt_processing_title
                        status == "1" || status == "confirmed" -> R.mipmap.ic_icon_state_broad_success to R.string.wt_transaction_success
                        else -> R.mipmap.ic_icon_state_broad_fail to R.string.wt_transaction_failed
                    }
                    
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(35.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        stringResource(textRes),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${if (transaction.direction == "in") "+" else "-"}$amount ",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.W700,
                        color = Color.Black.copy(0.9f)
                    )
                    Text(transaction.token_symbol ?: "BNB", fontSize = 20.sp, color = Color.Black.copy(0.65f), fontWeight = FontWeight.W600)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // To/From based on direction?
                // LedgerItem usually has 'counterparty' which is the other address.
                // Or we can show From and To if available.
                // Assuming LedgerItem has address (my address) and counterparty.
                // If direction is 'in', From = counterparty, To = address
                // If direction is 'out', From = address, To = counterparty
                
                val (fromAddress, toAddress) = if (transaction.direction == "in") {
                     transaction.counterparty to transaction.address
                } else {
                     transaction.address to transaction.counterparty
                }

                DetailRow(
                    label = stringResource(R.string.wt_transfer_to_address), 
                    value = toAddress,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(toAddress))
                        Toast.makeText(context, context.getString(R.string.wt_copied), Toast.LENGTH_SHORT).show()
                    }
                )
                DetailRow(
                    label = stringResource(R.string.wt_payer_address), 
                    value = fromAddress,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(fromAddress))
                        Toast.makeText(context, context.getString(R.string.wt_copied), Toast.LENGTH_SHORT).show()
                    }
                )
                DetailRow(
                    label = stringResource(R.string.wt_tx_hash), 
                    value = transaction.tx_hash,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(transaction.tx_hash))
                        Toast.makeText(context, context.getString(R.string.wt_copied), Toast.LENGTH_SHORT).show()
                    }
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.wt_network_fee), color = Color.Gray)
                        Text("$fee ${transaction.token_symbol}", fontWeight = FontWeight.Medium) // Assuming fee in same token or native? Usually native (BNB). But keeping logic simple.
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.wt_network), color = Color.Black.copy(0.65f), fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.mipmap.ic_wt_bsc),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BSC", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.wt_time), color = Color.Black.copy(0.65f), fontSize = 16.sp)
                        Text(dateStr, color = Color.Black.copy(0.65f), fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, onCopy: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(label, color = Color.Black.copy(0.65f), fontSize = 16.sp, modifier = Modifier.width(68.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayValue = if (value.length > 20) "${value.take(10)}...${value.takeLast(10)}" else value
                Text(

                    displayValue, 
                    fontWeight = FontWeight.W400,
                    maxLines = 3,
                    fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.mipmap.ic_transfer_detail_copy),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp).noRippleClickable(onClick = onCopy)
                )
            }
        }
    }
}

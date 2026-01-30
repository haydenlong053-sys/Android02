package com.cboda.intelligent.compose.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.dialog.pop.AuthCodeBroadPop
import com.cboda.intelligent.dialog.pop.AuthPasswordBroadPop
import com.cboda.intelligent.dialog.pop.AuthQuestionBroadPop
import com.cboda.intelligent.viewmodel.WTViewModel
import razerdp.basepopup.BasePopupWindow

@Composable
fun WTSecurityVerification(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPassWordVerifySuccess: (String) -> Unit,
    viewModel: WTViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(showDialog) {
        if (showDialog) {
            // Ensure safe center info is loaded
            if (viewModel.uiState.value.safeCenterInfo == null) {
                viewModel.getSafeCenterInfo()
            }

            // Wait for safeCenterInfo
            viewModel.uiState
                .map { it.safeCenterInfo }
                .filterNotNull()
                .first()

            val codePop = AuthCodeBroadPop(context, SafeJumpBroadEnum.TransferAuthSure.sourceType)
            codePop.onDismissListener = object : BasePopupWindow.OnDismissListener() {
                override fun onDismiss() {
                    onDismiss()
                }
            }

            codePop.transferInvoke = {
                val questionPop = AuthQuestionBroadPop(context, SafeJumpBroadEnum.QuestionAuth.sourceType)
                questionPop.onDismissListener = object : BasePopupWindow.OnDismissListener() {
                    override fun onDismiss() {
                        onDismiss()
                    }
                }
                questionPop.backInvoke = {
                    val passwordPop = AuthPasswordBroadPop(context, SafeJumpBroadEnum.EthPasswordAuth.sourceType)
                    passwordPop.onDismissListener = object : BasePopupWindow.OnDismissListener() {
                        override fun onDismiss() {
                            onDismiss()
                        }
                    }
                    passwordPop.backPwdInvoke = { pwd ->
                        onPassWordVerifySuccess(pwd)
                    }
                    passwordPop.showPopupWindow()
                }
                questionPop.showPopupWindow()
            }
            codePop.showPopupWindow()
        }
    }
}

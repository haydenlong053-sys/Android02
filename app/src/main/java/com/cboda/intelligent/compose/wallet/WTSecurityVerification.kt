package com.cboda.intelligent.compose.wallet

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.SafeCenterBroadActivity
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.dialog.pop.AuthCodeBroadPop
import com.cboda.intelligent.dialog.pop.AuthPasswordBroadPop
import com.cboda.intelligent.dialog.pop.AuthQuestionBroadPop
import com.cboda.baselibrary.popup.CommonTipBroadPop
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
            // Request safe center info every time
            viewModel.getSafeCenterInfo()

            // Wait for safeCenterInfo
            val safeCenterInfo = viewModel.uiState
                .map { it.safeCenterInfo }
                .filterNotNull()
                .first()

            val isPayPasswordSet = !safeCenterInfo.payPassword.isNullOrEmpty()
            val isTwoFactorSet = !safeCenterInfo.twoFactorCode.isNullOrEmpty()
            val isSecurityIdSet = !safeCenterInfo.securityId.isNullOrEmpty()

            if (!isPayPasswordSet || !isTwoFactorSet || !isSecurityIdSet) {
                val commonTipPop = CommonTipBroadPop(
                    context,
                    context.getString(R.string.mine_tip_other_title),
                    context.getString(R.string.mine_tip_other_des)
                )
                commonTipPop.setCancelBtn(context.getString(R.string.mine_tip_wait))
                commonTipPop.setSureBtn(context.getString(R.string.mine_ok))
                
                commonTipPop.sureInvoke = {
                    val intent = Intent(context, SafeCenterBroadActivity::class.java)
                    context.startActivity(intent)
                }
                
                commonTipPop.onDismissListener = object : BasePopupWindow.OnDismissListener() {
                    override fun onDismiss() {
                        onDismiss()
                    }
                }
                commonTipPop.showPopupWindow()
                return@LaunchedEffect
            }

            val codePop = AuthCodeBroadPop(context, SafeJumpBroadEnum.TransferAuthSure.sourceType)
            codePop.setBackPressEnable(true)
            codePop.setOutSideDismiss(true)
            codePop.onDismissListener = object : BasePopupWindow.OnDismissListener() {
                override fun onDismiss() {
                    onDismiss()
                }
            }

            codePop.transferInvoke = {
                val questionPop = AuthQuestionBroadPop(context, SafeJumpBroadEnum.QuestionAuth.sourceType)
                questionPop.setBackPressEnable(true)
                questionPop.setOutSideDismiss(true)
                questionPop.onDismissListener = object : BasePopupWindow.OnDismissListener() {
                    override fun onDismiss() {
                        onDismiss()
                    }
                }
                questionPop.backInvoke = {
                    val passwordPop = AuthPasswordBroadPop(context, SafeJumpBroadEnum.EthPasswordAuth.sourceType)
                    passwordPop.setBackPressEnable(true)
                    passwordPop.setOutSideDismiss(true)
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

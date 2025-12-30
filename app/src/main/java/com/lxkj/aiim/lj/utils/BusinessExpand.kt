package com.lxkj.aiim.lj.utils


import android.content.Context
import android.content.Intent
import com.lxkj.aiim.R
import com.lxkj.aiim.dialog.pop.AuthCodePop
import com.lxkj.aiim.dialog.pop.AuthPasswordPop
import com.lxkj.aiim.dialog.pop.AuthQuestionPop
import com.lxkj.aiim.dialog.pop.AuthWayPop
import com.lxkj.aiim.dialog.pop.ChangeGuidePop
import com.lxkj.aiim.lj.GoogleAuthStep1Activity
import com.lxkj.aiim.lj.SecurityAuthEditActivity
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.model.SafeCenterBean

//仅有密码，
//1，点击谷歌，密码验证后，去绑定谷歌
//2，点击密码，引导添加谷歌或密保验证
//3，点击密保，密码验证后，去绑定密保
//
//仅有密码，以及谷歌
//1，点击谷歌，弹引导框，完成谷歌验证以及密码验证后，去换绑谷歌
//2，点击密码，弹引导框，完成谷歌验证后，重置密码
//3，点击密保，完成谷歌验证以及密码验证后，去换绑密保
//
//仅有密码，以及密保
//1，点击谷歌，完成密保验证以及密码验证后，去绑定谷歌
//2，点击密码，弹引导框，验证密保后，去重置密码
//3，点击密保，弹引导框，验证密码后，去重置密保
//
//有密保，谷歌，密码
//1，点谷歌，弹引导框，完成谷歌验证以及密保，密码验证后，去换绑谷歌
//2，点密码，弹引导框，完成谷歌验证以及密保后，去重置密码
//3，点密保，弹引导框，完成谷歌验证，以及密码后，去重置密保
fun goSafeCenterAuth(context: Context, jumpSource: Int, safeCenterBean: SafeCenterBean) {
    if (safeCenterBean.twoFactorCode?.isNotEmpty() == true && safeCenterBean.securityId?.isNotEmpty() == true && safeCenterBean.payPassword?.isNotEmpty() == true) {
        when (jumpSource) {
            //1，点谷歌，弹引导框，完成谷歌验证以及密保，密码验证后，去换绑谷歌
            SafeJumpEnum.GoogleAuth.sourceType -> {
                val changeGuidePop = ChangeGuidePop(
                    context,
                    context.getString(R.string.mine_change_google_auth_title),
                    context.getString(R.string.mine_change_auth_des2)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodePop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val authQuestionPop = AuthQuestionPop(
                            context,
                            jumpSource
                        )
                        authQuestionPop.backInvoke = {
                            val mPop = AuthPasswordPop(
                                context,
                                jumpSource
                            )
                            mPop.backInvoke = {
                                context.startActivity(
                                    Intent(context, GoogleAuthStep1Activity::class.java)
                                )
                            }
                            mPop.showPopupWindow()
                        }
                        authQuestionPop.showPopupWindow()

                    }
                    codePop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()

            }
            //2，点密码，弹引导框，完成谷歌验证以及密保后，去重置密码
            SafeJumpEnum.PasswordAuth.sourceType -> {
                val changeGuidePop = ChangeGuidePop(
                    context,
                    context.getString(R.string.mine_change_password_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodePop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val authQuestionPop = AuthQuestionPop(
                            context,
                            jumpSource
                        )
                        authQuestionPop.backInvoke = {
                            val mPop = AuthPasswordPop(
                                context,
                                SafeJumpEnum.PasswordChange.sourceType
                            )
                            mPop.showPopupWindow()
                        }
                        authQuestionPop.showPopupWindow()

                    }
                    codePop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()

            }
            //3，点密保，弹引导框，完成谷歌验证，以及密码后，去重置密保
            SafeJumpEnum.QuestionAuth.sourceType -> {
                val changeGuidePop = ChangeGuidePop(
                    context,
                    context.getString(R.string.mine_change_question_auth_title),
                    context.getString(R.string.mine_change_auth_des2)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodePop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val mPop = AuthPasswordPop(
                            context,
                            jumpSource
                        )
                        mPop.backInvoke = {
                            context.startActivity(
                                Intent(
                                    context,
                                    SecurityAuthEditActivity::class.java
                                )
                            )
                        }
                        mPop.showPopupWindow()

                    }
                    codePop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()
            }
        }

    } else if (safeCenterBean.securityId?.isNotEmpty() == true && safeCenterBean.payPassword?.isNotEmpty() == true) {
        when (jumpSource) {
            //1，仅有密码，以及密保->点击谷歌，完成密保验证以及密码验证后，去绑定谷歌
            SafeJumpEnum.GoogleAuth.sourceType -> {
                val authQuestionPop = AuthQuestionPop(
                    context,
                    jumpSource
                )
                authQuestionPop.backInvoke = {
                    val mPop = AuthPasswordPop(
                        context,
                        jumpSource
                    )
                    mPop.backInvoke = {
                        context.startActivity(
                            Intent(context, GoogleAuthStep1Activity::class.java)
                        )
                    }
                    mPop.showPopupWindow()
                }
                authQuestionPop.showPopupWindow()
            }
            //2，仅有密码，以及密保->点击密码，弹引导框，验证密保后，去重置密码
            SafeJumpEnum.PasswordAuth.sourceType -> {
                val changeGuidePop = ChangeGuidePop(
                    context,
                    context.getString(R.string.mine_change_password_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val authQuestionPop = AuthQuestionPop(
                        context,
                        jumpSource
                    )
                    authQuestionPop.backInvoke = {
                        val mPop = AuthPasswordPop(
                            context,
                            SafeJumpEnum.PasswordChange.sourceType
                        )
                        mPop.showPopupWindow()
                    }
                    authQuestionPop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()
            }
            //3，仅有密码，以及密保->点击密保，弹引导框，验证密码后，去重置密保
            SafeJumpEnum.QuestionAuth.sourceType -> {
                val changeGuidePop = ChangeGuidePop(
                    context,
                    context.getString(R.string.mine_change_question_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val mPop = AuthPasswordPop(
                        context,
                        jumpSource
                    )
                    mPop.backInvoke = {
                        context.startActivity(
                            Intent(
                                context,
                                SecurityAuthEditActivity::class.java
                            )
                        )
                    }
                    mPop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()
            }
        }

    } else if (safeCenterBean.twoFactorCode?.isNotEmpty() == true && safeCenterBean.payPassword?.isNotEmpty() == true) {
        when (jumpSource) {
            //仅有密码，以及谷歌->1，点击谷歌，弹引导框，完成谷歌验证以及密码验证后，去换绑谷歌
            SafeJumpEnum.GoogleAuth.sourceType -> {
                val changeGuidePop = ChangeGuidePop(
                    context,
                    context.getString(R.string.mine_change_google_auth_title),
                    context.getString(R.string.mine_change_auth_des2)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodePop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val mPop = AuthPasswordPop(
                            context,
                            jumpSource
                        )
                        mPop.backInvoke = {
                            context.startActivity(
                                Intent(context, GoogleAuthStep1Activity::class.java)
                            )
                        }
                        mPop.showPopupWindow()

                    }
                    codePop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()
            }
            //仅有密码，以及谷歌->2，点击密码，弹引导框，完成谷歌验证后，重置密码
            SafeJumpEnum.PasswordAuth.sourceType -> {
                val changeGuidePop = ChangeGuidePop(
                    context,
                    context.getString(R.string.mine_change_password_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodePop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val mPop = AuthPasswordPop(
                            context,
                            SafeJumpEnum.PasswordChange.sourceType
                        )
                        mPop.showPopupWindow()
                    }
                    codePop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()
            }
            //仅有密码，以及谷歌->3，点击密保，完成谷歌验证以及密码验证后，去换绑密保
            SafeJumpEnum.QuestionAuth.sourceType -> {
                val codePop = AuthCodePop(
                    context,
                    jumpSource
                )
                codePop.transferInvoke = {
                    val mPop = AuthPasswordPop(
                        context,
                        jumpSource
                    )
                    mPop.backInvoke = {
                        context.startActivity(
                            Intent(
                                context,
                                SecurityAuthEditActivity::class.java
                            )
                        )
                    }
                    mPop.showPopupWindow()

                }
                codePop.showPopupWindow()
            }
        }

    } else if (safeCenterBean.payPassword?.isNotEmpty() == true) {
        when (jumpSource) {
            //仅有密码->1，点击谷歌，密码验证后，去绑定谷歌
            SafeJumpEnum.GoogleAuth.sourceType -> {
                val mPop = AuthPasswordPop(
                    context,
                    jumpSource
                )
                mPop.backInvoke = {
                    context.startActivity(
                        Intent(context, GoogleAuthStep1Activity::class.java)
                    )
                }
                mPop.showPopupWindow()

            }
            //仅有密码->2，点击密码，弹引导框+引导添加谷歌或密保验证框
            SafeJumpEnum.PasswordAuth.sourceType -> {
                val changeGuidePop = ChangeGuidePop(
                    context,
                    context.getString(R.string.mine_change_password_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val authWayPop = AuthWayPop(context, SafeJumpEnum.PasswordAuth.sourceType)
                    authWayPop.showPopupWindow()
                }
                changeGuidePop.showPopupWindow()
            }
            //仅有密码->3，点击密保，密码验证后，去绑定密保
            SafeJumpEnum.QuestionAuth.sourceType -> {
                val mPop = AuthPasswordPop(
                    context,
                    jumpSource
                )
                mPop.backInvoke = {
                    context.startActivity(
                        Intent(context, SecurityAuthEditActivity::class.java)
                    )
                }
                mPop.showPopupWindow()

            }
        }

    }

}
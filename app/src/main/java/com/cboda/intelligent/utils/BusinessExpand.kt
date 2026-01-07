package com.cboda.intelligent.utils


import android.content.Context
import android.content.Intent
import com.cboda.intelligent.R
import com.cboda.intelligent.dialog.pop.AuthCodeBroadPop
import com.cboda.intelligent.dialog.pop.AuthPasswordBroadPop
import com.cboda.intelligent.dialog.pop.AuthQuestionBroadPop
import com.cboda.intelligent.dialog.pop.AuthWayBroadPop
import com.cboda.intelligent.dialog.pop.ChangeGuideBroadPop
import com.cboda.intelligent.activity.GoogleAuthStep1BroadActivity
import com.cboda.intelligent.activity.SecurityAuthEditBroadActivity
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.model.SafeCenterBroadBean

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
fun goSafeCenterAuth(context: Context, jumpSource: Int, safeCenterBean: SafeCenterBroadBean) {
    if (safeCenterBean.twoFactorCode?.isNotEmpty() == true && safeCenterBean.securityId?.isNotEmpty() == true && safeCenterBean.payPassword?.isNotEmpty() == true) {
        when (jumpSource) {
            //1，点谷歌，弹引导框，完成谷歌验证以及密保，密码验证后，去换绑谷歌
            SafeJumpBroadEnum.GoogleAuth.sourceType -> {
                val changeGuidePop = ChangeGuideBroadPop(
                    context,
                    context.getString(R.string.mine_change_google_auth_title),
                    context.getString(R.string.mine_change_auth_des2)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodeBroadPop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val authQuestionPop = AuthQuestionBroadPop(
                            context,
                            jumpSource
                        )
                        authQuestionPop.backInvoke = {
                            val mPop = AuthPasswordBroadPop(
                                context,
                                jumpSource
                            )
                            mPop.backInvoke = {
                                context.startActivity(
                                    Intent(context, GoogleAuthStep1BroadActivity::class.java)
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
            SafeJumpBroadEnum.PasswordAuth.sourceType -> {
                val changeGuidePop = ChangeGuideBroadPop(
                    context,
                    context.getString(R.string.mine_change_password_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodeBroadPop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val authQuestionPop = AuthQuestionBroadPop(
                            context,
                            jumpSource
                        )
                        authQuestionPop.backInvoke = {
                            val mPop = AuthPasswordBroadPop(
                                context,
                                SafeJumpBroadEnum.PasswordChange.sourceType
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
            SafeJumpBroadEnum.QuestionAuth.sourceType -> {
                val changeGuidePop = ChangeGuideBroadPop(
                    context,
                    context.getString(R.string.mine_change_question_auth_title),
                    context.getString(R.string.mine_change_auth_des2)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodeBroadPop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val mPop = AuthPasswordBroadPop(
                            context,
                            jumpSource
                        )
                        mPop.backInvoke = {
                            context.startActivity(
                                Intent(
                                    context,
                                    SecurityAuthEditBroadActivity::class.java
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
            SafeJumpBroadEnum.GoogleAuth.sourceType -> {
                val authQuestionPop = AuthQuestionBroadPop(
                    context,
                    jumpSource
                )
                authQuestionPop.backInvoke = {
                    val mPop = AuthPasswordBroadPop(
                        context,
                        jumpSource
                    )
                    mPop.backInvoke = {
                        context.startActivity(
                            Intent(context, GoogleAuthStep1BroadActivity::class.java)
                        )
                    }
                    mPop.showPopupWindow()
                }
                authQuestionPop.showPopupWindow()
            }
            //2，仅有密码，以及密保->点击密码，弹引导框，验证密保后，去重置密码
            SafeJumpBroadEnum.PasswordAuth.sourceType -> {
                val changeGuidePop = ChangeGuideBroadPop(
                    context,
                    context.getString(R.string.mine_change_password_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val authQuestionPop = AuthQuestionBroadPop(
                        context,
                        jumpSource
                    )
                    authQuestionPop.backInvoke = {
                        val mPop = AuthPasswordBroadPop(
                            context,
                            SafeJumpBroadEnum.PasswordChange.sourceType
                        )
                        mPop.showPopupWindow()
                    }
                    authQuestionPop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()
            }
            //3，仅有密码，以及密保->点击密保，弹引导框，验证密码后，去重置密保
            SafeJumpBroadEnum.QuestionAuth.sourceType -> {
                val changeGuidePop = ChangeGuideBroadPop(
                    context,
                    context.getString(R.string.mine_change_question_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val mPop = AuthPasswordBroadPop(
                        context,
                        jumpSource
                    )
                    mPop.backInvoke = {
                        context.startActivity(
                            Intent(
                                context,
                                SecurityAuthEditBroadActivity::class.java
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
            SafeJumpBroadEnum.GoogleAuth.sourceType -> {
                val changeGuidePop = ChangeGuideBroadPop(
                    context,
                    context.getString(R.string.mine_change_google_auth_title),
                    context.getString(R.string.mine_change_auth_des2)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodeBroadPop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val mPop = AuthPasswordBroadPop(
                            context,
                            jumpSource
                        )
                        mPop.backInvoke = {
                            context.startActivity(
                                Intent(context, GoogleAuthStep1BroadActivity::class.java)
                            )
                        }
                        mPop.showPopupWindow()

                    }
                    codePop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()
            }
            //仅有密码，以及谷歌->2，点击密码，弹引导框，完成谷歌验证后，重置密码
            SafeJumpBroadEnum.PasswordAuth.sourceType -> {
                val changeGuidePop = ChangeGuideBroadPop(
                    context,
                    context.getString(R.string.mine_change_password_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val codePop = AuthCodeBroadPop(
                        context,
                        jumpSource
                    )
                    codePop.transferInvoke = {
                        val mPop = AuthPasswordBroadPop(
                            context,
                            SafeJumpBroadEnum.PasswordChange.sourceType
                        )
                        mPop.showPopupWindow()
                    }
                    codePop.showPopupWindow()

                }
                changeGuidePop.showPopupWindow()
            }
            //仅有密码，以及谷歌->3，点击密保，完成谷歌验证以及密码验证后，去换绑密保
            SafeJumpBroadEnum.QuestionAuth.sourceType -> {
                val codePop = AuthCodeBroadPop(
                    context,
                    jumpSource
                )
                codePop.transferInvoke = {
                    val mPop = AuthPasswordBroadPop(
                        context,
                        jumpSource
                    )
                    mPop.backInvoke = {
                        context.startActivity(
                            Intent(
                                context,
                                SecurityAuthEditBroadActivity::class.java
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
            SafeJumpBroadEnum.GoogleAuth.sourceType -> {
                val mPop = AuthPasswordBroadPop(
                    context,
                    jumpSource
                )
                mPop.backInvoke = {
                    context.startActivity(
                        Intent(context, GoogleAuthStep1BroadActivity::class.java)
                    )
                }
                mPop.showPopupWindow()

            }
            //仅有密码->2，点击密码，弹引导框+引导添加谷歌或密保验证框
            SafeJumpBroadEnum.PasswordAuth.sourceType -> {
                val changeGuidePop = ChangeGuideBroadPop(
                    context,
                    context.getString(R.string.mine_change_password_auth_title),
                    context.getString(R.string.mine_change_auth_des1)
                )
                changeGuidePop.sureInvoke = {
                    val authWayPop = AuthWayBroadPop(context, SafeJumpBroadEnum.PasswordAuth.sourceType)
                    authWayPop.showPopupWindow()
                }
                changeGuidePop.showPopupWindow()
            }
            //仅有密码->3，点击密保，密码验证后，去绑定密保
            SafeJumpBroadEnum.QuestionAuth.sourceType -> {
                val mPop = AuthPasswordBroadPop(
                    context,
                    jumpSource
                )
                mPop.backInvoke = {
                    context.startActivity(
                        Intent(context, SecurityAuthEditBroadActivity::class.java)
                    )
                }
                mPop.showPopupWindow()

            }
        }

    }

}
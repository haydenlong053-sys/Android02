package com.lxkj.aiim.lj.configuration

enum class SafeJumpEnum(val sourceType: Int) {
    None(0),
    GoogleAuth(1),
    PasswordAuth(2),
    QuestionAuth(3),

    PasswordChange(4),
    PasswordChangeSure(5),
    PasswordBind(6),
    PasswordBindSure(7),
    TransferAuthSure(8),
    ChangeMachineAuth(9),
    LoginNewMachine(10),
    LoginForgetPassword(11),


}
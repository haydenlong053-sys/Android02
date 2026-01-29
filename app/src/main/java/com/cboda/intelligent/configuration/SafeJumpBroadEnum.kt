package com.cboda.intelligent.configuration

enum class SafeJumpBroadEnum(val sourceType: Int) {
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

    //钱包模块的密码验证，用于返回一个用户输入的密码来生成密钥的回调
    EthPasswordAuth(12),

}
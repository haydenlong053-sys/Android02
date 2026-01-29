package com.cboda.intelligent.utils

import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.WalletUtils
import java.io.File
import java.security.SecureRandom

object WalletManager {

    // 标准以太坊路径: m/44'/60'/0'/0/0
    private val HARDENED_BIT = Bip32ECKeyPair.HARDENED_BIT
    private val DERIVATION_PATH = intArrayOf(
        44 or HARDENED_BIT,
        60 or HARDENED_BIT,
        0 or HARDENED_BIT,
        0,
        0
    )

    /**
     * 创建新的 BIP39 钱包
     * @param password 钱包密码 (同时作为 BIP39 Passphrase 使用)
     * @param directory 钱包文件保存目录
     * @return 包含文件名和助记词的 Bip39Wallet 对象
     */
    fun createWallet(password: String, directory: File): Bip39Wallet {
        // 1. 生成助记词
        val initialEntropy = ByteArray(16)
        SecureRandom().nextBytes(initialEntropy)
        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)

        // 2. 推导密钥对 (使用密码作为 Passphrase)
        val keyPair = deriveKeyPair(mnemonic, password)

        // 3. 保存 Keystore 文件
        val filename = WalletUtils.generateWalletFile(password, keyPair, directory, false)

        return Bip39Wallet(filename, mnemonic)
    }

    /**
     * 通过助记词获取钱包凭证
     * @param password 钱包密码 (用于 BIP39 Passphrase)
     * @param mnemonic 助记词
     * @return Credentials 凭证对象，如果失败返回 null
     */
    fun getWalletAddressByMnemonic(password: String, mnemonic: String): Credentials? {
        return try {
            val keyPair = deriveKeyPair(mnemonic, password)
            Credentials.create(keyPair)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 导入助记词钱包
     * @param password 钱包密码 (同时作为 BIP39 Passphrase 使用)
     * @param mnemonic 助记词
     * @param directory 钱包文件保存目录
     * @return 包含文件名和助记词的 Bip39Wallet 对象
     */
    fun importWallet(password: String, mnemonic: String, directory: File): Bip39Wallet {
        val keyPair = deriveKeyPair(mnemonic, password)
        val filename = WalletUtils.generateWalletFile(password, keyPair, directory, false)
        return Bip39Wallet(filename, mnemonic)
    }

    /**
     * 导入私钥钱包
     * @param password 钱包密码
     * @param privateKey 私钥 (hex 字符串)
     * @param directory 钱包文件保存目录
     * @return 钱包文件名
     */
    fun importPrivateKey(password: String, privateKey: String, directory: File): String {
        val credentials = Credentials.create(privateKey)
        return WalletUtils.generateWalletFile(password, credentials.ecKeyPair, directory, false)
    }

    /**
     * 加载钱包凭证
     * @param password 钱包密码
     * @param file 钱包文件
     * @return Credentials 凭证对象
     */
    fun loadCredentials(password: String, file: File): Credentials {
        return WalletUtils.loadCredentials(password, file)
    }

    /**
     * 从助记词推导密钥对
     * @param mnemonic 助记词
     * @param passphrase BIP39 密语 (这里复用钱包密码)
     */
    private fun deriveKeyPair(mnemonic: String, passphrase: String): Bip32ECKeyPair {
        // 1. 生成种子 (Seed)
        val seed = MnemonicUtils.generateSeed(mnemonic, passphrase)
        
        // 2. 生成主密钥
        val master = Bip32ECKeyPair.generateKeyPair(seed)
        
        // 3. 推导路径: m/44'/60'/0'/0/0
        return Bip32ECKeyPair.deriveKeyPair(master, DERIVATION_PATH)
    }
}

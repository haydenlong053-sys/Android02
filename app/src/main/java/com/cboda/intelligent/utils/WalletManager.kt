package com.cboda.intelligent.utils

import org.web3j.abi.datatypes.Address
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Credentials
import org.web3j.crypto.Hash
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
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


    fun convertToWei(amount:String): BigInteger? {
        return Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
    }

    fun convertFromWei(weiAmount:String): BigInteger? {
       return Convert.fromWei(weiAmount, Convert.Unit.ETHER).toBigInteger()
    }

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

    // 校验地址是否有效（格式 + checksum）
    fun isValidAddress(address: String?): Boolean {
        if (address == null) return false
        val addr = address.trim()
        if (!addr.startsWith("0x", ignoreCase = true) || addr.length != 42) return false

        val lowerAddr = addr.lowercase()
        val hex = lowerAddr.substring(2)
        if (!hex.matches(Regex("^[0-9a-f]{40}$"))) return false

        // 全小写或全大写 → 有效
        if (addr == lowerAddr || addr == addr.uppercase()) return true

        // 混合大小写 → 严格校验 checksum
        val hash = Hash.sha3(Numeric.hexStringToByteArray(lowerAddr))
        val hashHex = Numeric.toHexStringNoPrefix(hash).lowercase()

        for (i in 0 until 40) {
            val originalChar = addr.substring(2 + i, 3 + i)
            val isUpper = originalChar == originalChar.uppercase()
            val hashDigit = hashHex[i].toString().toInt(16)

            if (isUpper != (hashDigit >= 8)) return false
        }
        return true
    }

    // 自动转为正确的 checksum 版（如果输入有效）
    fun toChecksumAddress(address: String?): String? {
        if (address == null || !isValidAddress(address)) return null

        val lower = address.lowercase().substring(2)
        val hash = Hash.sha3(Numeric.hexStringToByteArray("0x$lower"))
        val hashHex = Numeric.toHexStringNoPrefix(hash).lowercase()

        val sb = StringBuilder("0x")
        for (i in 0 until 40) {
            val c = lower[i]
            val hashDigit = hashHex[i].toString().toInt(16)
            sb.append(if (hashDigit >= 8) c.uppercaseChar() else c)
        }
        return sb.toString()
    }
}

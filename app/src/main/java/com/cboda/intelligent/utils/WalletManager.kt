package com.cboda.intelligent.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.web3j.abi.datatypes.Address
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Credentials
import org.web3j.crypto.Hash
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.Wallet
import org.web3j.crypto.WalletFile
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object WalletManager {

    // DataStore 定义（Preferences 模式）
    private  val Context.walletDataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_wallet_data")
    private  val WALLET_KEY="wallet_master_key"
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

    suspend fun createSecureWallet(context: Context,userPassword:String,directory: File,userId:String): Pair<String, String> {
        // 1. 生成助记词
        val initialEntropy = ByteArray(16)
        SecureRandom().nextBytes(initialEntropy)
        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)

        // 2. 推导密钥对 (使用密码作为 Passphrase)
        val keyPair = deriveKeyPair(mnemonic, userPassword)
        val credentials = Credentials.create(keyPair)
        val address =credentials.address

        // 3. 保存 Keystore 文件
        val filename = WalletUtils.generateWalletFile(userPassword, keyPair, directory, false)
        val keystoreFile = File(directory, filename)
        val keystoreJson = keystoreFile.readText()

        // 立即删除临时文件
        keystoreFile.delete()
        directory.delete()
        // 4. 用 Android Keystore 生成/获取加密密钥
        val encryptionKey = getOrCreateEncryptionKey(WALLET_KEY)
        // 5. 加密 keystore JSON
        val encryptedKeystore = encryptString(keystoreJson, encryptionKey)
        // 额外：加密助记词（仅用于导出功能）
        val encryptedMnemonic = encryptString(mnemonic, encryptionKey)

        context.walletDataStore.edit { prefs->
            prefs[stringPreferencesKey("keystore_${userId}_$address")] = encryptedKeystore
            prefs[stringPreferencesKey("mnemonic_${userId}_$address")] = encryptedMnemonic
        }

         return address to mnemonic

    }

    suspend fun  loadWalletInfoByAddress(context: Context,address: String,userPassword: String,userId: String): Credentials?{
        val encryptedKeystore = context.walletDataStore.data
            .map { prefs -> prefs[stringPreferencesKey("keystore_${userId}_$address")] }
            .first() ?: return null
        val encryptionKey = getOrCreateEncryptionKey(WALLET_KEY)
        val keystoreJson = decryptString(encryptedKeystore, encryptionKey) ?: return null

        return try {
            val loadCredentials = loadCredentialsFromJsonString(userPassword,keystoreJson)
            loadCredentials
        } catch (e: Exception) {
            e.printStackTrace()
            null  // 密码错或解密失败
        }


    }



    // 全局或单例的 ObjectMapper（性能更好）
    private val objectMapper = ObjectMapper()

    private fun loadCredentialsFromJsonString(
        password: String,
        keystoreJson: String
    ): Credentials? {
        return try {
            // 1. JSON → WalletFile 对象
            val walletFile: WalletFile = objectMapper.readValue(keystoreJson, WalletFile::class.java)

            // 2. 用密码解密 WalletFile → ECKeyPair
            val ecKeyPair = Wallet.decrypt(password, walletFile)

            // 3. 创建 Credentials
            Credentials.create(ecKeyPair)
        } catch (e: Exception) {
            e.printStackTrace()
            null  // 密码错、格式错或解密失败
        }
    }

    suspend fun exportMnemonic(
        context: Context,
        address: String,
        userId: String
    ): String? {
        try {
            val encryptionKey = getOrCreateEncryptionKey(WALLET_KEY)

            val encryptedMnemonic = context.walletDataStore.data
                .map { prefs -> prefs[stringPreferencesKey("mnemonic_${userId}_$address")] }
                .first() ?: return null

            return decryptString(encryptedMnemonic, encryptionKey)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
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

    // ====================
// Android Keystore 生成/获取密钥（AES-256-GCM）
    private fun getOrCreateEncryptionKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }

        return keyStore.getKey(alias, null) as SecretKey
    }
    // 加密函数
    private fun encryptString(data: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv  // 随机 IV
        val encrypted = cipher.doFinal(data.toByteArray())
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    // 解密函数
    private fun decryptString(encryptedBase64: String, key: SecretKey): String? {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
            val iv = combined.copyOfRange(0, 12)  // GCM IV 通常 12 字节
            val encrypted = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)  // 128-bit tag
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            null
        }
    }
}

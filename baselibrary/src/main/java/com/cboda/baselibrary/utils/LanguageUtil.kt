package com.cboda.baselibrary.utils

import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageUtil {
    const val SIMPLIFIED = "SimplifiedChinese"//简体中文
    const val TRADITIONAL = "TraditionalChinese"//繁体中文
    const val ENGLISH = "English"//英文
    const val HINDI = "Hindi"//印地语

    const val SPANISH = "Spanish"//西班牙语

    const val FRENCH = "French"//法语
    const val ARABIC = "Arabic"//阿拉伯语
    const val BENGALI = "Bengali"//孟加拉语
    const val RUSSIAN = "Russian"//俄语
    const val PORTUGAL = "Portugal"//葡萄牙语
    const val INDONESIAN = "Indonesian"//印尼语
    const val JAPANESE = "Japanese"//日語
    const val URDU_LANGUAGE = "UrduLanguage"//乌尔都语
    const val GERMAN = "German"//德语

    const val PUNJAB_LANGUAGE = "PunjabLanguage"//旁遮普语
    const val JAVANESE = "Javanese"//爪哇语
    const val FILIPINO = "Filipino"//菲律宾语
    const val VIETNAM = "Vietnam"//越南语
    const val KOREAN = "Korean"//韩语
    const val TURKISH = "Turkish"//土耳其语
    const val ITALIAN = "Italian"//意大利语
    const val THAI = "thai"//泰语
    const val MALAY = "Malay"//马来语
    const val POLISH = "Polish"//波兰语
    const val ROMANIA = "Romania"//罗马尼亚语
    const val LAO = "Lao"//老挝语
    const val MONGOLIAN = "Mongolian"//蒙古语
    const val HAUSA = "Hausa"//豪萨语
    const val SWAHILI_LANGUAGE = "SwahiliLanguage"//斯瓦希里语
    const val BURMESE = "Burmese"//缅甸语
    const val NEPAL = "Nepal"//尼泊尔语
    const val ZULU = "Zulu"//祖鲁语
    const val HEBREW = "Hebrew"//希伯来语
    const val GREEK     = "Greek"//希腊语
    const val UKRAINE = "Ukraine"//乌克兰语


//    fun getAllLanguage(){
//        for (i in 0..34) {
//            val dataListBean = DataListBroadBean()
//            when (i) {
//                0 -> {
//                    dataListBean.name = "简体中文"
//                    dataListBean.value = "zh-CN"
//                    dataListBean.sqlKey = SIMPLIFIED
//                }
//
//                1 -> {
//                    dataListBean.name = "繁體中文"
//                    dataListBean.value = "zh-TW"
//                    dataListBean.sqlKey = TRADITIONAL
//                }
//
//                2 -> {
//                    dataListBean.name = "English"
//                    dataListBean.value = "en-US"
//                    dataListBean.sqlKey = ENGLISH
//                }
//
//                3 -> {
//                    dataListBean.name = "हिन्दी"
//                    dataListBean.value = "hi-IN"
//                    dataListBean.sqlKey = HINDI
//                }
//
//                4 -> {
//                    dataListBean.name = "Español"
//                    dataListBean.value = "es-ES"
//                    dataListBean.sqlKey = SPANISH
//                }
//
//                5 -> {
//                    dataListBean.name = "Français"
//                    dataListBean.value = "fr-FR"
//                    dataListBean.sqlKey = FRENCH
//                }
//
//                6 -> {
//                    dataListBean.name = "العربية"
//                    dataListBean.value = "ar-EG"
//                    dataListBean.sqlKey = ARABIC
//                }
//
//                7 -> {
//                    dataListBean.name = "বাংলা"
//                    dataListBean.value = "bn-IN"
//                    dataListBean.sqlKey = BENGALI
//                }
//
//                8 -> {
//                    dataListBean.name = "русский"
//                    dataListBean.value = "ru-RU"
//                    dataListBean.sqlKey = RUSSIAN
//                }
//
//                9 -> {
//                    dataListBean.name = "Português"
//                    dataListBean.value = "pt-BR"
//                    dataListBean.sqlKey = PORTUGAL
//                }
//
//                10 -> {
//                    dataListBean.name = "Indonesia"
//                    dataListBean.value = "id-ID"
//                    dataListBean.sqlKey = INDONESIAN
//                }
//
//                11 -> {
//                    dataListBean.name = "اردو"
//                    dataListBean.value = "ur-PK"
//                    dataListBean.sqlKey = URDU_LANGUAGE
//                }
//
//                12 -> {
//                    dataListBean.name = "日本語"
//                    dataListBean.value = "ja-JP"
//                    dataListBean.sqlKey = JAPANESE
//                }
//
//                13 -> {
//                    dataListBean.name = "Deutsch"
//                    dataListBean.value = "de-DE"
//                    dataListBean.sqlKey = GERMAN
//                }
//
//                14 -> {
//                    dataListBean.name = "ਪੰਜਾਬੀ"
//                    dataListBean.value = "pa-IN"
//                    dataListBean.sqlKey = PUNJAB_LANGUAGE
//                }
//
//                15 -> {
//                    dataListBean.name = "Jawa"
//                    dataListBean.value = "jv-ID"
//                    dataListBean.sqlKey = JAVANESE
//                }
//
//                16 -> {
//                    dataListBean.name = "Filipino"
//                    dataListBean.value = "in-ID"
//                    dataListBean.sqlKey = FILIPINO
//                }
//
//                17 -> {
//                    dataListBean.name = "Tiếng Việt"
//                    dataListBean.value = "vi-VN"
//                    dataListBean.sqlKey = VIETNAM
//                }
//
//                18 -> {
//                    dataListBean.name = "한국어"
//                    dataListBean.value = "ko-KR"
//                    dataListBean.sqlKey = KOREAN
//                }
//
//                19 -> {
//                    dataListBean.name = "Türkçe"
//                    dataListBean.value = "tr-TR"
//                    dataListBean.sqlKey = TURKISH
//                }
//
//                20 -> {
//                    dataListBean.name = "Italiano"
//                    dataListBean.value = "it-IT"
//                    dataListBean.sqlKey = ITALIAN
//                }
//
//                21 -> {
//                    dataListBean.name = "ไทย"
//                    dataListBean.value = "th-TH"
//                    dataListBean.sqlKey = THAI
//                }
//
//                22 -> {
//                    dataListBean.name = "Bahasa Melayu"
//                    dataListBean.value = "ms-MY"
//                    dataListBean.sqlKey = MALAY
//                }
//
//                23 -> {
//                    dataListBean.name = "Polski"
//                    dataListBean.value = "pl-PL"
//                    dataListBean.sqlKey = POLISH
//                }
//
//                24 -> {
//                    dataListBean.name = "Română"
//                    dataListBean.value = "ro-RO"
//                    dataListBean.sqlKey = ROMANIA
//                }
//
//                25 -> {
//                    dataListBean.name = "ລາວ"
//                    dataListBean.value = "lo-LA"
//                    dataListBean.sqlKey = LAO
//                }
//
//                26 -> {
//                    dataListBean.name = "Монгол"
//                    dataListBean.value = "mn-MN"
//                    dataListBean.sqlKey = MONGOLIAN
//                }
//
//                27 -> {
//                    dataListBean.name = "Hausa"
//                    dataListBean.value = "ha-NG"
//                    dataListBean.sqlKey = HAUSA
//                }
//
//                28 -> {
//                    dataListBean.name = "Kiswahili"
//                    dataListBean.value = "sw-KE"
//                    dataListBean.sqlKey = SWAHILI_LANGUAGE
//                }
//
//                29 -> {
//                    dataListBean.name = "မြန်မာ"
//                    dataListBean.value = "my-MM"
//                    dataListBean.sqlKey = BURMESE
//                }
//
//                30 -> {
//                    dataListBean.name = "नेपाली"
//                    dataListBean.value = "ne-NP"
//                    dataListBean.sqlKey = NEPAL
//                }
//
//                31 -> {
//                    dataListBean.name = "isiZulu"
//                    dataListBean.value = "zu-ZA"
//                    dataListBean.sqlKey = ZULU
//                }
//
//                32 -> {
//                    dataListBean.name = "עברית"
//                    dataListBean.value = "he-IL"
//                    dataListBean.sqlKey = HEBREW
//                }
//
//                33 -> {
//                    dataListBean.name = "Ελληνικά"
//                    dataListBean.value = "el-GR"
//                    dataListBean.sqlKey = GREEK
//                }
//
//                34 -> {
//                    dataListBean.name = "українська"
//                    dataListBean.value = "uk-UA"
//                    dataListBean.sqlKey = UKRAINE
//                }
//            }
//            listBeans.add(dataListBean)
//        }
//    }



    fun initLanguage(application: Application) {
//        com.hjq.language.MultiLanguages.init(application)
    }

    fun getLanguageContext(context: Context?): Context {
//        return com.hjq.language.MultiLanguages.attach(getAppContext(context))
//        val lanList = getLanList(getAppLanguageKey())
//        return updateResources(getAppContext(context), lanList) ?: getAppContext(context)
        return getAppContext(context)
    }

    fun setAppLanguage(key: String, mContext: Context? = null) {
        val lanList = getLanList(key)
        val locale = LocaleListCompat.forLanguageTags(lanList)
        AppCompatDelegate.setApplicationLocales(locale)
//        val local = Locale(lanList)
//        com.hjq.language.MultiLanguages.setAppLanguage(getAppContext(mContext), local)

    }


    private fun getLanList(key: String): String {
        return when (key) {
            TRADITIONAL -> "zh-TW"
            SIMPLIFIED -> "zh-CN"
            else -> "en"
        }
    }

    private fun updateResources(context: Context, lanList: String? = "", mLocale: Locale? = null): Context? {
        return if (TextUtils.isEmpty(lanList) && null == mLocale) {
            null
        } else {
            val locale = mLocale ?: Locale(lanList)
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            context.createConfigurationContext(configuration)
        }
    }


    //    // 设置当前的语种（返回 true 表示需要重启 App）
//    MultiLanguages.setAppLanguage(Context context, Locale locale);
//
//// 获取当前的语种
//    MultiLanguages.getAppLanguage(Context context);
//
//// 跟随系统语种（返回 true 表示需要重启 App）
//    MultiLanguages.clearAppLanguage(Context context);
//    其他 API
//// 获取系统的语种
//    MultiLanguages.getSystemLanguage(Context context);
//// 是否跟随系统的语种
//    MultiLanguages.isSystemLanguage(Context context);
//
//// 对比两个语言是否同一个语种（比如：中文有简体和繁体，英语有美式和英式）
//    MultiLanguages.equalsLanguage(Locale locale1, Locale locale2);
//// 对比两个语言是否同一个语种并且同一个地方的（比如：中国大陆用的中文简体，中国台湾用的中文繁体）
//    MultiLanguages.equalsLanguageAndCountry(Locale locale1, Locale locale2);
//// 注意事项：如果是为了判断两个 Locale 对象是否相同，请使用 equalsLanguageAndCountry 方法来判断，而不是 equalsLanguage 方法，
//// 另外请不要用 Locale 类中自带的 equals 方法来判断，因为它会比较更多的信息，会导致判断错误。
//
//
//// 获取某个语种下的 String
//    MultiLanguages.getLanguageString(Context context, Locale locale, int stringId);
//// 生成某个语种下的 Resources 对象
//    MultiLanguages.generateLanguageResources(Context context, Locale locale);
//
//// 更新 Context 的语种
//    MultiLanguages.updateAppLanguage(Context context);
//// 更新 Resources 的语种
//    MultiLanguages.updateAppLanguage(Resources resources);
//
//// 设置默认的语种（越早设置越好）
//    MultiLanguages.setDefaultLanguage(Locale locale);
//
//// 设置语种变化监听器
//    MultiLanguages.setOnLanguageListener(new OnLanguageListener() {
//
//        @Override
//        public void onAppLocaleChange(Locale oldLocale, Locale newLocale) {
//            Log.d("MultiLanguages", "监听到应用切换了语种，旧语种：" + oldLocale + "，新语种：" + newLocale);
//        }
//
//        @Override
//        public void onSystemLocaleChange(Locale oldLocale, Locale newLocale) {
//            Log.d("MultiLanguages", "监听到系统切换了语种，旧语种：" + oldLocale + "，新语种：" + newLocale + "，是否跟随系统：" + MultiLanguages.isSystemLanguage());
//        }
//    });
}
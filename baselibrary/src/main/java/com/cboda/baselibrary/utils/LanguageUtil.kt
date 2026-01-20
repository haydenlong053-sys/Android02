package com.cboda.baselibrary.utils

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.hjq.language.MultiLanguages
import java.util.Locale

object LanguageUtil {
    const val SIMPLIFIED = "SimplifiedChinese"//简体中文
    const val TRADITIONAL = "TraditionalChinese"//繁体中文

    fun initLanguage(application: Application) {
        MultiLanguages.init(application)
    }

    fun getLanguageContext(context: Context?): Context {
        return MultiLanguages.attach(getAppContext(context))
    }

    fun setAppLanguage(key: String, mContext: Context? = null) {
//        val locale = LocaleListCompat.forLanguageTags("zh-TW")
//        AppCompatDelegate.setApplicationLocales(locale)
        val local = Locale("zh-TW")
        MultiLanguages.setAppLanguage(getAppContext(mContext), local)

    }

//    private fun updateResources(context: Context, locale: Locale): Context {
//        val configuration = context.resources.configuration
//        configuration.setLocale(locale)
//        configuration.setLayoutDirection(locale)
//        return context.createConfigurationContext(configuration)
//    }


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
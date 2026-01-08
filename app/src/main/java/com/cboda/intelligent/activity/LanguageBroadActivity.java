package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.MainBroadActivity;
import com.cboda.intelligent.R;
import com.cboda.intelligent.app.LiXinBroadApp;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.LanguageBroadAdapter;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.configuration.AppBroadConsts;
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil;
import com.cboda.intelligent.utils.AppBroadUtils;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import java.util.ArrayList;
import java.util.List;


public class LanguageBroadActivity extends BaseSimpleBroadActivity {

    private LanguageBroadAdapter languageAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private String language = "";
    private RecyclerView ryLangeuage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.切换语言));

//        Locale current = getResources().getConfiguration().locale;
//        language = current.getLanguage(); // 获取当前系统语言
//        Log.e("TAG", "当前系统语言："+ language );

        languageAdapter = new LanguageBroadAdapter(this, listBeans);
        ryLangeuage.setLayoutManager(new LinearLayoutManager(this));
        ryLangeuage.setAdapter(languageAdapter);
        languageAdapter.setOnItemClickListener(new LanguageBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                for (int i = 0; i < listBeans.size(); i++) {
                    listBeans.get(i).isChecked = false;
                }
                listBeans.get(position).isChecked = true;
                languageAdapter.notifyDataSetChanged();

                MMKVBroadUtils.Companion.saveYuYan(listBeans.get(position).name);
                for (int i = 0; i < AppBroadUtils.Companion.getActivityList().size(); i++) {
                    AppBroadUtils.Companion.getActivityList().get(i).finish();
                }
                if (TextUtils.isEmpty(SharePrefBroadBroadUtil.getString(LiXinBroadApp.Companion.getMContext(), AppBroadConsts.UID, ""))) {
                    Intent intent = new Intent(LiXinBroadApp.Companion.getMContext(), NewEquipmentBroadActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LiXinBroadApp.Companion.getMContext(), MainBroadActivity.class);
                    startActivity(intent);
                }
            }
        });

        listBeans.clear();
        for (int i = 0; i < 35; i++) {
            DataListBroadBean dataListBean = new DataListBroadBean();
            switch (i) {
                case 0://简体中文 values-zh-rCN
                    dataListBean.name = "简体中文";
                    dataListBean.value = "zh-CN";
                    break;
                case 1://繁体中文 values-zh-rTW
                    dataListBean.name = "繁體中文";
                    dataListBean.value = "zh-TW";
                    break;
                case 2://英语 values-en-rUS
                    dataListBean.name = "English";
                    dataListBean.value = "en-US";
                    break;
                case 3://印地语 values-hi-rIN
                    dataListBean.name = "हिन्दी";
                    dataListBean.value = "hi-IN";
                    break;
                case 4://西班牙语 values-es-rES
                    dataListBean.name = "Español";
                    dataListBean.value = "es-ES";
                    break;
                case 5://法语 values-fr-rFR
                    dataListBean.name = "Français";
                    dataListBean.value = "fr-FR";
                    break;
                case 6://阿拉伯语 values-ar-rEG
                    dataListBean.name = "العربية";
                    dataListBean.value = "ar-EG";
                    break;
                case 7://孟加拉语 values-bn-rIN
                    dataListBean.name = "বাংলা";
                    dataListBean.value = "bn-IN";
                    break;
                case 8://俄语 values-ru-rRU
                    dataListBean.name = "русский";
                    dataListBean.value = "ru-RU";
                    break;
                case 9://葡萄牙语 values-pt-rBR
                    dataListBean.name = "Português";
                    dataListBean.value = "pt-BR";
                    break;
                case 10://印尼语 values-id-rID
                    dataListBean.name = "Indonesia";
                    dataListBean.value = "id-ID";
                    break;
                case 11://乌尔都语 values-ur-rPK
                    dataListBean.name = "اردو";
                    dataListBean.value = "ur-PK";
                    break;
                case 12://日本語 values-ja-rJP
                    dataListBean.name = "日本語";
                    dataListBean.value = "ja-JP";
                    break;
                case 13://德语 values-de-rDE
                    dataListBean.name = "Deutsch";
                    dataListBean.value = "de-DE";
                    break;
                case 14://旁遮普语 values-pa-rIN
                    dataListBean.name = "ਪੰਜਾਬੀ";
                    dataListBean.value = "pa-IN";
                    break;
                case 15://爪哇语 values-jv-rID
                    dataListBean.name = "Jawa";
                    dataListBean.value = "jv-ID";
                    break;
                case 16://菲律宾语 values-in-rID
                    dataListBean.name = "Filipino";
                    dataListBean.value = "in-ID";
                    break;
                case 17://越南语 values-vi-rVN
                    dataListBean.name = "Tiếng Việt";
                    dataListBean.value = "vi-VN";
                    break;
                case 18://韩语 values-ko-rKR
                    dataListBean.name = "한국어";
                    dataListBean.value = "ko-KR";
                    break;
                case 19://土耳其语 values-tr-rTR
                    dataListBean.name = "Türkçe";
                    dataListBean.value = "tr-TR";
                    break;
                case 20://意大利语 values-it-rIT
                    dataListBean.name = "Italiano";
                    dataListBean.value = "it-IT";
                    break;
                case 21://泰语 values-th-rTH
                    dataListBean.name = "ไทย";
                    dataListBean.value = "th-TH";
                    break;
                case 22://马来语 values-ms-rMY
                    dataListBean.name = "Bahasa Melayu";
                    dataListBean.value = "ms-MY";
                    break;
                case 23://波兰语 values-pl-rPL
                    dataListBean.name = "Polski";
                    dataListBean.value = "pl-PL";
                    break;
                case 24://罗马尼亚语 values-ro-rRO
                    dataListBean.name = "Română";
                    dataListBean.value = "ro-RO";
                    break;
                case 25://老挝语 values-lo-rLA
                    dataListBean.name = "ລາວ";
                    dataListBean.value = "lo-LA";
                    break;
                case 26://蒙古语 values-mn-rMN
                    dataListBean.name = "Монгол";
                    dataListBean.value = "mn-MN";
                    break;
                case 27://豪萨语 values-ha-rNG
                    dataListBean.name = "Hausa";
                    dataListBean.value = "ha-NG";
                    break;
                case 28://斯瓦希里语 values-sw-rKE
                    dataListBean.name = "Kiswahili";
                    dataListBean.value = "sw-KE";
                    break;
                case 29://缅甸语 values-ms-rMY
                    dataListBean.name = "မြန်မာ";
                    dataListBean.value = "my-MM";
                    break;
                case 30://尼泊尔语 values-ne-rNP
                    dataListBean.name = "नेपाली";
                    dataListBean.value = "ne-NP";
                    break;
                case 31://祖鲁语 values-zu-rZA
                    dataListBean.name = "isiZulu";
                    dataListBean.value = "zu-ZA";
                    break;
                case 32://希伯来语 values-he-rIL
                    dataListBean.name = "עברית";
                    dataListBean.value = "he-IL";
                    break;
                case 33://希腊语 values-el-rGR
                    dataListBean.name = "Ελληνικά";
                    dataListBean.value = "el-GR";
                    break;
                case 34://乌克兰语  values-uk-rUA
                    dataListBean.name = "українська";
                    dataListBean.value = "uk-UA";
                    break;
            }
            listBeans.add(dataListBean);
        }
        boolean ischeck = false;
        for (int i = 0; i < listBeans.size(); i++) {
            if (listBeans.get(i).name.equals(MMKVBroadUtils.Companion.getYuYan())) {
                listBeans.get(i).isChecked = true;
                ischeck = true;
            } else {
                listBeans.get(i).isChecked = false;
            }
        }
        if (!ischeck) {
            listBeans.get(0).isChecked = true;
            MMKVBroadUtils.Companion.saveYuYan(listBeans.get(0).name);
        }

        languageAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_language;
    }

    private void initViews() {
        ryLangeuage = findViewById(R.id.ryLangeuage);
    }
}
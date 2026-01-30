package com.cboda.select.picture.basic;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cboda.select.picture.PictureSelectorFragment;
import com.cboda.select.picture.R;
import com.cboda.select.picture.config.SelectorConfig;
import com.cboda.select.picture.config.SelectorProviders;
import com.cboda.select.picture.immersive.ImmersiveManager;
import com.cboda.select.picture.language.PictureLanguageUtils;
import com.cboda.select.picture.style.PictureWindowAnimationStyle;
import com.cboda.select.picture.style.SelectMainStyle;
import com.cboda.select.picture.utils.StyleUtils;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author：luck
 * @date：2021/11/17 9:59 上午
 * @describe：PictureSelectorSupporterActivity
 */
public class PictureSelectorSupporterActivity extends BaseActivity {
    private SelectorConfig selectorConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSelectorConfig();
        immersive();
        setContentView(R.layout.ps_activity_broad_container);
        setupFragment();
        View viewBottom = findViewById(R.id.viewBottom);
        setNavBarHeightListener(new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(Integer integer) {
                if (integer!=null && integer > 0){
                    ViewGroup.LayoutParams params = viewBottom.getLayoutParams();
                    params.height =integer;
                    viewBottom.setLayoutParams(params);
                    viewBottom.setVisibility(View.VISIBLE);
                }
                return null;
            }
        });
    }

    private void initSelectorConfig() {
        selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
    }

    private void immersive() {
        SelectMainStyle mainStyle = selectorConfig.selectorStyle.getSelectMainStyle();
        int statusBarColor = mainStyle.getStatusBarColor();
        int navigationBarColor = mainStyle.getNavigationBarColor();
        boolean isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack();
        if (!StyleUtils.checkStyleValidity(statusBarColor)) {
            statusBarColor = ContextCompat.getColor(this, R.color.ps_color_grey);
        }
        if (!StyleUtils.checkStyleValidity(navigationBarColor)) {
            navigationBarColor = ContextCompat.getColor(this, R.color.ps_color_grey);
        }
        ImmersiveManager.immersiveAboveAPI23(this, statusBarColor, navigationBarColor, isDarkStatusBarBlack);

    }

    private void setupFragment() {
        FragmentInjectManager.injectFragment(this, PictureSelectorFragment.TAG,
                PictureSelectorFragment.newInstance());
    }

    /**
     * set app language
     */
    public void initAppLanguage() {
        if (selectorConfig != null && !selectorConfig.isOnlyCamera) {
            PictureLanguageUtils.setAppLanguage(this, selectorConfig.language, selectorConfig.defaultLanguage);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initAppLanguage();
    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
//        if (selectorConfig != null) {
//            super.attachBaseContext(PictureContextWrapper.wrap(newBase, selectorConfig.language, selectorConfig.defaultLanguage));
//        } else {
//            super.attachBaseContext(newBase);
//        }
////        super.attachBaseContext(LanguageUtil.INSTANCE.getLanguageContext(newBase));
//    }

    @Override
    public void finish() {
        super.finish();
        if (selectorConfig != null) {
            PictureWindowAnimationStyle windowAnimationStyle = selectorConfig.selectorStyle.getWindowAnimationStyle();
            overridePendingTransition(0, windowAnimationStyle.activityExitAnimation);
        }
    }


}

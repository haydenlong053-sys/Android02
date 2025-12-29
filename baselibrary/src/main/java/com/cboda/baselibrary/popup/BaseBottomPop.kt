package com.cboda.baselibrary.popup

import android.content.Context
import android.view.Gravity
import android.view.animation.Animation
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.Direction
import razerdp.util.animation.TranslationConfig

open class BaseBottomPop(context: Context) : BasePopupWindow(context){
     init {
         popupGravity = Gravity.BOTTOM
         setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)
     }

     override fun onCreateShowAnimation(): Animation? {
         return AnimationHelper.asAnimation()
             .withTranslation(
                 TranslationConfig()
                     .from(Direction.BOTTOM)
                     .to(Direction.IDLE)
             )
             .toShow()
     }

     override fun onCreateDismissAnimation(): Animation? {
         return AnimationHelper.asAnimation()
             .withTranslation(
                 TranslationConfig()
                     .from(Direction.IDLE)
                     .to(Direction.BOTTOM)
             )
             .toShow()
     }
 }
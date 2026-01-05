package com.cboda.agora.ui.ui.praiseEffect

import com.cboda.agora.ui.ui.basic.AUIImageBroadView
import com.cboda.agora.ui.ui.praiseEffect.impI.AUIAbstractPathBroadAnimator
import com.cboda.agora.ui.ui.praiseEffect.impI.AUIPraiseEffectBroadLayout

interface IAUIPraiseBroadEffect {

    fun setOnHearLayoutListener(onHearLayoutListener: AUIPraiseEffectBroadLayout.OnHearLayoutListener?){}

    fun getPraiseEffectView(): AUIImageBroadView?

    fun setPraiseEffectViewIcon(ResId:Int){}

    fun setPraiseEffectViewSize(width:Int,height:Int){}

    fun getAnimator(): AUIAbstractPathBroadAnimator?

    fun setAnimator(animator: AUIAbstractPathBroadAnimator?){}

    fun setDrawableIds(drawableIds: Array<Int>){}

    fun addFavor(){}
}
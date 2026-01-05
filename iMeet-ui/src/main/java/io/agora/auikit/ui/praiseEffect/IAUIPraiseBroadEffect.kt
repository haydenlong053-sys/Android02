package io.agora.auikit.ui.praiseEffect

import io.agora.auikit.ui.basic.AUIImageBroadView
import io.agora.auikit.ui.praiseEffect.impI.AUIAbstractPathBroadAnimator
import io.agora.auikit.ui.praiseEffect.impI.AUIPraiseEffectBroadLayout

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
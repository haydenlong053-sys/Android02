package com.cboda.intelligent.compose.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cboda.intelligent.R
import com.cboda.intelligent.compose.noRippleClickable
import com.cboda.intelligent.compose.theme.AppButtonSolidColors
import com.cboda.intelligent.compose.theme.AppLineColors
import com.cboda.intelligent.compose.theme.AppTextColors
import com.cboda.intelligent.compose.theme.AppTheme

/**
 * 通用的居中标题栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeNormalScreenToolBar(title:String,
                               onBack: () -> Unit,
//                               titleColor:Color= AppTextColors.Primary,
//                               backgroundColor: Color= MaterialTheme.colorScheme.surface,
//                               navigationColor:Color= MaterialTheme.colorScheme.onSurface
                                       titleColor:Color= Color.Black,
                               backgroundColor: Color= Color.White,
                               navigationColor:Color= Color.Black
){
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = titleColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.W500
            )
        },
        navigationIcon = {
            Box(
                modifier = Modifier.size(48.dp).noRippleClickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.wt_back),
                    tint = navigationColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor
        )
    )

}

/**
 * 主色调的按钮 默认是蓝色主色调的
 * 常用于确认等按钮
 */
@Composable
fun ComposeAccentButton(btnText:String, onBtnClick: () -> Unit,
//                        btnTextColor:Color= MaterialTheme.colorScheme.onPrimary,
                        btnTextColor:Color= Color.White,
                        btnTextSize:TextUnit=16.sp,
                        backgroundColor: Color=Color(0xFF3B47DD),
                        ){
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .noRippleClickable(onClick = onBtnClick)
    ) {
        Box(contentAlignment = Alignment.Center,) {
            Text(
                text = btnText,
                fontSize = btnTextSize,
                fontWeight = FontWeight.W500,
                color = btnTextColor
            )
        }
    }
}


@Composable
fun ComposeImageButton(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    btnText:String,


){

}


/**
 * 蓝色线框按钮
 * 常用于取消按钮
 */
@Composable
fun ComposeOutLineButton(btnText:String, onBtnClick: () -> Unit,
//                         btnTextColor:Color= AppLineColors.OutLineNormal,
                         btnTextColor:Color=Color(0xFF3B47DD) ,
                         btnTextSize:TextUnit=16.sp,
//                         backgroundColor: Color= AppButtonSolidColors.OutLineBtnSolid,
                         backgroundColor: Color= Color.Transparent,
                         outLineColor: Color= Color(0xFF3B47DD),
                         outlineWidth: Dp =1.dp
){
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(outlineWidth,outLineColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .noRippleClickable(onClick = onBtnClick)
    ) {
        Box(contentAlignment = Alignment.Center,) {
            Text(
                text = btnText,
                fontSize = btnTextSize,
                fontWeight = FontWeight.W500,
                color = btnTextColor
            )
        }
    }
}

@Preview
@Composable
private  fun ComposeNormalScreenToolbarPreview(){
     ComposeNormalScreenToolBar("This is Title",{})

}
@Preview(name = "暗色模式", uiMode = UI_MODE_NIGHT_YES)
@Composable
private  fun ComposeNormalScreenToolbarDarkThemePreview(){
    AppTheme(true) {
        ComposeNormalScreenToolBar("This is Title",{})
    }

}

@Preview(showBackground = true, widthDp = 200)
@Composable
private  fun AccentButtonPreview(){
     ComposeAccentButton("confirm", onBtnClick = {})
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private  fun OutlineButtonPreview(){
    ComposeOutLineButton("cancel", onBtnClick = {})
}
@Preview(name = "暗色模式", uiMode = UI_MODE_NIGHT_YES, showBackground = true, widthDp = 200, )
@Composable
private  fun OutlineButtonDarkThemePreview(){
    AppTheme(true) {
        ComposeOutLineButton("cancel", onBtnClick = {})
    }
}

@Preview
@Composable
private  fun IconTextButtonPreview(){
     IconTextButton(
         text = stringResource(R.string.wt_scan),
         onClick = {},
         iconResId = R.mipmap.ic_wt_detail_scan,
         iconPosition = IconPosition.Top,
         backgroundColor =  Color(0xFFFCEDD8),
         iconTextSpacing = 8.dp,
         width = 100.dp,
         height = 80.dp,
         contentColor = Color(0xFFD9622C),
         textStyle = TextStyle(
             fontSize = 12.sp,
             fontWeight = FontWeight.W500

         )


     )
}

@Preview
@Composable
private  fun IconTextButtonStartIconPreview(){
    IconTextButton("add wallet",
        onClick = {},
        iconSize = 20.dp,
        iconResId = R.mipmap.ic_wt_btn_add,
        iconTextSpacing = 4.dp,
        height = 46.dp,
        width = 200.dp,
        cornerRadius = 16.dp,
        backgroundColor = Color(0xFF3B47DD)
    )

}
@Preview(name ="无icon的button")
@Composable
private  fun IconTextButtonNonetIconPreview(){
    IconTextButton("add wallet",
        onClick = {},
        height = 46.dp,
        width = 200.dp,
        cornerRadius = 16.dp,
        backgroundColor = Color(0xFF3B47DD)
    )

}

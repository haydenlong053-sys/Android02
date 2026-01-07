package com.cboda.intelligent.compose.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cboda.intelligent.compose.profile.EditAddressScreen
import com.cboda.intelligent.compose.profile.EditInterestsScreen
import com.cboda.intelligent.compose.profile.EditJobScreen

/**
 * 这是一个 Compose 宿主函数，用于在 View 框架中启动整个个人信息编辑流程。
 *
 * @param onExternalBack 当用户需要退出整个 Compose 页面时（例如返回到 Fragment/Activity），调用此回调。
 * 通常实现为 requireActivity().onBackPressedDispatcher.onBackPressed()
 */
@Composable
fun ProfileFlowHost(
    startDestination:String="edit_job",
    profession:String,
    interestList: List<String> =emptyList<String>(),
    onFinalJob: (finalString: String) -> Unit = {},
    onFinalInterest: (finalString: List<String>) -> Unit = {},
    onBack: (() -> Unit)? = {},
    onExternalBack: () -> Unit ={} // <--- 关键修改：接收外部返回回调
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
//
//        // 1. 个人信息主页
//        composable("profile_info") {
//            ProfileInfoScreen(
//                navController = navController,
//                viewModel = viewModel,
//                onExternalBack = onExternalBack // 传递外部返回回调
//            )
//        }

        // 2. 修改职业页面
        composable("edit_job") {
            EditJobScreen(
                navController = navController,
                initialProfession = profession,
                onConfirm = onFinalJob,
                onBack=onBack
            )
        }

        // 2. 修改地区页面
        composable("edit_address") {
            EditAddressScreen(
                navController = navController,
                initialProfession = profession,
                onConfirm = onFinalJob,
                onBack=onBack
            )
        }

        // 3. 修改兴趣页面
        composable("edit_interests") {
            EditInterestsScreen(
                navController = navController,
                initialList=interestList,
                onConfirm = onFinalInterest,
                onBack=onBack
            )
        }
    }
}
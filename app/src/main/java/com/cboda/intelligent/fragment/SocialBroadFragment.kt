package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cboda.intelligent.R
import com.cboda.intelligent.model.SocialBroadCircle
import com.cboda.intelligent.model.SocialContact
import com.cboda.intelligent.viewmodel.SocialViewBroadModel
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.Dp
import com.cboda.chatui.utils.PinyinBroadUtils
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.cboda.chatui.contact.ContactViewBroadModel
import coil.compose.AsyncImage
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.truncateTextWithEllipsis
import com.cboda.intelligent.activity.VerifyBroadActivity
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.goTempTranslateString
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

enum class CircleInteractionBroadMode {
    WithAllSingleSelect, // 有“全部”选项，单选
    ToggleSingleNoAll    // 无“全部”，单选且可取消选择
}

class SocialFragment : Fragment() {
    private val viewModel: SocialViewBroadModel by viewModels()
    private val contactViewModel: ContactViewBroadModel by viewModels()

    private var mGroupInvitedCount by mutableStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val compose = ComposeView(requireContext())
        compose.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        compose.setContent {
            SocialScreen(
                vm = viewModel,
                groupInvitedCount = mGroupInvitedCount,
                onNewFriendClick = { navigateToNewFriends() },
                onNewGroupClick = { navigateToNewGroupInvites() },
                onContactClick = { navigateToFriendInfoPage(it) }
            )
        }
        EventBus.getDefault().register(this)
        return compose
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: AppBroadEvent<*>?) {
        val eventCode = event?.eventCode
        if (eventCode == AppEventBroadCode.EVENT_REFRESH_CONTACT) {
            eLogD("SocialFragment", "收到刷新列表的事件通知")
            viewModel.refresh()
        }

    }

    private fun navigateToNewFriends() {
        JumpBroadUtils.startYanZhengActivity(requireContext(), VerifyBroadActivity.FRIEND_APPLY)
    }

    private fun navigateToNewGroupInvites() {
        JumpBroadUtils.startYanZhengActivity(requireContext(), VerifyBroadActivity.GROUP_INVITED)
    }

    private fun navigateToFriendInfoPage(userId: String) {
        JumpBroadUtils.startHaoYouAddActivity(requireContext(), userId,"2")
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SocialScreen(
    vm: SocialViewBroadModel,
    groupInvitedCount: Int? = 0,
    interactionMode: CircleInteractionBroadMode = CircleInteractionBroadMode.ToggleSingleNoAll,
    onNewFriendClick: () -> Unit = {},
    onNewGroupClick: () -> Unit = {},
    onContactClick: (String) -> Unit = {}
) {
    val state by vm.uiState.collectAsState()
    val listState = remember { LazyListState() }
    val scope = rememberCoroutineScope()

    // 计算每个字母段起始索引，用于滚动
    val indexToPosition = remember(state.groupedByLetter) {
        val mapping = mutableMapOf<Char, Int>()
        // 前置两项：邀请行 + 圈子行，使整页一起滚动
        var index = 3 // 邀请行 + 圈子行 + 解释行
        state.indexLetters.forEach { letter ->
            mapping[letter] = index // header index
            val size = state.groupedByLetter[letter]?.size ?: 0
            index += 1 + size // header + items
        }
        mapping
    }

    Box(Modifier.fillMaxSize()) {
        // 单一列表：整页滚动（邀请行 + 圈子行 + 联系人分组）
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                InviteRow(
                    onFriendClick = onNewFriendClick,
                    onGroupClick = onNewGroupClick,
                    friendBadgeCount = state.newFriendCount,
                    groupBadgeCount = state.newGroupInviteCount
                )
            }
            item {
                CirclesRow(
                    circles = state.circles,
                    selectedTag = state.selectedCircleTag,
                    onSelect = { vm.selectCircle(it) },
                    mode = interactionMode
                )
            }
            item {
                val explanationText = state.selectedCircleTag?.let { tag ->
                    state.circles.firstOrNull { it.characterTags == tag }?.characterExplanation
                } ?: stringResource(R.string.all)
                CharacterExplanation(text = explanationText)
            }
            state.indexLetters.forEach { letter ->
                stickyHeader(key = "header_$letter") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF7F8FA))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = 12.sp,
                            color = Color.Black.copy(0.45f),
                            lineHeight = TextUnit(13f, TextUnitType.Sp),
                            fontWeight = FontWeight.W400
                        )
                    }
                }

                val itemsOfLetter = state.groupedByLetter[letter].orEmpty()
                items(itemsOfLetter, key = { it.id }) { contact ->
                    ContactItem(contact, onContactClick)
                }
            }
        }

        // 右侧字母索引（动态）
        RightIndex(
            letters = state.indexLetters,
            onLetterClick = { ch ->
                vm.onIndexLetterSelected(ch)
                val pos = indexToPosition[ch]
                if (pos != null) {
                    // 平滑滚动到该段（使用协程作用域而非 LaunchedEffect）
                    scope.launch {
                        listState.animateScrollToItem(pos)
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // 中间字母遮罩
        if (state.showOverlay && state.overlayLetter != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .background(Color(0x88000000), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.overlayLetter.toString(),
                        fontSize = 28.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SocialScreenPreview() {
    SocialScreen(
        vm = SocialViewBroadModel(),
        onNewFriendClick = {},
        onNewGroupClick = {}
    )
}

@Preview(showBackground = true, name = "圈子交互：有“全部”")
@Composable
private fun SocialScreenPreview_WithAll() {
    SocialScreen(
        vm = SocialViewBroadModel(),
        interactionMode = CircleInteractionBroadMode.WithAllSingleSelect,
        onNewFriendClick = {},
        onNewGroupClick = {}
    )
}

@Preview(showBackground = true, name = "圈子交互：无“全部”，可取消")
@Composable
private fun SocialScreenPreview_ToggleNoAll() {
    SocialScreen(
        vm = SocialViewBroadModel(),
        interactionMode = CircleInteractionBroadMode.ToggleSingleNoAll,
        onNewFriendClick = {},
        onNewGroupClick = {}
    )
}

@Composable
private fun InviteRow(
    onFriendClick: () -> Unit,
    onGroupClick: () -> Unit,
    friendBadgeCount: Int = 0,
    groupBadgeCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InviteCard(
            iconRes = R.mipmap.ic_new_friend_broad_request,
            title = stringResource(R.string.new_friend),
            onClick = onFriendClick,
            badgeCount = friendBadgeCount,
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
        )
        InviteCard(
            iconRes = R.mipmap.ic_new_group_broad_request,
            title = stringResource(R.string.new_group_invite),
            onClick = onGroupClick,
            badgeCount = groupBadgeCount,
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
        )
    }
}

@Composable
private fun InviteCard(
    iconRes: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val (iconRef, titleRef, badgeRef) = createRefs()

            // 图标：左侧，垂直居中
            androidx.compose.foundation.Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(40.dp)
                    .constrainAs(iconRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )

            // 标题文本：与图标右间距 8dp，垂直居中，内容自适应宽度
            Text(
                text = title,
                fontSize = 15.sp,
                color = Color.Black.copy(0.9f),
                fontWeight = FontWeight.W400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(titleRef) {
                    start.linkTo(iconRef.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
//                    end.linkTo(parent.end)
                    width = Dimension.preferredWrapContent
                }
            )

            // 角标：紧贴标题文本的右上角（位于文本上沿之外）
            if (badgeCount > 0) {
                CounterBadge(
                    count = badgeCount,
                    modifier = Modifier.constrainAs(badgeRef) {
                        // 水平方向：从文本右侧向右贴合少许间隙
                        start.linkTo(titleRef.end, margin = (-6).dp)
                        // 垂直方向：角标底部与文本的顶部对齐，使其落在文本上方
                        bottom.linkTo(titleRef.top)
                        top.linkTo(titleRef.top)
                    }
                )
            }
        }
    }
}

@Composable
private fun CounterBadge(count: Int, modifier: Modifier = Modifier) {
    val display = if (count > 99) "99+" else count.toString()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFA3E3E))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = display,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
private fun CirclesRow(
    circles: List<SocialBroadCircle>,
    selectedTag: String?,
    onSelect: (String?) -> Unit,
    mode: CircleInteractionBroadMode = CircleInteractionBroadMode.ToggleSingleNoAll
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val horizontalPadding = 32.dp // 16dp 左右间距
    val itemSpacing = 8.dp
    val gaps = 3 // 4个item之间的3个间隙
    val minChipWidth = (screenWidth - horizontalPadding - itemSpacing * gaps) / 4

    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        paperColor(30),
////                        paperColor(30),
//                        paperColor(66),
//                        paperColor(100)
//                    )
//                )
//            )
            .padding(horizontal = 16.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (mode) {
            CircleInteractionBroadMode.WithAllSingleSelect -> {
                Chip(
                    text = stringResource(R.string.all),
                    selected = selectedTag.isNullOrBlank(),
                    onClick = { onSelect(null) },
                    minWidth = minChipWidth
                )
                circles.forEach { c ->
                    Chip(
                        text = c.characterTags,
                        selected = selectedTag == c.characterTags,
                        onClick = { onSelect(c.characterTags) },
                        minWidth = minChipWidth
                    )
                }
            }

            CircleInteractionBroadMode.ToggleSingleNoAll -> {
                circles.forEach { c ->
                    val isSelected = selectedTag == c.characterTags
                    //此处文案翻译临时处理，后端接口文案一直是中文
                    Chip(
//                        text = c.characterTags,
                        text = goTempTranslateString(LocalContext.current,c.characterTags),
                        selected = isSelected,
                        onClick = { onSelect(if (isSelected) null else c.characterTags) },
                        minWidth = minChipWidth
                    )
                }
            }
        }
    }
//    Spacer(Modifier.height(8.dp))
}

@Composable
private fun CharacterExplanation(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .background(Color(0xFFF7F8FA))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text =  goTempTranslateString(LocalContext.current,text),
            fontSize = 14.sp,
            color = Color.Black.copy(0.65f),
            lineHeight = TextUnit(19.6f, TextUnitType.Sp),
            fontWeight = FontWeight.W400
        )
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit, minWidth: Dp) {
    val bg = Color.White
    val fg = if (selected) Color(0xFF3B47DD) else Color.Black.copy(alpha = 0.65f)
    Box(
        modifier = Modifier
            .widthIn(min = minWidth)
            .background(bg, RoundedCornerShape(10.dp))
            .then(
                if (selected)
                    Modifier.border(1.dp, Color(0xFFE2E3FF), RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = fg,
            textAlign = TextAlign.Center
        )
    }
}

private fun paperColor(alphaPercent: Int): Color {
    val base = Color(0xFFF7F8FA)
    val alpha = when (alphaPercent) {
        0 -> 0f
        30 -> 0.65f
        66 -> 0.90f
        100 -> 1f
        else -> 1f
    }
    return base.copy(alpha = alpha)
}

@Composable
private fun ContactItem(contact: SocialContact, onContactClick: (String) -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
//        .background(Color(0xFFF7F8FA))
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onContactClick(contact.userId)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 简易头像：首字母圆形背景
            val display = (contact.remarkName?.takeIf { it.isNotBlank() } ?: (contact.nickName
                ?: contact.userId))
            val initial = run {
                val first = display.firstOrNull()
                if (first == null) '#'
                else {
                    val py = PinyinBroadUtils.getPinyin(first.toString())
                    val ch = py.firstOrNull()
                    if (ch != null && ch.isLetter()) ch.uppercaseChar() else '#'
                }
            }
            AsyncImage(
                model = contact.icon,
                contentDescription = stringResource(R.string.contact_avatar),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.mipmap.ic_broad_head),  // 加载中
                error = painterResource(R.mipmap.ic_broad_head),              // 加载失败
                contentScale = ContentScale.Crop
            )

//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .background(Color(0xFFE6E7EB), CircleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(text = initial.toString(), fontSize = 17.sp, color = Color.Black.copy(0.9f))
//            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = display,
                        fontSize = 17.sp,
                        color = Color.Black.copy(0.9f),
                        fontWeight = FontWeight.W500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = true),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "${contact.grade ?: 0}${stringResource(R.string.score_unit)}",
                        fontSize = 14.sp,
                        color = Color(0xffEF7B05),
                        fontWeight = FontWeight.W500
                    )
                    Spacer(Modifier.width(10.dp))
                    if (!contact.characterTags.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3B47DD).copy(0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
//                                text = contact.characterTags,
                                text = goTempTranslateString(LocalContext.current,contact.characterTags),
                                fontSize = 11.sp,
                                color = Color(0xFF3B47DD),
                                overflow= TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.height(4.dp))
//                val sub = listOfNotNull(contact.city, contact.profession).joinToString(" · ")
//                if (sub.isNotBlank()) {
//                    Text(text = sub, fontSize = 14.sp, color = Color(0xFF0F0F25).copy(0.45f))
//                }
                val contactSex =
                    if (contact.sex.isNullOrEmpty()) "" else (if (contact.sex == "1") stringResource(R.string.男2) else stringResource(R.string.女2))

                val sub = if (contact.registerDay == null)
                    listOfNotNull(
                        contact.city,
                        contactSex,
                        contact.profession
                    ).filter { it.isNotBlank() } else listOfNotNull(
                    contact.city,
                    contactSex,
                    contact.profession,
                    "${contact.registerDay}${stringResource(R.string.day2)}"
                ).filter { it.isNotBlank() }
                if (sub.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        sub.forEachIndexed { index, bean ->
                            if (index != 0) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .width(1.dp)
                                        .height(12.dp)
                                        .background(
                                            Color.Black.copy(0.16f),
                                            RoundedCornerShape(16.dp)
                                        )
                                )
                            }
                            Text(
                                text = truncateTextWithEllipsis(bean),
                                fontSize = 14.sp,
                                color = Color(0xFF0F0F25).copy(0.45f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RightIndex(
    letters: List<Char>,
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var colHeight by remember { mutableStateOf(0) }
    Column(
        modifier = modifier
            .padding(end = 6.dp)
            .background(Color.Transparent)
            .onGloballyPositioned { colHeight = it.size.height }
            .pointerInput(letters) {
                // 简单滑动手势：根据触点纵坐标映射到对应字母
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.pressed && colHeight > 0) {
                            val y = change.position.y.coerceIn(0f, colHeight.toFloat())
                            val idx = ((y / colHeight) * letters.size).toInt()
                                .coerceIn(0, letters.lastIndex)
                            onLetterClick(letters[idx])
                        }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        letters.forEach { ch ->
            Box(
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .clickable(
                        indication = null, // 没有点击效果
                        interactionSource = remember { MutableInteractionSource() }) {
                        onLetterClick(
                            ch
                        )
                    }
            ) {
                Text(
                    text = ch.toString(),
                    fontSize = 11.sp,
                    color = Color(0xFF555555),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
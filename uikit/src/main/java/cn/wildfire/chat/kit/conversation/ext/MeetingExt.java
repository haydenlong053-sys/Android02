package cn.wildfire.chat.kit.conversation.ext;

import android.content.Context;
import android.util.Log; // 用于演示回调
import android.view.Gravity; // 如果需要设置 Gravity
import android.view.View;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.bridge.YeHuoCommonBridge;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.widget.popup.OpenMeetingDialog;
import cn.wildfirechat.message.MeetingInviteMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;
import kotlin.Unit; // 导入 Kotlin 的 Unit 类型
// 不需要 FunctionN 接口了，Java Lambda 可以直接匹配

public class MeetingExt extends ConversationExt {

    @ExtContextMenuItem
    public void pickMeeting(View containerView, Conversation conversation) {
        // 假设我们不提供初始标题，使用默认的空字符串
        // String initialMeetingTitle = "";
        // 或者，如果你想提供一个初始标题：

        if (conversation.type == Conversation.ConversationType.Group) {
            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(fragment.getActivity(),"huiyishi","",conversation.target,null);
        } else {
            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(fragment.getActivity(),"huiyishi",conversation.target,"",null);
        }
//        String initialMeetingTitle = conversation.target+" 会议室"; // 示例
//
//        OpenMeetingDialog meetingDialog = new OpenMeetingDialog(
//                containerView.getContext(),
//                initialMeetingTitle, // 新增的 title 参数
//                true,               // initialOpenMic
//                false,              // initialOpenMute
//                // 更新后的回调函数，现在包含 meetingTitle
//                (finalOpenMic, finalOpenMute, finalMeetingTitle) -> {
//
//                    if (conversation != null) {
//
//                        if (conversation.type == Conversation.ConversationType.Group) {
//                            MeetingInviteMessageContent inviteMessageContent =new MeetingInviteMessageContent("122","sss",finalMeetingTitle,"这是描述",finalOpenMic,finalOpenMute);
//                            messageViewModel.sendMessage(conversation,inviteMessageContent);
//
//                        } else {
//
//                        }
//                    }
//                    // Kotlin 的 (..., meetingTitle: String) -> Unit?
//                    // 在 Java lambda 中，如果 Kotlin 端期望 Unit 或 Unit?，返回 Unit.INSTANCE 通常是安全的。
//                    // 如果 Kotlin 端严格需要 null，则返回 null。这里 Unit.INSTANCE 应该没问题。
//                    return Unit.INSTANCE;
//                }
//        );
//
//        // 显示 PopupWindow 的方式保持不变，取决于 BasePopupWindow 的 API
//        // 例如，在屏幕中央显示:
//        meetingDialog.setPopupGravity(Gravity.CENTER); // OpenMeetingDialog 内部已设置
//        meetingDialog.showPopupWindow(); // 调用 BasePopupWindow 的显示方法
//
//        // 或者，如果想相对于 containerView 显示:
//        // meetingDialog.showAsDropDown(containerView);
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_meeting;
    }

    @Override
    public String title(Context context) {
        return "会议室";
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}

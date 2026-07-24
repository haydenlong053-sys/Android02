package com.freddy.im.util

import com.cboda.baselibrary.utils.BusLiveBroadData
import com.cboda.baselibrary.utils.LiveDataBroadBroadBus
import com.freddy.im.http.model.im.GroupBusiExt

@JvmField
val groupDataLiveData2: BusLiveBroadData<GroupBusiExt> = LiveDataBroadBroadBus.getInstance().with("groupData2Update")
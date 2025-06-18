package com.borysmukovnin.quester.dialogs.models

import com.borysmukovnin.quester.models.*
import java.time.Instant

data class DialogNode(
    val id: String,
    val text: String,
    val options: List<DialogOption>,
    val Settings: Options
)

data class DialogOption(
    val text: String,
    val hover: String,
    val nextNodeId: String?,
    val conditions: List<Condition> = emptyList(),
    val actions: List<Action> = emptyList()
)

data class PlayerDialogData(
    val DialogNode: DialogNode,
    val Status: Status,
    val LastStarted: Instant,
    val TimesCompleted: Int,
)
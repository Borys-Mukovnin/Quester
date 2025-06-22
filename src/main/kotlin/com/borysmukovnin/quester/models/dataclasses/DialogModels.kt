package com.borysmukovnin.quester.models.dataclasses

import org.bukkit.entity.Player
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

data class DialogSession(
    val player: Player,
    var currentNode: DialogNode
)
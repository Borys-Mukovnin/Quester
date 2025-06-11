package com.borysmukovnin.quester.dialogs.models

import com.borysmukovnin.quester.models.Action
import com.borysmukovnin.quester.models.Condition

data class DialogNode(
    val id: String,
    val text: String,
    val options: List<DialogOption>
)

data class DialogOption(
    val text: String,
    val nextNodeId: String?,
    val conditions: List<Condition> = emptyList(),
    val actions: List<Action> = emptyList()
)
package com.borysmukovnin.quester.Models

import org.bukkit.entity.Player

data class Quest(
    val Name: String,
    val Description: List<String>,
    val StartConditions: List<String>,
    val Stages: Map<String,Stage>,
    val IsComplete: Boolean = false
)
data class Stage(
    val Name: String,
    val Description: List<String>,
    val Objectives: List<Objective>,
    val Rewards: List<String>
)
interface Objective {
    var ProgressCurrent: Int
    var ProgressGoal: Int
    fun isComplete() : Boolean
}
interface Condition {
    fun isFulfilled(player: Player) : Boolean
}

enum class ItemLocation {
    MAIN_HAND,
    OFF_HAND,
    INVENTORY,
    HOTBAR,
    ARMOR,
    ENDER_CHEST,
    CURSOR,
    ANY
}
enum class Weather {
    RAIN,
    THUNDER,
    ANY
}

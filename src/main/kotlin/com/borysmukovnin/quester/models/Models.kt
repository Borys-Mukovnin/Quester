package com.borysmukovnin.quester.models

import org.bukkit.entity.Player

data class Quest(
    val Name: String,
    val Description: List<String>,
    val StartConditions: List<Condition>,
    val Stages: Map<String,Stage>,
) {
    val IsComplete: Boolean
        get() = Stages.values.all { it.IsComplete }
}
data class Stage(
    val Name: String,
    val Description: List<String>,
    val Objectives: List<Objective>,
    val actions: List<Action>
) {
    val IsComplete: Boolean
        get() = Objectives.all { it.isComplete() }
}

interface Condition {
    fun isMet(player: Player) : Boolean
    fun deepCopy(): Condition
}

interface Action {
    fun execute(player: Player)
    fun deepCopy(): Action
}

interface Objective {
    var ProgressCurrent: Int
    var ProgressGoal: Int
    var Conditions: List<Condition>?
    fun isComplete() : Boolean
    fun deepCopy(): Objective
}

data class Options(
    val Repeatable: Int
)

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

enum class Mode {
    GAIN,
    LOSE
}

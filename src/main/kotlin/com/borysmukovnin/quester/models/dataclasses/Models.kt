package com.borysmukovnin.quester.models.dataclasses

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.Duration

data class Quest(
    val Name: String,
    val Description: List<String>,
    val Options: Options,
    val StartConditions: List<Condition>,
    val StartActions: List<Action>,
    val Stages: Map<String, Stage>,
) {
    val IsComplete: Boolean
        get() = Stages.values.all { it.IsComplete }
}

data class PlayerQuestData(
    val Quest: Quest,
    val Status: Status,
    val LastStarted: Instant,
    val TimesCompleted: Int,
)

data class Stage(
    val Name: String,
    val Description: List<String>,
    val Options: Options,
    val Objectives: List<Objective>,
    val Actions: List<Action>
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
    val Repeatable: Int = 1,
    val Cancelable: Boolean = true,
    val TimeLimit: Duration?,
    val StartDate: Instant?,
    val EndDate: Instant?
)
data class Gui(
    val name: String,
    val size: Int,
    val items: List<GuiItem>
)

data class GuiItem(
    val itemStack: ItemStack,
    val position: List<Int>,
    val leftClick: String?,
    val rightClick: String?,
    val middleClick: String?
)
data class Lang(
    val progressMessage: String,
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

enum class Status {
    ACTIVE, COMPLETED, INACTIVE
}

enum class TravelMode {
    WALK, SPRINT, SWIM, FLY, MOUNT
}


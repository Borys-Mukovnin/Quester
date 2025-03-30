package com.borysmukovnin.quester.Models

import org.bukkit.entity.Player
import org.bukkit.event.Event

data class QuestData(
    val Name: String,
    val QuestDescription: List<String>,
    val StartConditions: List<String>,
    val Stages: MutableMap<String,StageSection>
)
data class StageSection(
    val StageName: String,
    val StageDescription: List<String>,
    val StageObjectives: List<Objective>,
    val StageRewards: List<String>
)

data class ActiveStageSection (
    val Complete: Boolean,
    val Current: Boolean,
    val Objectives: List<Objective>
)

data class ActiveQuest (
    val QuestData: QuestData,
    val ActiveStages: MutableMap<String,ActiveStageSection>
)
interface Objective {
    var Completion: Boolean
    var ProgressCurrent: Int
    var ProgressGoal: Int
}

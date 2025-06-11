package com.borysmukovnin.quester.utils

import com.borysmukovnin.quester.models.Quest
import com.borysmukovnin.quester.models.Stage

fun Quest.deepCopy(): Quest {
    return Quest(
        Name = this.Name,
        Description = this.Description.toList(),
        StartConditions = this.StartConditions.map {it.deepCopy()},
        Stages = this.Stages.mapValues { it.value.deepCopy() }
    )
}
fun Stage.deepCopy(): Stage {
    return Stage(
        Name = this.Name,
        Description = this.Description.toList(),
        Objectives = this.Objectives.map { it.deepCopy() },
        actions = this.actions.map {it.deepCopy()}
    )
}

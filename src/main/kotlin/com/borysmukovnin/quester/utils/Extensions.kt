package com.borysmukovnin.quester.utils

import com.borysmukovnin.quester.models.dataclasses.Options
import com.borysmukovnin.quester.models.dataclasses.Quest
import com.borysmukovnin.quester.models.dataclasses.Stage

fun Quest.deepCopy(): Quest {
    return Quest(
        Name = this.Name,
        Description = this.Description.toList(),
        Options = this.Options,
        StartConditions = this.StartConditions,
        StartActions = this.StartActions,
        Stages = this.Stages.mapValues { it.value.deepCopy() }.toMutableMap()
    )
}
fun Quest.nextIncompleteStage(): Stage? {
    return Stages.values
        .firstOrNull { !it.IsComplete }
}
fun Stage.deepCopy(): Stage {
    return Stage(
        Name = this.Name,
        Description = this.Description.toList(),
        Objectives = this.Objectives.map { it.deepCopy() },
        Options = this.Options,
        Actions = this.Actions
    )
}
fun Options.deepCopy(): Options {
    return Options(
        Repeatable = this.Repeatable,
        Cancelable = this.Cancelable,
        TimeLimit = this.TimeLimit,
        StartDate = this.StartDate,
        EndDate = this.EndDate
    )
}


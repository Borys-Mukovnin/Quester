package com.borysmukovnin.quester.models.objectives

import com.borysmukovnin.quester.models.Objective
import org.bukkit.Location

class GotoObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 1
    private var _goto: Location? = null

    override var ProgressCurrent: Int
        get() = _progressCurrent
        set(value) {
            _progressCurrent = value
        }
    override var ProgressGoal: Int
        get() = _progressGoal
        set(value) {
            _progressGoal = value
        }
    var Goto: Location?
        get() = _goto
        set(value) {
            _goto = value
        }
    override fun isComplete(): Boolean {
        return ProgressCurrent == ProgressGoal
    }
    override fun deepCopy(): Objective {
        val copy = GotoObjective()
        copy.ProgressCurrent = this._progressCurrent
        copy.ProgressGoal = this._progressGoal
        copy.Goto = this._goto?.clone()  // Location has a .clone() method for deep copy
        return copy
    }

}
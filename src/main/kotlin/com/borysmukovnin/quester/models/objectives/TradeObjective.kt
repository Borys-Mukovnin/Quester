package com.borysmukovnin.quester.models.objectives

import com.borysmukovnin.quester.models.dataclasses.Condition
import com.borysmukovnin.quester.models.dataclasses.Objective

class TradeObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 1
    private var conditions: List<Condition>? = null

    override var Conditions: List<Condition>?
        get() = conditions
        set(value) {
            conditions = value
        }

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

    override fun isComplete(): Boolean {
        return ProgressCurrent >= ProgressGoal
    }
    override fun deepCopy(): Objective {
        val copy = TradeObjective()
        copy.ProgressCurrent = this._progressCurrent
        copy.ProgressGoal = this._progressGoal
        return copy
    }

}
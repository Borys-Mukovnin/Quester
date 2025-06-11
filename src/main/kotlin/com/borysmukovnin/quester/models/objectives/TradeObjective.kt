package com.borysmukovnin.quester.models.objectives

import com.borysmukovnin.quester.models.Objective

class TradeObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 0

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
        return ProgressCurrent == ProgressGoal
    }
    override fun deepCopy(): Objective {
        val copy = TradeObjective()
        copy.ProgressCurrent = this._progressCurrent
        copy.ProgressGoal = this._progressGoal
        return copy
    }

}
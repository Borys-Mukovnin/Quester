package com.borysmukovnin.quester.models.objectives

import com.borysmukovnin.quester.models.Objective

class ExpObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 0
    private var _mode: ExpMode? = ExpMode.GAIN

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
    var Mode: ExpMode?
        get() = _mode
        set(value) {
            _mode = value
        }

    override fun isComplete(): Boolean {
        return ProgressCurrent == ProgressGoal
    }
    override fun deepCopy(): Objective {
        val copy = ExpObjective()
        copy.ProgressCurrent = this._progressCurrent
        copy.ProgressGoal = this._progressGoal
        copy.Mode = this._mode
        return copy
    }

}

enum class ExpMode {
    GAIN,
    LOSE
}
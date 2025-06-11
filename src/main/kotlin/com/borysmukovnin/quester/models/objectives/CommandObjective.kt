package com.borysmukovnin.quester.models.objectives

import com.borysmukovnin.quester.models.Objective

class CommandObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 1
    private var _command: String = "say Hello world!"

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

    var Command: String
        get() = _command
        set(value) {
            _command = value
        }
    override fun isComplete(): Boolean {
        return ProgressCurrent == ProgressGoal
    }
    override fun deepCopy(): CommandObjective {
        val copy = CommandObjective()
        copy.ProgressCurrent = this._progressCurrent
        copy.ProgressGoal = this._progressGoal
        copy.Command = this._command
        return copy
    }

}
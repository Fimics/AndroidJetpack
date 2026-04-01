package com.mic.nav.state;

import com.noetix.libcore.utils.KLog;

public class StateMachine {

    private static final String TAG = "StateMachine";
    private IState currentState;

    private StateMachine() {}


    private static class StateMachineHolder {
        private static final StateMachine INSTANCE = new StateMachine();
    }

    public static StateMachine getInstance() {
        return StateMachineHolder.INSTANCE;
    }

    // 设置初始状态
    public StateMachine(IState initialState) {
        this.currentState = initialState;
    }



    // 切换到新的状态
    public void transferToNewState(IState newState) {

        // 如果当前状态就是要切换的状态，直接返回，不执行任何操作
        // 通过类名判断是否相同类型的状态
        if (currentState != null && currentState.getClass() == newState.getClass()) {
            KLog.d(TAG, "Already in state: " + newState.getClass().getSimpleName());
            return;
        }

            if (currentState != null) {
            KLog.d(TAG, "transferToNewState: " + newState.getClass().getSimpleName()+" exit");
            currentState.exit();  // 退出当前状态
        }
        currentState = newState;
        currentState.enter();  // 进入新的状态
        executeState();
    }

    public IState getCurrentState(){
        return currentState;
    }

    // 执行当前状态的逻辑
    public void executeState() {
        if (currentState != null) {
            currentState.execute();
        }
    }
}

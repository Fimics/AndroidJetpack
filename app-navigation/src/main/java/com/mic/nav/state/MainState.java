package com.mic.nav.state;

import android.content.Context;

import com.noetix.libcore.utils.KLog;import com.noetix.robotics.state.BaseState;

public class MainState extends BaseState implements IState{
   private static final String TAG = "MainState";
    private final Context context;

    public MainState(Context context) {
        this.context = context;
    }

    @Override
    public void enter() {
        super.enter();
        KLog.d(TAG, "Entering MainState");
    }

    @Override
    public void execute() {
       super.execute();
       KLog.d(TAG, "Executing MainState");
    }

    @Override
    public void exit() {
      super.exit();
       KLog.d(TAG, "Exiting MainState");
    }
}

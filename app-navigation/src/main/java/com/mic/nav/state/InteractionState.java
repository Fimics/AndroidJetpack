package com.mic.nav.state;

import android.content.Context;
import android.content.Intent;

import com.noetix.libnoetix.IRobotSDKManager;
import com.noetix.robotics.chat.ChatActivity;import com.noetix.robotics.state.BaseState;

public class InteractionState extends BaseState implements IState{
    private final Context context;

    public InteractionState(Context context) {
        this.context = context;
    }

    @Override
    public void enter() {
        Intent intent = new Intent(context, ChatActivity.class);
//        context.startActivity(intent);
        IRobotSDKManager.getInstance().pause(false);
        super.enter();
    }

    @Override
    public void execute() {
       super.execute();
    }

    @Override
    public void exit() {
        IRobotSDKManager.getInstance().pause(false);
        super.exit();
    }
}

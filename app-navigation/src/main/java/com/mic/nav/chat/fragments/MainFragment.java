package com.mic.nav.chat.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.noetix.libcore.utils.KLog;
import com.noetix.libnoetix.IRobotSDKManager;
import com.noetix.robotics.AppConfig;
import com.noetix.robotics.R;
import com.noetix.robotics.chat.ChatAdapter;
import com.noetix.robotics.chat.ChatViewModel;
import com.noetix.robotics.player.NXPlayer;
import com.noetix.robotics.view.ColumnarView;
import com.noetix.robotics.view.HoldDownView;
import com.noetix.robotics.volces.llm.LLMHistory;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;
    private HoldDownView holdDownView;
    private AppCompatButton btnPress;
    private ColumnarView columnarView;
    private boolean isManual = false;
    private boolean isSpeaking = false;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =inflater.inflate(R.layout.fragment_main, container, false);
        holdDownView = view.findViewById(R.id.hold_down_view);
        btnPress = view.findViewById(R.id.btn_press);
        columnarView = view.findViewById(R.id.columnar_view);
        if (isManual) {
            KLog.d(TAG,"onCreateView 手动模式 监听CaeAudio...");
            btnPress.setVisibility(View.VISIBLE);
            handleHoldDownChanged();
        } else {
            holdDownView.setVisibility(View.GONE);
        }
        LLMHistory.getInstance().clean();
        return view;
    }

    private void handleHoldDownChanged() {
        holdDownView.setOnHoldDownListener(new HoldDownView.OnHoldDownListener() {
            @Override
            public void onPress() {
                KLog.d(TAG,"onPress");
                btnPress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRelease() {
                KLog.d(TAG, "onRelease  up cancel");
                btnPress.setVisibility(View.VISIBLE);
                hideColumnarView();
                isSpeaking = false;
            }

            @Override
            public void onLongPress() {
                KLog.d(TAG, "onLongPress");
                isSpeaking = true;
                btnPress.setVisibility(View.GONE);
            }
        });


    }


//    private class CaeAudioObserver implements Observer<CaeAudioEvent> {
//
//        @Override
//        public void onChanged(CaeAudioEvent caeAudioEvent) {
//            if (!isManual) return;
//            byte[] audioData = caeAudioEvent.audioData;
////            KLog.d(TAG,"isSpeaking ->"+isSpeaking);
//            if (isSpeaking) {
//                if (audioData != null) {
////                    KLog.d(TAG,"显示 波形");
//                    columnarView.setVisibility(View.VISIBLE);
//                    byte[] newAudioData = getNewAudioData(audioData);
//                    columnarView.setWaveData(newAudioData, 0);
//                }
//            } else {
////                KLog.d(TAG,"隐藏 波形");
//                columnarView.setVisibility(View.GONE);
//            }
//
//        }
//    }

    private void hideColumnarView() {
        columnarView.setVisibility(View.GONE);
    }

    private final float ratio = 0.3f;

    private byte[] getNewAudioData(byte[] audioData) {
        byte[] newAudioData = new byte[audioData.length];
        for (int i = 0; i < audioData.length; i++) {
            newAudioData[i] = (byte) (audioData[i] * ratio);
        }
        return newAudioData;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView(view);
        setupObservers();
        chatViewModel.init(getViewLifecycleOwner());
        NXPlayer.getInstance().start();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(null); // 禁用默认动画提升性能

        chatAdapter = new ChatAdapter();
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupObservers() {
        chatViewModel.getChatMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.submitList(messages);
            if (!messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
//            KLog.d("nx_asr", "界面更新...");
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        IRobotSDKManager.getInstance().chatMode(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        chatViewModel.cleanup();
        KLog.d(TAG, "interrupt  on pause");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}

package com.mic.rx2.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mic.rx2.R;
import com.mic.rx2.core.DemoItem;
import com.mic.rx2.core.Output;


public class RxDemoFragment extends Fragment {

    private TextView tvOutput;
    private RxDemoViewModel viewModel;

    private final Output output = new Output() {
        @Override public void clear() {
            if (tvOutput != null) tvOutput.setText("");
        }

        @Override public void print(String line) {
            if (tvOutput == null) return;
            tvOutput.append(line + "\n");
            // 滚动到底部
            int scrollAmount = tvOutput.getLayout() == null ? 0
                    : tvOutput.getLayout().getLineTop(tvOutput.getLineCount()) - tvOutput.getHeight();
            if (scrollAmount > 0) tvOutput.scrollTo(0, scrollAmount);
            else tvOutput.scrollTo(0, 0);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rx_demo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rvGrid = view.findViewById(R.id.rvGrid);
        tvOutput = view.findViewById(R.id.tvOutput);
        tvOutput.setMovementMethod(new ScrollingMovementMethod());

        DemoAdapter adapter = new DemoAdapter(this::runDemo);

        GridLayoutManager glm = new GridLayoutManager(requireContext(), 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Header：占满一行（3列），Normal：占1列
                int vt = adapter.getItemViewType(position);
                return vt == 1 ? 3 : 1;
            }
        });

        rvGrid.setLayoutManager(glm);
        rvGrid.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(RxDemoViewModel.class);
        viewModel.getItems().observe(getViewLifecycleOwner(), adapter::submitList);
    }

    private void runDemo(DemoItem item) {
        // Header 行（action==null）不执行
        if (item == null || item.action == null) return;

        output.clear();
        output.print("Category: " + item.category);
        output.print("Operator: " + item.title);
        output.print("----------------------------");

        try {
            item.action.run(output);
        } catch (Exception e) {
            output.print("ERROR: " + e.getMessage());
            Toast.makeText(requireContext(), "Run failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

package com.mic.rx2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mic.rx2.R;
import com.mic.rx2.core.DemoItem;


public class DemoAdapter extends ListAdapter<DemoItem, DemoAdapter.VH> {

    public interface OnItemClick {
        void onClick(DemoItem item);
    }

    private final OnItemClick onItemClick;

    public DemoAdapter(OnItemClick onItemClick) {
        super(DIFF);
        this.onItemClick = onItemClick;
    }

    private static final DiffUtil.ItemCallback<DemoItem> DIFF =
            new DiffUtil.ItemCallback<DemoItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull DemoItem oldItem, @NonNull DemoItem newItem) {
                    // title + category 作为唯一键足够
                    return oldItem.title.equals(newItem.title) && oldItem.category.equals(newItem.category);
                }

                @Override
                public boolean areContentsTheSame(@NonNull DemoItem oldItem, @NonNull DemoItem newItem) {
                    return areItemsTheSame(oldItem, newItem);
                }
            };

    @Override
    public int getItemViewType(int position) {
        DemoItem item = getItem(position);
        return item.action == null ? 1 : 0; // 1=Header, 0=Normal
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new VH(v);
    }


    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DemoItem item = getItem(position);

        if (item.action == null) {
            // Header
            holder.tvTitle.setText(item.title);
            holder.tvCategory.setVisibility(View.GONE);

            // 关键：压缩 Header 高度
            holder.itemView.setMinimumHeight(0);
            holder.itemView.setPadding(
                    dp(holder.itemView, 8),   // left
                    dp(holder.itemView, 6),   // top
                    dp(holder.itemView, 8),   // right
                    dp(holder.itemView, 6)    // bottom
            );

            holder.tvTitle.setTextSize(14); // 小一点

            // Header 样式（你可以自己调）
            holder.itemView.setBackgroundColor(0xFF222222);
            holder.tvTitle.setTextColor(0xFFFFFFFF);
            return;
        }

        // Normal item
        holder.tvTitle.setText(item.title);
        holder.tvCategory.setVisibility(View.VISIBLE);
        holder.tvCategory.setText(item.category);

        // 每一类：不同背景 + 不同字体颜色
        applyStyle(holder, item.category);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(item);
        });
    }

    private void applyStyle(VH holder, String category) {
        int bg;
        int titleColor;
        int subColor;

        switch (category) {
            case "Create":
                bg = 0xFF2D3E50; titleColor = 0xFFFFFFFF; subColor = 0xFFB0C4DE;
                break;
            case "Transform":
                bg = 0xFF4A235A; titleColor = 0xFFFFFFFF; subColor = 0xFFE6D6F2;
                break;
            case "Filter":
                bg = 0xFF145A32; titleColor = 0xFFFFFFFF; subColor = 0xFFB9F6CA;
                break;
            case "Combine":
                bg = 0xFF6E2C00; titleColor = 0xFFFFFFFF; subColor = 0xFFFFE0B2;
                break;
            case "Conditional":
                bg = 0xFF0E6251; titleColor = 0xFFFFFFFF; subColor = 0xFFB2DFDB;
                break;
            case "Aggregating":
                bg = 0xFF1A237E; titleColor = 0xFFFFFFFF; subColor = 0xFFC5CAE9;
                break;
            case "Error":
                bg = 0xFF7F0000; titleColor = 0xFFFFFFFF; subColor = 0xFFFFCDD2;
                break;
            case "Schedulers":
                bg = 0xFF263238; titleColor = 0xFFFFFFFF; subColor = 0xFFCFD8DC;
                break;
            case "Utility":
                bg = 0xFF37474F; titleColor = 0xFFFFFFFF; subColor = 0xFFB0BEC5;
                break;
            default:
                bg = 0xFF444444; titleColor = 0xFFFFFFFF; subColor = 0xFFDDDDDD;
                break;
        }

        holder.itemView.setBackgroundColor(bg);
        holder.tvTitle.setTextColor(titleColor);
        holder.tvCategory.setTextColor(subColor);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }

    private int dp(View v, int dp) {
        return (int) (dp * v.getResources().getDisplayMetrics().density + 0.5f);
    }

}

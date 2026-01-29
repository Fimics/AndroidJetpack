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

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DemoItem item = getItem(position);
        holder.tvTitle.setText(item.title);
        holder.tvCategory.setText(item.category);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(item);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}

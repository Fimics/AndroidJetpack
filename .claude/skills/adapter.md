# /adapter - 创建 RecyclerView Adapter

快速生成 RecyclerView 的 Adapter + ViewHolder + DiffUtil。

## 步骤

1. 确认列表项信息：数据类名、展示字段、交互事件（点击、长按、滑动等）
2. 生成以下代码：
   - **ListAdapter**：继承 `ListAdapter<T, VH>`，内置 DiffUtil
   - **ViewHolder**：使用 ViewBinding 绑定视图
   - **DiffUtil.ItemCallback**：实现 `areItemsTheSame` 和 `areContentsTheSame`
   - **布局 XML**：item_xxx.xml 列表项布局
3. 点击事件通过 lambda 回调传递给外部
4. 如果需要多类型列表，生成 sealed class ViewType + 多个 ViewHolder

## 模板

```kotlin
class XxxAdapter(
    private val onItemClick: (Item) -> Unit
) : ListAdapter<Item, XxxAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemXxxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: ItemXxxBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.apply {
                // bind data
                root.setOnClickListener { onItemClick(item) }
            }
        }
    }

    private class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
    }
}
```
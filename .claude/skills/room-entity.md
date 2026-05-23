# /room-entity - 创建 Room 数据库实体

快速生成 Room Entity + Dao + 相关代码。

## 步骤

1. 确认实体信息：表名、字段列表（名称、类型、是否可空、默认值）
2. 生成以下文件：
   - **Entity** data class：`@Entity`、`@PrimaryKey`、`@ColumnInfo` 注解
   - **Dao** interface：`@Insert`、`@Update`、`@Delete`、`@Query` 基础 CRUD
   - 如果需要，生成 `TypeConverter` 处理复杂类型
3. 在已有的 `AppDatabase` 中注册新 Entity（添加到 `@Database(entities = [...])` 和 abstract dao 方法）
4. Dao 查询方法返回 `Flow<List<T>>` 以支持响应式观察

## 命名规范

- Entity 类名：大驼峰单数（如 `User`、`Device`）
- 表名：小写下划线复数（如 `users`、`devices`）
- Dao 类名：`XxxDao`（如 `UserDao`）
# /api - 创建网络 API 接口

快速生成 Retrofit API 接口 + 数据模型 + Repository 调用层。

## 步骤

1. 确认接口信息：URL、HTTP 方法、请求参数、响应结构
2. 生成以下代码：
   - **Response Model**：Kotlin data class，使用 `@SerializedName` 注解
   - **Retrofit Service Interface**：`@GET` / `@POST` / `@PUT` / `@DELETE` 方法声明
   - **Repository 方法**：封装网络调用，处理异常，返回 `Result<T>` 或 `Flow<T>`
3. 如果是新的 base URL，在 Retrofit 实例中配置
4. 错误处理：统一的网络异常转换

## 模板

```kotlin
// Service
interface XxxService {
    @GET("endpoint")
    suspend fun getData(@Query("key") key: String): Response<XxxResponse>
}

// Repository
class XxxRepository(private val service: XxxService) {
    fun getData(key: String): Flow<Result<XxxData>> = flow {
        val response = service.getData(key)
        if (response.isSuccessful) {
            emit(Result.success(response.body()!!.toData()))
        } else {
            emit(Result.failure(HttpException(response)))
        }
    }
}
```
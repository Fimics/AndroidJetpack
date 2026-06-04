# arch —— 架构基础库

提供四套架构模式的基类与统一的 Base 能力，所有页面应继承本模块对应基类。
详见 [`docs/01-arch.md`](../docs/01-arch.md)。

## 目录

```
com.mic.guide.arch
├── base/   BaseActivity / BaseFragment / BaseViewModel / BaseRepository / BaseApplication
├── mvc/    MvcActivity / MvcController
├── mvp/    MvpActivity / MvpFragment / MvpPresenter / MvpView
├── mvvm/   MvvmActivity / MvvmFragment / MvvmViewModel / LiveDataExt
└── mvi/    MviActivity / MviViewModel / MviContract(Intent/State/Effect)
```

## 依赖方式

业务模块以 `api` 引入，继承 arch 暴露的 appcompat / 生命周期 / 协程等基础能力：

```kotlin
dependencies {
    api(project(":arch"))
}
```

## 用法示例

### MVVM

```kotlin
class HomeViewModel : MvvmViewModel() {
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    fun load() = launchWithLoading {
        _title.value = repo.fetchTitle()   // Loading / 异常已由基类托管
    }
}

class HomeActivity : MvvmActivity<ActivityHomeBinding, HomeViewModel>() {
    override val viewModel: HomeViewModel by viewModels()
    override fun createBinding(inflater: LayoutInflater) = ActivityHomeBinding.inflate(inflater)
    override fun initData() = viewModel.load()
    override fun observe() {
        viewModel.title.observe(this) { binding.tvTitle.text = it }
    }
    override fun onLoading(loading: Boolean) { binding.progress.isVisible = loading }
}
```

### MVI

```kotlin
data class CounterState(val count: Int = 0) : MviState
sealed interface CounterIntent : MviIntent { data object Inc : CounterIntent }
sealed interface CounterEffect : MviEffect { data class Toast(val msg: String) : CounterEffect }

class CounterViewModel : MviViewModel<CounterIntent, CounterState, CounterEffect>(CounterState()) {
    override fun handleIntent(intent: CounterIntent) = when (intent) {
        CounterIntent.Inc -> {
            setState { copy(count = count + 1) }
            sendEffect(CounterEffect.Toast("当前 ${currentState.count}"))
        }
    }
}
```

> MVVM/MVI 的 Fragment 用 `by viewModels()` 需额外引入 `androidx.fragment:fragment-ktx`。

package com.mic.rx2.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mic.rx2.core.DemoItem;
import com.mic.rx2.operators.O1_Create;
import com.mic.rx2.operators.O2_Transform;
import com.mic.rx2.operators.O3_Filter;
import com.mic.rx2.operators.O4_Combine;
import com.mic.rx2.operators.O5_Conditional;
import com.mic.rx2.operators.O6_Aggregating;
import com.mic.rx2.operators.O7_Error;
import com.mic.rx2.operators.O8_Schedulers;
import com.mic.rx2.operators.O9_Utility;

import java.util.ArrayList;
import java.util.List;

public class RxDemoViewModel extends ViewModel {

    private final MutableLiveData<List<DemoItem>> items = new MutableLiveData<>();

    public RxDemoViewModel() {
        items.setValue(buildItems());
    }

    public LiveData<List<DemoItem>> getItems() {
        return items;
    }

    private List<DemoItem> buildItems() {
        List<DemoItem> list = new ArrayList<>();

        // ===============================
        // Create
        // ===============================
        O1_Create create = new O1_Create();
        list.add(header("Create"));
        list.add(new DemoItem("Create", "DemoSubscribe", create::demoSubscribe));
        list.add(new DemoItem("Create", "just()", create::demoJust));
        list.add(new DemoItem("Create", "fromIterable()", create::demoFrom));
        list.add(new DemoItem("Create", "create()", create::demoCreate));
        list.add(new DemoItem("Create", "defer()", create::demoDefer));
        list.add(new DemoItem("Create", "range()", create::demoRange));
        list.add(new DemoItem("Create", "interval()", create::demoInterval));
        list.add(new DemoItem("Create", "timer()", create::demoTimer));
        list.add(new DemoItem("Create", "empty()", create::demoEmpty));
        list.add(new DemoItem("Create", "never()", create::demoNever));
        list.add(new DemoItem("Create", "error()", create::demoError));

        // ===============================
        // Transform
        // ===============================
        O2_Transform transform = new O2_Transform();
        list.add(header("Transform"));
        list.add(new DemoItem("Transform", "map()", transform::demoMap));
        list.add(new DemoItem("Transform", "flatMap()", transform::demoFlatMap));
        list.add(new DemoItem("Transform", "concatMap()", transform::demoConcatMap));
        list.add(new DemoItem("Transform", "switchMap()", transform::demoSwitchMap));
        list.add(new DemoItem("Transform", "scan()", transform::demoScan));
        list.add(new DemoItem("Transform", "buffer()", transform::demoBuffer));
        list.add(new DemoItem("Transform", "window()", transform::demoWindow));
        list.add(new DemoItem("Transform", "groupBy()", transform::demoGroupBy));
        list.add(new DemoItem("Transform", "cast / ofType", transform::demoCastOfType));

        // ===============================
        // Filter
        // ===============================
        O3_Filter filter = new O3_Filter();
        list.add(header("Filter"));
        list.add(new DemoItem("Filter", "filter()", filter::demoFilter));
        list.add(new DemoItem("Filter", "take()", filter::demoTake));
        list.add(new DemoItem("Filter", "takeLast()", filter::demoTakeLast));
        list.add(new DemoItem("Filter", "skip()", filter::demoSkip));
        list.add(new DemoItem("Filter", "skipLast()", filter::demoSkipLast));
        list.add(new DemoItem("Filter", "distinct()", filter::demoDistinct));
        list.add(new DemoItem("Filter", "distinctUntilChanged()", filter::demoDistinctUntilChanged));
        list.add(new DemoItem("Filter", "debounce()", filter::demoDebounce));
        list.add(new DemoItem("Filter", "throttleLast/sample()", filter::demoThrottleSample));
        list.add(new DemoItem("Filter", "ignoreElements()", filter::demoIgnoreElements));

        // ===============================
        // Combine
        // ===============================
        O4_Combine combine = new O4_Combine();
        list.add(header("Combine"));
        list.add(new DemoItem("Combine", "merge()", combine::demoMerge));
        list.add(new DemoItem("Combine", "concat()", combine::demoConcat));
        list.add(new DemoItem("Combine", "zip()", combine::demoZip));
        list.add(new DemoItem("Combine", "combineLatest()", combine::demoCombineLatest));
        list.add(new DemoItem("Combine", "withLatestFrom()", combine::demoWithLatestFrom));
        list.add(new DemoItem("Combine", "startWith()", combine::demoStartWith));
        list.add(new DemoItem("Combine", "amb()", combine::demoAmb));

        // ===============================
        // Conditional
        // ===============================
        O5_Conditional conditional = new O5_Conditional();
        list.add(header("Conditional"));
        // 注意：这里方法顺序严格按你的表：all/any/contains/isEmpty/defaultIfEmpty/switchIfEmpty/takeUntil/skipUntil
        list.add(new DemoItem("Conditional", "all()", conditional::demoAll));
        list.add(new DemoItem("Conditional", "any()", conditional::demoAny));
        list.add(new DemoItem("Conditional", "contains()", conditional::demoContains));
        list.add(new DemoItem("Conditional", "isEmpty()", conditional::demoIsEmpty));
        list.add(new DemoItem("Conditional", "defaultIfEmpty()", conditional::demoDefaultIfEmpty));
        list.add(new DemoItem("Conditional", "switchIfEmpty()", conditional::demoSwitchIfEmpty));
        list.add(new DemoItem("Conditional", "takeUntil()", conditional::demoTakeUntil));
        list.add(new DemoItem("Conditional", "skipUntil()", conditional::demoSkipUntil));

        // ===============================
        // Aggregating
        // ===============================
        O6_Aggregating aggregating = new O6_Aggregating();
        list.add(header("Aggregating"));
        list.add(new DemoItem("Aggregating", "reduce()", aggregating::demoReduce));
        list.add(new DemoItem("Aggregating", "collect()", aggregating::demoCollect));
        list.add(new DemoItem("Aggregating", "count()", aggregating::demoCount));
        list.add(new DemoItem("Aggregating", "toList()", aggregating::demoToList));
        list.add(new DemoItem("Aggregating", "toMap()", aggregating::demoToMap));
        list.add(new DemoItem("Aggregating", "first()", aggregating::demoFirst));
        list.add(new DemoItem("Aggregating", "last()", aggregating::demoLast));
        list.add(new DemoItem("Aggregating", "single()", aggregating::demoSingle));

        // ===============================
        // Error
        // ===============================
        O7_Error error = new O7_Error();
        list.add(header("Error"));
        list.add(new DemoItem("Error", "onErrorReturn()", error::demoOnErrorReturn));
        list.add(new DemoItem("Error", "onErrorResumeNext()", error::demoOnErrorResumeNext));
        list.add(new DemoItem("Error", "retry()", error::demoRetry));
        list.add(new DemoItem("Error", "retryWhen()", error::demoRetryWhen));
        list.add(new DemoItem("Error", "doOnError()", error::demoDoOnError));

        // ===============================
        // Schedulers
        // ===============================
        O8_Schedulers schedulers = new O8_Schedulers();
        list.add(header("Schedulers"));
        list.add(new DemoItem("Schedulers", "subscribeOn()", schedulers::demoSubscribeOn));
        list.add(new DemoItem("Schedulers", "observeOn()", schedulers::demoObserveOn));
        list.add(new DemoItem("Schedulers", "publishOn()", schedulers::demoPublishOn));
        list.add(new DemoItem("Schedulers", "unsubscribeOn()", schedulers::demoUnsubscribeOn));
        list.add(new DemoItem("Schedulers", "delay()", schedulers::demoDelay));
        list.add(new DemoItem("Schedulers", "timeout()", schedulers::demoTimeout));

        // ===============================
        // Utility
        // ===============================
        O9_Utility utility = new O9_Utility();
        list.add(header("Utility"));
        list.add(new DemoItem("Utility", "doOnNext()", utility::demoDoOnNext));
        list.add(new DemoItem("Utility", "doOnSubscribe()", utility::demoDoOnSubscribe));
        list.add(new DemoItem("Utility", "doOnComplete()", utility::demoDoOnComplete));
        list.add(new DemoItem("Utility", "doFinally()", utility::demoDoFinally));
        list.add(new DemoItem("Utility", "doOnDispose/cancel()", utility::demoDoOnCancel));
        list.add(new DemoItem("Utility", "delay()", utility::demoDelay));
        list.add(new DemoItem("Utility", "timeout()", utility::demoTimeout));
        list.add(new DemoItem("Utility", "repeat()", utility::demoRepeat));
        list.add(new DemoItem("Utility", "cache / share", utility::demoCacheShare));

        return list;
    }

    /**
     * Header 的约定：action == null 代表“分类标题行”
     */
    private DemoItem header(String category) {
        return new DemoItem(category, category, null);
    }
}

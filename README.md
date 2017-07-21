# SmoothRefreshLayout
## [English](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/README_EN.md) | 中文
一个高效的Android刷新库，理论上支持所有的视图，比官方的SwipeRefreshLayout更强大且使用方便.    
本开源库的部分代码来自[android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh).    
非常感谢他提供的这么棒的开源项目！    

## 特性:
 - 理论上支持所有的视图，且可根据具体需求高效适配.
 - 支持5种模式,NONE（做为FrameLayout使用）、REFRESH（头部刷新）、LOAD_MORE（底部刷新）、BOTH（头部刷新和底部刷新）、OVER_SCROLL（越界回弹）.
 - 支持嵌套滑动,完整实现了NestedScrollingChild，NestedScrollingParent 接口,玩转CoordinatorLayout.
 - 直接继承自ViewGroup,拥有卓越的性能,支持类FameLayout的特性（Gravity、Margin).
 - 支持自动刷新、自动上拉加载、到底自动加载更多（不推荐，建议使用Adapter实现）.
 - 支持越界回弹.
 - 支持抽屉效果.
 - 支持刷新视图自定样式,STYLE_DEFAULT(默认不改变大小)、STYLE_SCALE(动态改变大小)
 - 支持二级刷新事件（TwoLevelSmoothRefreshLayout）.
 - 支持ListView，GridView，RecyclerView加载更多的平滑滚动.
 - 支持内容视图的Margin,PS:滚动中没有了Margin效果？SmoothRefreshLayout不存在这种问题.
 - 丰富的回调接口和调试信息,可利用现有Api实现丰富的效果.

## 演示程序
下载 [Demo.apk](https://raw.githubusercontent.com/dkzwm/SmoothRefreshLayout/master/apk/demo.apk)    

## 快照
- 测试QQ浏览器样式    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_web_browser_style.gif)

- 测试QQ活动样式    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_style.gif)

- 测试2级刷新    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_two_level_refresh.gif)

- 包含FrameLayout    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_frameLayout.gif)

- 包含TextView     
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_textView.gif)

- 包含ListView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_listView.gif)

- 包含GridView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_gridView.gif)

- 包含RecyclerView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_recyclerView.gif)

- 包含ViewPager    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_viewPager.gif)

- 包含WebView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_webView.gif)

- CoordinatorLayout    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_recyclerView_in_coordinatorLayout.gif)

- 越界回弹模式    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_overScroll.gif)

- 测试嵌套滑动   
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_nested_scroll.gif)

## 使用   
#### Gradle
```
repositories {  
    ...
    maven { url 'https://jitpack.io' }  
}

dependencies {  
    compile 'com.github.dkzwm:SmoothRefreshLayout:1.3.3'
}
```
#### 在Xml中配置
```
<?xml version="1.0" encoding="utf-8"?>
<me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout
	xmlns:android="http://schemas.android.com/apk/res/android"
	xmlns:app="http://schemas.android.com/apk/res-auto"
	android:id="@+id/smoothRefreshLayout"
	android:layout_width="match_parent"
	android:layout_height="match_parent">
	<TextView
		android:id="@+id/textView_"
		android:layout_width="match_parent"
		android:layout_height="match_parent"/>
</me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout>
```

####  Java代码配置
```
SmoothRefreshLayout refreshLayout = (SmoothRefreshLayout)findViewById(R.id.smoothRefreshLayout);
refreshLayout.setMode(SmoothRefreshLayout.MODE_BOTH);
refreshLayout.setHeaderView(new ClassicHeader(this));
refreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
	@Override
	public void onRefreshBegin(boolean isRefresh) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshLayout.refreshComplete();
			}
		}, 4000);
	}
});
```

#### 自定义刷新视图
##### 接口定义
```
public interface IRefreshView {    

    byte TYPE_HEADER = 0;
    byte TYPE_FOOTER = 1;

    byte STYLE_DEFAULT = 0;
    byte STYLE_SCALE = 1;

    /**
     * 返回是头部视图还是尾部视图
     */
    int getType();

    /**
     * 一般情况都是View实现本接口，所以返回this;
     */
    View getView();

    /**
     * 获取视图样式，现支持2种样式，默认样式和缩放样式。
     */
    int getStyle();

    /**
     * 获取视图的自定义高度，当视图样式为STYLE_SCALE时，必须返回一个确切且大于0的值
     */
    int getCustomHeight();

    /**
     * 手指离开屏幕
     */
    void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator);

    /**
     * 重置视图
     */
    void onReset(SmoothRefreshLayout layout);

    /**
     * 重新配置视图，准备刷新
     */
    void onRefreshPrepare(SmoothRefreshLayout layout);

    /**
     * 开始刷新
     */
    void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator);

    /**
     * 刷新完成
     */
    void onRefreshComplete(SmoothRefreshLayout layout,boolean isSuccessful);

    /**
     * 当头部或者尾部视图发生位置变化
     */
    void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator);

}
```

##### 添加自定义刷新视图
- 代码添加
```    
    setHeaderView(@NonNull IRefreshView header);
    setFooterView(@NonNull IRefreshView footer);
```    

- 请直接写入Xml文件,SmoothRefreshLayout会根据添加的View是否是实现了IRefreshView接口进行判断
 
#### Xml属性 
##### SmoothRefreshLayout 自身配置
|名称|类型|描述|
|:---:|:---:|:---:|
|sr_mode|enum|模式设置（默认:`none`）|
|sr_content|integer|指定内容视图的资源ID|
|sr_resistance|float|刷新视图的移动阻尼（默认:`1.65f`）|
|sr_resistance_of_pull_up|float|Footer视图的移动阻尼（默认:`1.65f`）|
|sr_resistance_of_pull_down|float|Header视图的移动阻尼（默认:`1.65f`）|
|sr_ratio_of_refresh_height_to_refresh|float|触发刷新时位置占刷新视图的高度比（默认:`1.1f`）|
|sr_ratio_of_header_height_to_refresh|float|触发刷新时位置占Header视图的高度比（默认:`1.1f`）|
|sr_ratio_of_footer_height_to_refresh|float|触发加载更多时位置占Footer视图的高度比（默认:`1.1f`）|
|sr_offset_ratio_to_keep_refresh_while_Loading|float|刷新中保持视图位置占刷新视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果|
|sr_offset_ratio_to_keep_header_while_Loading|float|刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果|
|sr_offset_ratio_to_keep_footer_while_Loading|float|刷新中保持视图位置占Footer视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果|
|sr_can_move_the_max_ratio_of_refresh_height|float|最大移动距离占刷新视图的高度比（默认:`0f`，表示不会触发）|
|sr_can_move_the_max_ratio_of_header_height|float|最大移动距离占Header视图的高度比（默认:`0f`，表示不会触发）|
|sr_can_move_the_max_ratio_of_footer_height|float|最大移动距离占Footer视图的高度比（默认:`0f`，表示不会触发）|
|sr_duration_to_close_of_refresh|integer|指定收缩刷新视图到起始位置的时长（默认:`500`）|
|sr_duration_to_close_of_header|integer|指定收缩Header视图到起始位置的时长（默认:`500`）|
|sr_duration_to_close_of_footer|integer|指定收缩Footer视图到起始位置的时长（默认:`500`）|
|sr_duration_of_back_to_refresh_height|integer|收缩刷新视图到触发刷新位置的时长（默认:`200`）|
|sr_duration_of_back_to_header_height|integer|收缩刷新视图到触发Header刷新位置的时长（默认:`200`）|
|sr_duration_of_back_to_footer_height|integer|收缩刷新视图到触发Footer刷新位置的时长（默认:`200`）|
|sr_enable_pin_content|boolean|固定内容视图（默认:`false`）|
|sr_enable_keep_refresh_view|boolean|刷新中保持视图停留在所设置的应该停留的位置（默认:`true`）|
|sr_enable_pull_to_refresh|boolean|拉动刷新,下拉或者上拉到触发刷新位置即立即触发刷新（默认:`false`）|
|sr_enable_over_scroll|boolean|越界回弹（默认:`true`）,使用者需要自己设置内容视图的 `overScrollMode` 为 `never` 才能达到最优效果|

##### SmoothRefreshLayout包裹内部其他View支持配置
|名称|类型|描述|
|:---:|:---:|:---:|
|sr_layout_gravity|flag|指定其它被包裹视图的对齐属性(非content view、非refresh view)|

#### Java属性设置方法
|名称|参数|描述|
|:---:|:---:|:---:|
|setHeaderView|IRefreshView|配置头部视图|
|setFooterView|IRefreshView|配置尾部视图|
|setContentView|View|配置内容视图|
|setDisableWhenHorizontalMove|boolean|内部视图含有横向滑动视图(例如ViewPager)时需设置改属性为ture（默认:`false`）|
|setEnableNextPtrAtOnce|boolean|刷新完成即可再次刷新|
|setResistance|float|刷新视图的移动阻尼（默认:`1.65f`）|
|setResistanceOfPullUp|float|Footer视图的移动阻尼（默认:`1.65f`）|
|setResistanceOfPullDown|float|Header视图的移动阻尼（默认:`1.65f`）|
|setRatioOfRefreshViewHeightToRefresh|float|触发刷新时位置占刷新视图的高度比（默认:`1.1f`）|
|setRatioOfHeaderHeightToRefresh|float|触发刷新时位置占Header视图的高度比（默认:`1.1f`）|
|setRatioOfFooterHeightToRefresh|float|触发加载更多时位置占Footer视图的高度比（默认:`1.1f`）|
|setOffsetRatioToKeepRefreshViewWhileLoading|float|刷新中保持视图位置占刷新视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果|
|setOffsetRatioToKeepHeaderWhileLoading|float|刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果|
|setOffsetRatioToKeepFooterWhileLoading|float|刷新中保持视图位置占Footer视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果|
|setCanMoveTheMaxRatioOfRefreshViewHeight|float|最大移动距离占刷新视图的高度比（默认:`0f`，表示不会触发）|
|setCanMoveTheMaxRatioOfHeaderHeight|float|最大移动距离占Header视图的高度比（默认:`0f`，表示不会触发）|
|setCanMoveTheMaxRatioOfFooterHeight|float|最大移动距离占Footer视图的高度比（默认:`0f`，表示不会触发）|
|setDurationToClose|int|指定收缩刷新视图到起始位置的时长（默认:`500`）|
|setDurationToCloseHeader|int|指定收缩Header视图到起始位置的时长（默认:`500`）|
|setDurationToCloseFooter|int|指定收缩Footer视图到起始位置的时长（默认:`500`）|
|setDurationOfBackToRefreshViewHeight|integer|收缩刷新视图到触发刷新位置的时长（默认:`200`）|
|setDurationOfBackToHeaderHeight|integer|收缩刷新视图到触发Header刷新位置的时长（默认:`200`）|
|setDurationOfBackToFooterHeight|integer|收缩刷新视图到触发Footer刷新位置的时长（默认:`200`）|
|setEnablePinContentView|boolean|固定内容视图（默认:`false`）|
|setEnabledPullToRefresh|boolean|拉动刷新,下拉或者上拉到触发刷新位置即立即触发刷新（默认:`false`）|
|setEnableOverScroll|boolean|越界回弹（默认:`true`）,使用者需要自己设置内容视图的 `overScrollMode` 为 `never` 才能达到最优效果|
|setEnabledInterceptEventWhileLoading|boolean|刷新中拦截不响应触摸操作（默认:`false`）|
|setEnableHeaderDrawerStyle|boolean|Header抽屉样式,即Header视图在内容视图下面（默认:`false`）|
|setEnableFooterDrawerStyle|boolean|Footer抽屉样式,即Footer视图在内容视图下面（默认:`false`）|
|setDisablePerformRefresh|boolean|关闭触发Header刷新（默认:`false`）|
|setDisablePerformLoadMore|boolean|关闭触发Footer刷新（默认:`false`）|
|setDisableRefresh|boolean|禁用Header刷新（默认:`false`）|
|setDisableLoadMore|boolean|禁用Footer刷新（默认:`false`）|
|setEnableKeepRefreshView|boolean|刷新中保持视图停留在所设置的应该停留的位置（默认:`true`）|
|setEnableScrollToBottomAutoLoadMore|boolean|到底部自动加载（默认:`false`）|
|setEnablePinRefreshViewWhileLoading|boolean|固定刷新视图在所设置的应该停留的位置，并且不响应移动，即Material样式（默认:`false`）,设置前提是开启了`setEnablePinContentView`和`setEnableKeepRefreshView`2个选项，否则运行时会抛出异常|

#### 回调
|名称|参数|描述|
|:---:|:---:|:---:|
|setOnRefreshListener|T extends OnRefreshListener|刷新事件监听回调|
|addOnUIPositionChangedListener|OnUIPositionChangedListener|添加视图位置变化的监听回调|
|removeOnUIPositionChangedListener|OnUIPositionChangedListener|移除视图位置变化的监听回调|
|setOnLoadMoreScrollCallback|OnLoadMoreScrollCallback|Footer完成刷新后进行平滑滚动的回调|
|setOnChildScrollUpCallback|OnChildScrollUpCallback|检查内容视图是否在顶部的回调（SmoothRefreshLayout内部`canChildScrollUp()`方法）|
|setOnChildScrollDownCallback|OnChildScrollDownCallback|检查内容视图是否在底部的回调（SmoothRefreshLayout内部`canChildScrollDown()`方法）|
|setOnHookHeaderRefreshCompleteCallback|OnHookUIRefreshCompleteCallBack|设置Header刷新完成的Hook回调，可实现延迟完成刷新|
|setOnHookFooterRefreshCompleteCallback|OnHookUIRefreshCompleteCallBack|设置Footer刷新完成的Hook回调，可实现延迟完成刷新|

#### 其它
|名称|参数|描述|
|:---:|:---:|:---:|
|debug|boolean|Debug开关|
|refreshComplete|无参|刷新完成,且设置最后一次刷新状态为成功|
|refreshComplete|boolean|刷新完成,参数:设置最后一次刷新是否刷新成功|
|refreshComplete|boolean,long|刷新完成,参数1:设置最后一次刷新是否刷新成功,参数2:设置延迟重置刷新状态的时间（会先触发刷新视图的刷新完成回调，但在延迟的时间内库实际上状态仍是刷新状态）|
|refreshComplete|long|刷新完成,且设置最后一次刷新状态为成功,参数:设置延迟重置刷新状态的时间（会先触发刷新视图的刷新完成回调，但在延迟的时间内库实际上状态仍是刷新状态）|
|setLoadingMinTime|long|设置开始刷新到结束刷新的最小时间差(默认:`500`),参数:时间差|
|autoRefresh|无参|自动触发Header刷新,立即触发刷新事件并滚动到触发Header刷新位置|
|autoRefresh|boolean|自动触发Header刷新,参数:是否立即触发刷新事件,会滚动到触发Header刷新位置|
|autoRefresh|boolean,boolean|自动触发Header刷新,参数1:是否立即触发刷新事件,参数2:是否滚动到触发Header刷新位置|
|autoLoadMore|无参|自动触发Footer刷新,立即触发刷新事件并滚动到触发Footer刷新位置|
|autoLoadMore|boolean|自动触发Footer刷新,参数:是否立即触发刷新事件,会滚动到触发Footer刷新位置|
|autoLoadMore|boolean,boolean|自动触发Footer刷新,参数1:是否立即触发刷新事件,参数2:是否滚动到触发Footer刷新位置|
|setLoadMoreScrollTargetView|View|设置Footer移动时,响应移动事件的内容视图,例如在SmoothRefreshLayout中有一个CoordinatorLayout,CoordinatorLayout中有AppbarLayout、RecyclerView等，加载更多时希望被移动的视图为RecyclerView而不是CoordinatorLayout,那么设置RecyclerView为TargetView即可|

## Thanks
- [liaohuqiu android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh)    
- [pnikosis material-progress](https://github.com/pnikosis/materialish-progress)      

## License

	MIT License

	Copyright (c) 2017 dkzwm
	Copyright (c) 2015 liaohuqiu.net

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
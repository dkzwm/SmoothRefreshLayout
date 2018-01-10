# SmoothRefreshLayout

[![Release](https://jitpack.io/v/com.github.dkzwm/SmoothRefreshLayout.svg)](https://jitpack.io/#dkzwm/SmoothRefreshLayout)
[![Methods](https://img.shields.io/badge/Methods%20%7C%20Size-732%20%7C%2073%20KB-e91e63.svg)](http://www.methodscount.com/?lib=com.github.dkzwm.SmoothRefreshLayout%3Acore%3A1.6.1.3)
[![MinSdk](https://img.shields.io/badge/MinSdk-11-blue.svg)](https://developer.android.com/about/versions/android-3.0.html)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/LICENSE)

## [English](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/README_EN.md) | 中文
一个高效的Android刷新库，理论上支持所有的视图，比官方的SwipeRefreshLayout更强大且使用方便.    
本开源库的部分代码来自[android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh).    
非常感谢他提供的这么棒的开源项目！    

## 特性:
 - 理论上支持所有的视图,且可根据具体需求高效适配.
 - 支持多点触摸.
 - 支持嵌套滑动,完整实现了NestedScrollingChild，NestedScrollingParent 接口,玩转CoordinatorLayout.
 - 直接继承自ViewGroup,拥有卓越的性能,支持类FameLayout的特性（Gravity、Margin).
 - 支持自动刷新、自动上拉加载、到底自动加载更多（不推荐，建议使用Adapter实现，可自定义到底判断逻辑回调实现预加载更多）.
 - 支持越界回弹.
 - 支持刷新视图自定样式:STYLE_DEFAULT(默认不改变大小)、STYLE_SCALE(动态改变大小，一直会重测量并布局，所以性能会有损失)、STYLE_PIN(不会改变视图大小，固定在顶部或者底部)、STYLE_FOLLOW_SCALE(先纵向跟随移动并且不改变视图大小，大于视图高度后动态改变视图大小且性能会有损失)、STYLE_FOLLOW_PIN(不会改变视图大小，先纵向跟随移动，大于视图高度后固定)、STYLE_FOLLOW_CENTER(不会改变视图大小，先纵向跟随移动，大于视图高度后让视图保持在移动的距离中心点).
 - 支持二级刷新事件（TwoLevelSmoothRefreshLayout）,PS:淘宝二楼、京东活动.
 - 支持横向刷新(HorizontalSmoothRefreshLayout).
 - 支持ListView、GridView、RecyclerView加载更多的同步平滑滚动.
 - 支持多状态视图:STATE_CONTENT(默认状态)、STATE_ERROR(异常状态)、STATE_EMPTY(空状态)、STATE_CUSTOM(自定义状态).
 - 支持手势:同步Fling(刷新视图仍可见的情况下,会先回滚隐藏刷新视图,而后向下传递Fling手势).
 - 丰富的回调接口和调试信息,可利用现有Api实现丰富的效果.

## 演示程序
下载 [Demo.apk](https://raw.githubusercontent.com/dkzwm/SmoothRefreshLayout/master/apk/demo.apk)    
## 更新日志
#### 老版本升级务必查看
 [更新日志](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/ext/UPDATE.md) 
## 快照
- 测试横向刷新（1.5.0版本添加）    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_horizontal_refresh.gif)

- 测试横向RecyclerView刷新（1.5.0版本添加）    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_horizontal_recyclerView.gif)

- 测试多方向布局下的刷新    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_multi_direction_views.gif)

- 测试QQ浏览器样式    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_web_browser_style.gif)

- 测试QQ活动样式    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_style.gif)

- 测试2级刷新    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_two_level_refresh.gif)
 
- 包含ListView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_listView.gif)

- 包含GridView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_gridView.gif)

- 包含RecyclerView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_recyclerView.gif)

- 包含ViewPager    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_viewPager.gif)

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
    //核心基础库，包含绝大多数功能，扩展库必须依赖本库（从1.6.0版本开始Core库不再自带刷新视图实现，只包含核心功能）
    compile 'com.github.dkzwm.SmoothRefreshLayout:core:1.6.1.3'
    //默认Classic样式的刷新视图实现库(从1.6.0版本才有，是从老版本的Core库中拆分出来的库)
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-classic:1.6.1.3'
    //默认Material样式的刷新视图实现库(从1.6.0版本才有，是从老版本的Core库中拆分出来的库)
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-material:1.6.1.3'
    //工具类库，带有一些快捷配置工具（自动滚动刷新工具，快速设置AppBarLayout工具）
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-utils:1.6.1.3'
    //扩展支持二级刷新库
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-two-level:1.6.1.3'
    //扩展支持横向刷新库
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-horizontal:1.6.1.3'
}
```
#### 在Xml中配置
```
<?xml version="1.0" encoding="utf-8"?>
<me.dkzwm.widget.srl.SmoothRefreshLayout
	xmlns:android="http://schemas.android.com/apk/res/android"
	xmlns:app="http://schemas.android.com/apk/res-auto"
	android:id="@+id/refreshLayout"
	android:layout_width="match_parent"
	android:layout_height="match_parent">
	<TextView
		android:layout_width="match_parent"
		android:layout_height="match_parent"/>
</me.dkzwm.widget.srl.SmoothRefreshLayout>
```
####  Java代码配置
```
SmoothRefreshLayout refreshLayout = (SmoothRefreshLayout)findViewById(R.id.smoothRefreshLayout);
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
public interface IRefreshView <T extends IIndicator> {    

    byte TYPE_HEADER = 0;
    byte TYPE_FOOTER = 1;

    byte STYLE_DEFAULT = 0;
    byte STYLE_SCALE = 1;
    byte STYLE_PIN = 2;
    byte STYLE_FOLLOW_SCALE = 3;
    byte STYLE_FOLLOW_PIN = 4;
    byte STYLE_FOLLOW_CENTER = 5;

    /**
     * 返回是头部视图还是尾部视图;
     */
    int getType();

    /**
     * 一般情况都是View实现本接口，所以返回this;
     */
    View getView();

    /**
     * 获取视图样式，自1.4.8版本后支持6种样式，STYLE_DEFAULT、STYLE_SCALE、STYLE_PIN、STYLE_FOLLOW_SCALE、STYLE_FOLLOW_PIN、STYLE_FOLLOW_CENTER;
     */
    int getStyle();

    /**
     * 获取视图的自定义高度，当视图样式为STYLE_SCALE和STYLE_FOLLOW_SCALE时，必须返回一个确切且大于0的值，使用横向刷新库时，该属性实际应该返回的是视图的宽度;
     * 自1.6.1版本开始，如果想要当前视图铺满布局即MATCH_PARENT，那么支持返回ViewGroup.LayoutParams.MATCH_PARENT对应的值即`-1`;
     */
    int getCustomHeight();

    /**
     * 手指离开屏幕;
     */
    void onFingerUp(SmoothRefreshLayout layout, T indicator);

    /**
     * 重置视图;
     */
    void onReset(SmoothRefreshLayout layout);

    /**
     * 重新配置视图，准备刷新;
     */
    void onRefreshPrepare(SmoothRefreshLayout layout);

    /**
     * 开始刷新;
     */
    void onRefreshBegin(SmoothRefreshLayout layout, T indicator);

    /**
     * 刷新完成;
     */
    void onRefreshComplete(SmoothRefreshLayout layout,boolean isSuccessful);

    /**
     * 当头部或者尾部视图发生位置变化;
     */
    void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, T indicator);

    /**
     * 当头部或者尾部视图仍然处于处理事务中，这时候移动其他刷新视图则会调用该方法;
     * 在1.4.6版本新加入;
     */
    void onPureScrollPositionChanged(SmoothRefreshLayout layout, byte status, T indicator);
}
```

##### 添加自定义刷新视图
- 全局静态代码构造    
```    
        SmoothRefreshLayout.setDefaultCreator(new IRefreshViewCreator() {
            @Override
            public void createHeader(SmoothRefreshLayout layout) {
                ClassicHeader header = new ClassicHeader(layout.getContext());
                layout.setHeaderView(header);
            }

            @Override
            public void createFooter(SmoothRefreshLayout layout) {
                ClassicFooter footer = new ClassicFooter(layout.getContext());
                layout.setFooterView(footer);
            }
        });
```   

- 动态代码添加   
```    
        ClassicHeader header = new ClassicHeader(mRefreshLayout.getContext());
        mRefreshLayout.setHeaderView(header);
        ClassicFooter footer = new ClassicFooter(mRefreshLayout.getContext());
        mRefreshLayout.setFooterView(footer);
```    

- 请直接写入Xml文件,SmoothRefreshLayout会根据添加的View是否是实现了IRefreshView接口进行判断
 
#### 实现类QQ下拉阻尼效果
 ```
        mRefreshLayout.setIndicatorOffsetCalculator(new IIndicator.IOffsetCalculator() {
            @Override
            public float calculate(@IIndicator.MovingStatus int status, int currentPos, float offset) {
                if (status == IIndicator.MOVING_HEADER) {
                    if (offset < 0) {
                        //如果希望拖动缩回时类似QQ一样没有阻尼效果，阻尼效果只存在于下拉则可以在此返回offset
                        //如果希望拖动缩回时类似QQ一样有阻尼效果，那么请注释掉这个判断语句
                        return offset;
                    }
                    return (float) Math.pow(Math.pow(currentPos / 2, 1.28d) + offset, 1 / 1.28d) * 2 - currentPos;
                } else if (status == IIndicator.MOVING_FOOTER) {
                    if (offset > 0) {
                        //如果希望拖动缩回时类似QQ一样没有阻尼效果，阻尼效果只存在于上拉则可以在此返回offset
                        //如果希望拖动缩回时类似QQ一样有阻尼效果，那么请注释掉这个判断语句
                        return offset;
                    }
                    return -((float) Math.pow(Math.pow(currentPos / 2, 1.28d) - offset, 1 / 1.28d) * 2 - currentPos);
                } else {
                    if (offset > 0) {
                        return (float) Math.pow(offset, 1 / 1.28d) * 2;
                    } else if (offset < 0) {
                        return -(float) Math.pow(-offset, 1 / 1.28d) * 2;
                    } else {
                        return offset;
                    }
                }
            }
        });
 ```
#### Xml属性 
##### SmoothRefreshLayout 自身配置
|名称|类型|描述|
|:---:|:---:|:---:|
|sr_content|reference|指定内容视图的资源ID|
|sr_resistance|float|移动刷新视图时候的移动阻尼（默认:`1.65f`）|
|sr_resistance_of_footer|float|移动Footer视图时候的移动阻尼（默认:`1.65f`）|
|sr_resistance_of_header|float|移动Header视图时候的移动阻尼（默认:`1.65f`）|
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
|sr_duration_of_back_to_keep_refresh_pos|integer|设置回滚到保持刷新视图位置的时间（默认:`200`）|
|sr_duration_of_back_to_keep_header_pos|integer|设置回滚到保持Header视图位置的时间（默认:`200`）|
|sr_duration_of_back_to_keep_header_pos|integer|设置回滚到保持Footer视图位置的时间（默认:`200`）|
|sr_enable_pin_content|boolean|固定内容视图（默认:`false`）|
|sr_enable_keep_refresh_view|boolean|刷新中保持视图停留在所设置的应该停留的位置（默认:`true`）|
|sr_enable_pull_to_refresh|boolean|拉动刷新,下拉或者上拉到触发刷新位置即立即触发刷新（默认:`false`）|
|sr_enable_over_scroll|boolean|越界回弹（默认:`true`）|
|sr_empty_layout|reference|指定空状态下对应的布局资源ID|
|sr_error_layout|reference|指定异常状态下对应的布局资源ID|
|sr_custom_layout|reference|指定自定义状态下对应的布局资源ID|
|sr_state|enum|状态设置 （默认:`STATE_CONTENT`）|
|sr_enable_refresh|boolean|设置是否启用下拉刷新（默认:`ture`）|
|sr_enable_load_more|boolean|设置是否启用加载更多（默认:`false`）|
|sr_header_background_color|color|设置Header刷新高度区域的背景色|
|sr_footer_background_color|color|设置Footer刷新高度区域的背景色|

##### TwoLevelSmoothRefreshLayout 自身配置
|名称|类型|描述|
|:---:|:---:|:---:|
|sr_enable_two_level_refresh|boolean|设置是否启用二级刷新（默认:`true`）|

##### SmoothRefreshLayout包裹内部其他View支持配置
|名称|类型|描述|
|:---:|:---:|:---:|
|layout_gravity|flag|指定其它被包裹视图的对齐属性(非 targetView、非refreshView)|

#### SmoothRefreshLayout java属性设置方法
|名称|参数|描述|
|:---:|:---:|:---:|
|setHeaderView|IRefreshView|配置头部视图|
|setFooterView|IRefreshView|配置尾部视图|
|setContentView|int,View|配置内容视图,参数1:设置内容视图对应的状态,参数2:状态对应的内容视图|
|setState|int|配置当前状态|
|setState|int,boolean|配置当前状态,参数1:当前状态,参数2:是否使用渐变动画过渡|
|setDisableWhenAnotherDirectionMove|boolean|内部视图含有其他方向滑动视图时需设置该属性为ture（默认:`false`）|
|setEnableNextPtrAtOnce|boolean|刷新完成即可再次刷新|
|setMaxOverScrollDuration|int|设置越界回弹动画最长时间（默认:`500`）|
|setMinOverScrollDuration|int|设置越界回弹动画最短时间（默认:`150`）|
|setResistance|float|移动刷新视图时候的移动阻尼（默认:`1.65f`）|
|setResistanceOfFooter|float|移动Footer视图时候的移动阻尼（默认:`1.65f`）|
|setResistanceOfHeader|float|移动Header视图时候的移动阻尼（默认:`1.65f`）|
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
|setDurationOfBackToKeepRefreshViewPosition|integer|设置回滚到保持刷新视图位置的时间（默认:`200`）|
|setDurationOfBackToKeepHeaderPosition|integer|设置回滚到保持Header视图位置的时间（默认:`200`）|
|setDurationOfBackToKeepFooterPosition|integer|设置回滚到保持Footer视图位置的时间（默认:`200`）|
|setEnablePinContentView|boolean|固定内容视图（默认:`false`）|
|setEnabledPullToRefresh|boolean|拉动刷新,下拉或者上拉到触发刷新位置即立即触发刷新（默认:`false`）|
|setEnableOverScroll|boolean|越界回弹（默认:`true`）|
|setEnabledInterceptEventWhileLoading|boolean|刷新中拦截不响应触摸操作（默认:`false`）|
|setEnableHeaderDrawerStyle|boolean|Header抽屉样式,即Header视图在内容视图下面（默认:`false`）|
|setEnableFooterDrawerStyle|boolean|Footer抽屉样式,即Footer视图在内容视图下面（默认:`false`）|
|setDisablePerformRefresh|boolean|关闭触发Header刷新（默认:`false`）|
|setDisablePerformLoadMore|boolean|关闭触发Footer刷新（默认:`false`）|
|setEnableLoadMoreNoMoreData|boolean|设置Footer没有更多数据，该选项设置`true`时在Frame层等同`setDisablePerformLoadMore`设置为`true`，只是自定义视图可以根据该标志位改变视图样式,`ClassicFooter`默认实现了对该属性的支持（默认:`false`）|
|isEnabledLoadMoreNoMoreDataNoNeedSpringBack|boolean|设置Footer没有更多数据情况下不再回弹|
|setDisableRefresh|boolean|禁用Header刷新（默认:`false`）|
|setDisableLoadMore|boolean|禁用Footer刷新（默认:`false`）|
|setEnableKeepRefreshView|boolean|刷新中保持视图停留在所设置的应该停留的位置（默认:`true`）|
|setEnableScrollToBottomAutoLoadMore|boolean|到底部自动加载（默认:`false`）|
|setEnablePinRefreshViewWhileLoading|boolean|固定刷新视图在所设置的应该停留的位置，并且不响应移动，即Material样式（默认:`false`）,设置前提是开启了`setEnablePinContentView`和`setEnableKeepRefreshView`2个选项，否则运行时会抛出异常|
|setSpringInterpolator|Interpolator|设置默认的滚动插值器|
|setOverScrollInterpolator|Interpolator|设置越界回弹时的滚动插值器|
|setEnableCheckFingerInsideAnotherDirectionView|boolean|设置是否开启检查手指按下点是否位于其他方向滚动视图内，该属性起作用必须满足开启`setDisableWhenAnotherDirectionMove`|
|setEnableCompatLoadMoreScroll|boolean|设置是否开启加载更多时的同步滚动（默认:`true`）|
|setHeaderBackgroundColor|int|设置Header刷新高度区域的背景色，可用以替代在Header样式为不需要动态改变视图大小的情况下又想设置刷新高度区域的背景色的场景|
|setFooterBackgroundColor|int|设置Footer刷新高度区域的背景色，可用以替代在Footer样式为不需要动态改变视图大小的情况下又想设置刷新高度区域的背景色的场景|
|setEnabledCanNotInterruptScrollWhenRefreshCompleted|boolean|设置开启当刷新完成时，回滚动作不能被打断|

#### SmoothRefreshLayout 回调
|名称|参数|描述|
|:---:|:---:|:---:|
|setOnRefreshListener|T extends OnRefreshListener|设置刷新事件监听回调|
|setOnStateChangedListener|OnStateChangedListener|设置状态改变回调|
|setChangeStateAnimatorCreator|IChangeStateAnimatorCreator|设置改变状态时使用的动画创建者|
|addOnUIPositionChangedListener|OnUIPositionChangedListener|添加视图位置变化的监听回调|
|removeOnUIPositionChangedListener|OnUIPositionChangedListener|移除视图位置变化的监听回调|
|setOnLoadMoreScrollCallback|OnLoadMoreScrollCallback|设置Footer完成刷新后进行平滑滚动的回调|
|setOnPerformAutoLoadMoreCallBack|OnPerformAutoLoadMoreCallBack|设置触发自动加载更多的条件回调，如果回调的`canAutoLoadMore()`方法返回`true`则会立即触发加载更多|
|setOnChildNotYetInEdgeCannotMoveHeaderCallBack|OnChildNotYetInEdgeCannotMoveHeaderCallBack|设置检查内容视图是否在顶部的重载回调（SmoothRefreshLayout内部`isChildNotYetInEdgeCannotMoveHeader()`方法）|
|setOnChildNotYetInEdgeCannotMoveFooterCallBack|OnChildNotYetInEdgeCannotMoveFooterCallBack|设置检查内容视图是否在底部的重载回调（SmoothRefreshLayout内部`isChildNotYetInEdgeCannotMoveFooter()`方法）|
|setOnHookHeaderRefreshCompleteCallback|OnHookUIRefreshCompleteCallBack|设置Header刷新完成的Hook回调，可实现延迟完成刷新|
|setOnHookFooterRefreshCompleteCallback|OnHookUIRefreshCompleteCallBack|设置Footer刷新完成的Hook回调，可实现延迟完成刷新|
|setOnFingerInsideAnotherDirectionViewCallback|OnFingerInsideAnotherDirectionViewCallback|设置检查手指按下点是否位于其他滚动视图内的重载回调，可自定义判断逻辑，提高判断效率|

#### SmoothRefreshLayout 其它
|名称|参数|描述|
|:---:|:---:|:---:|
|debug（静态方法）|boolean|Debug开关|
|setDefaultCreator（静态方法）|IRefreshViewCreator|设置刷新视图创建者,如果没有特殊指定刷新视图且设置的模式需要刷新视图则会调用创建者构建刷新视图|
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

#### TwoLevelSmoothRefreshLayout java属性设置方法
|名称|参数|描述|
|:---:|:---:|:---:|
|setRatioOfHeaderHeightToHintTwoLevelRefresh|float|设置触发二级刷新提示时的位置占Header视图的高度比|
|setRatioOfHeaderHeightToTwoLevelRefresh|float|设置触发二级刷新时的位置占Header视图的高度比|
|setOffsetRatioToKeepTwoLevelHeaderWhileLoading|float|二级刷新中保持视图位置占Header视图的高度比（默认:`1f`）|
|setDisableTwoLevelRefresh|boolean|设置是否关闭二级刷新（默认:`false`）|
|setDurationOfBackToKeepTwoLevelHeaderViewPosition|int|设置回滚到保持二级刷新Header视图位置的时间（默认:`500`）|
|setDurationToCloseTwoLevelHeader|int|设置二级刷新Header刷新完成回滚到起始位置的时间（默认:`500`）|

#### TwoLevelSmoothRefreshLayout 其它
|名称|参数|描述|
|:---:|:---:|:---:|
|autoTwoLevelRefreshHint|无参|自动触发二级刷新提示并滚动到触发提示位置后回滚回起始位置|
|autoTwoLevelRefreshHint|int|自动触发二级刷新提示并滚动到触发提示位置后停留指定时长，参数:停留多长时间|
|autoTwoLevelRefreshHint|boolean|自动触发二级刷新提示是否滚动到触发提示位置后回滚回起始位置，参数:是否滚到到触发位置|
|autoTwoLevelRefreshHint|boolean,int|自动触发二级刷新提示,参数1:是否滚动到触发位置，参数2:停留多长时间|
|autoTwoLevelRefreshHint|boolean,int,boolean|自动触发二级刷新提示,参数1:是否滚动到触发位置，参数2:停留多长时间，参数3:是否可以被触摸打断，即触发提示动作过程中拦截触摸事件，直到回滚到起始位置并重置为默认状态|

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
# 更新日志
## 1.4.7.2
> 修复RecyclerView未满屏幕情况下Footer滑动问题,#25。    
## 1.4.7.1
> 修改二级刷新，添加 `autoTwoLevelRefreshHint(boolean smoothScroll, int stayDuration, boolean canBeInterrupted)`方法，可以设置提示是否能被打断。    
> 修复某些嵌套布局下触摸事件处理没有非常好的工作问题。    
## 1.4.7
> 修改二级刷新，修复 `IIndicator` 转换错误。    
> 删除支援Margin特性，提高效率。    
> 修复某些嵌套布局下触摸事件处理没有非常好的工作问题。    
> 重命名了一些内部方法名。    
## 1.4.6.1
> 修复嵌套布局下，Fling处理逻辑问题导致惯性消失问题。    
> 添加 `setOnFingerInsideHorViewCallback` 方法。    
> 添加 `setEnableCheckFingerInsideHorView` 方法。    
> 添加 `isEnableCheckFingerInsideHorView` 方法。    
> 添加 `OnFingerInsideHorViewCallback` 回调接口。    
> 以上添加的方法和接口用于检测手指按下时，触摸点是否位于水平滚动视图内。开启 `setDisableWhenHorizontalMove`和 `setEnableCheckFingerInsideHorView`后，将根据手指按下的触摸点是否位于水平滚动视图内进行触摸事件拦截处理，如果在内部，就拦截横向滑动，如果不在就拦截处理所有滑动事件。
## 1.4.6
> 实现即使Header或者Footer中的事务还没完成（例如Header处于刷新中），仍然可以滑动其他刷新视图。原逻辑为如果Header处于刷新中就无法再拉起Footer。    
> 在 `IRefreshView` 接口中添加 `onPureScrollPositionChanged`方法，用于当刷新视图事务未完成，移动其它视图会回调该方法，可以根据具体需求实现该状态下的视图展现。    
> 优化优化回弹效果。   
> 修复某些特殊情况下触摸事件没有很好的处理问题。     
> 合并部分重复逻辑代码。    
> 添加 `setSpringInterpolator` 方法，用以设置默认的滚动插值器。    
> 添加 `setOverScrollInterpolator` 方法，用以设置越界回弹时的滚动插值器。    
## 1.4.5
> 修改二级刷新，修改 `TwoLevelSmoothRefreshLayout` styleable `sr_enable_two_level_pull_to_refresh` 为 `sr_enable_two_level_refresh`。     
> 修改二级刷新，删除 `isEnabledTwoLevelPullToRefresh` 方法，添加 `isDisabledTwoLevelRefresh` 方法。    
> 修改二级刷新，删除 `setEnableTwoLevelPullToRefresh` 方法，添加 `isDisabledTwoLevelRefresh` 方法。    
> 修改二级刷新，修复 `setDurationOfBackToKeepTwoLeveHeaderViewPosition`方法名，应为 `setDurationOfBackToKeepTwoLevelHeaderViewPosition`。    
> 修改二级刷新，添加 `autoTwoLevelRefreshHint` 相关方法。    
> 修复 `WaveTextRefreshView` 中动画显示错位问题(7.0系统BUG);    
> 修复部分逻辑错误。    
## 1.4.4.1
> 修改 `onNestedPreScroll`方法实现，合并逻辑，提高效率和可读性。    
> 修改 `onNestedPreFling` 方法实现，提高效率。
## 1.4.4
> 优化优化回弹效果。    
> 修复某些情况下 `ViewTreeObserver` 的 `OnScrollChangedListener` 没有完全移除问题。    
> 修改 `MaterialSmoothRefreshLayout` 限制Header最大移动距离为1.5倍。    
> 删除 `setOverScrollDistanceRatio` 方法和 `mOverScrollDistanceRatio` 属性。    
> 添加 `setMinOverScrollDuration` 方法。    
> 修复某些特殊情况下，越界回弹未正常终止问题。    
> 删除RefreshCompleteHook中的弱引用，避免某些特殊情况下，引用被回收导致RefreshLayout内部状态异常问题。    
## 1.4.3
> 修改越界回弹实现，优化回弹效果。    
> 修复移动视图过程中触发刷新刷新逻辑问题（PS：2个判断都少写了括号）。    
> 修复偶尔 `WaveHeader` 圆环刷新时坐标异常问题。    
> 添加 `setOverScrollDurationRatio` 方法，用于设置回弹时长比例。    
> 添加 `setMaxOverScrollDuration` 方法，用于设置最大回弹时长。    
> 添加 `isEnabledScrollToTopAutoRefresh` 方法。    
> 添加 `setEnableScrollToTopAutoRefresh` 方法。用于开启到顶自动刷新。    
> 修复加载更多无更多数据没有正确设置问题（PS：即MASK的值错了，应为`0x07<<10`而不是`0x05<<10`）。    
> 修复若干触摸事件处理BUG。
## 1.4.2(已废弃)
> 修改越界回弹实现，优化回弹效果。    
> 修复自动刷新下，无越界回弹效果问题。    
> 修复当自动刷新正在进行时，移动内容视图返回到顶部后无法继续向上移动问题。    
> 修复 `MaterialFooter` 刷新过程中颜色下标错误问题。    
## 1.4.1
> 修复状态视图填充LayoutParams不一致问题。    
> 修改包名为`me.dkzwm.widget.srl`,使包名更符合规范。    
> 修改二级刷新，删除 `setEnableBackToStartPosAtOnce`方法。    
> 修改二级刷新，添加 `setDurationOfBackToKeepTwoLeveHeaderViewPosition` 方法。    
> 修改二级刷新，添加 `setDurationToCloseTwoLevelHeader` 方法。    
> 修改二级刷新，添加 `setOffsetRatioToKeepTwoLevelHeaderWhileLoading` 方法。    
> 修改二级刷新，添加 `getOffsetToKeepTwoLevelHeaderWhileLoading` 方法。    
> 删除 `SmoothRefreshLayout` styleable中的 `sr_enable_two_level_pull_to_refresh` 属性。    
> 修改二级刷新，添加 `TwoLevelSmoothRefreshLayout` styleable，并添加 `sr_enable_two_level_pull_to_refresh` 属性。    
> 修改二级刷新，修改 `ITwoLevelIndicator` 继承 `IIndicator` 接口。    
> 修改二级刷新，在 `ITwoLevelIndicator` 中添加 `setOffsetRatioToKeepTwoLevelHeaderWhileLoading` 方法。    
> 修改二级刷新，在 `ITwoLevelIndicator` 中添加 `getOffsetToKeepTwoLevelHeaderWhileLoading` 方法。    
> 修改二级刷新，删除 `TwoLevelRefreshView` 接口参数中的 `IIndicator` 参数。    
> 添加 `WaveTextRefreshView`，存放于Demo的header中。    
> 更新英语Readme文件。    
> 修复静态刷新视图构造器在某些特定情况下未很好的工作问题。    
> 修复 `WaveHeader` 某些场景设置插值器不对问题。    
> 修复自动刷新可能被打断问题。    
## 1.4.0  
> 删除了MODE属性，功能性和其他属性有重复，导致逻辑复杂度直线上升，去掉后使用其他属性组合替代。    
> 修复未处于刷新状态调用refreshComplete()方法导致内部视图偏移问题。    
> 去掉多余资源，减小包体积。    
> 修改`setResistanceOfPullUp`方法为`setResistanceOfFooter`，使含义更明确。    
> 修改`setResistanceOfPullDown`方法为`setResistanceOfHeader`，使含义更明确。    
> 修改Xml `sr_resistance_of_pull_up`属性为 `sr_resistance_of_footer`，使含义更明确。    
> 修改Xml `sr_resistance_of_pull_down`属性为 `sr_resistance_of_header`，使含义更明确。    
> 修改`setDurationOfBackToRefreshViewHeight`方法为`setDurationOfBackToKeepRefreshViewPosition`，使含义更明确。    
> 修改Xml `sr_duration_of_back_to_refresh_height`属性为 `sr_duration_of_back_to_keep_refresh_pos`，使含义更明确。    
> 修改`getDurationOfBackToHeaderHeight`方法为`getDurationOfBackToKeepHeaderPosition`，使含义更明确。    
> 修改`setDurationOfBackToHeaderHeight`方法为`setDurationOfBackToKeepHeaderPosition`，使含义更明确。    
> 修改Xml `sr_duration_of_back_to_header_height`属性为 `sr_duration_of_back_to_keep_header_pos`，使含义更明确。    
> 修改`getDurationOfBackToFooterHeight`方法为`getDurationOfBackToKeepFooterPosition`，使含义更明确。    
> 修改`setDurationOfBackToFooterHeight`方法为`setDurationOfBackToKeepFooterPosition`，使含义更明确。    
> 修改Xml `sr_duration_of_back_to_footer_height`属性为 `sr_duration_of_back_to_keep_footer_pos`，使含义更明确。    
> 修改Xml `sr_layout_gravity`属性为 `layout_gravity`。    
> 删除Xml `sr_mode`属性。    
> 添加 `sr_enable_refresh`属性，设置是否启用下拉刷新。默认启用    
> 添加 `sr_enable_load_more`属性，设置是否启用加载更多。默认不启用。    
> 添加`setEnableHideHeaderView`方法。    
> 添加`isEnabledHideHeaderView`方法。   
> 添加`isEnabledHideFooterView`方法。   
> 添加`setEnableHideFooterView`方法。   
> 原有Mode对应改用如下属性组合替代。`NONE`->`setDisableRefresh(true)`+`setDisableLoadMore(true)`+`setEnableOverScroll(false)`;`REFRESH`->`setDisableRefresh(false)`+`setDisableLoadMore(true)`;
`LOAD_MORE`->`setDisableRefresh(true)`+`setDisableLoadMore(false)`;`OVER_SCROLL`->`setDisableRefresh(false)`+`setDisableLoadMore(false)`+`setDisablePerformRefresh(true)`+`setDisablePerformLoadMore(true)`+`setEnableHideHeaderView(true)`+`setEnableHideFooterView(true)`;`BOTH`->`setDisableRefresh(false)`+`setDisableLoadMore(false)`

## 1.3.5.1
> 修复多点触摸下体验不一致问题，使非NestedScrolling下的体验和NestedScroling下的体验保持一致。    
> 修改内部ScrollChecker实现，去掉不必要的防止内存泄露代码。    
> 修正Hook方法可能失效问题。    

## 1.3.5
> 添加setEnableLoadMoreNoMoreData（）方法。当使用到底部自动加载更多情况下再无数据调用此方法。默认`ClassicFooter`实现了该功能。    
> 给`MaterialFooter`添加了设置多颜色属性。    
> 添加支持多状态视图支持。    
> 修复部分情况下的关闭下拉刷新和关闭加载更多失效问题。    
> 修复自动加载更多的BUG。    
> 删除了对`material-progress`的依赖。    
> 去掉了一些多余代码，减少体积。    
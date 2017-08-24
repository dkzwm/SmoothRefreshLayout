# 更新日志
## 1.4.2
> 修改越界回弹实现，优化回弹效果。
> 修复自动刷新下，越界回弹实现问题。
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
> 更新英语Readme文件.    
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
# 更新日志

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
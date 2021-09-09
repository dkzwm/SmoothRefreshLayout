# 更新日志
## 1.7.2.4
> 修复#136，修正默认配置下特性情况下AppBarLayout拖动问题。
## 1.7.2.3
> 修复#136，修正参数传递错误。
## 1.7.2.2
> 兼容Android12。
## 1.7.2.1
> 修复#130，转移到Maven Central。
## 1.7.2
> 转移仓库到JitPack
## 1.7.1.6.androidx
> 修复#121
## 1.7.1.5.androidx
> 修复#120, 上一个版本未抽出时间全部测试，导致ext-horizontal出现严重错误，如果没有使用横向扩展支持的可以不用升级，如果使用横向扩展支持的请务必升级本版本。
## ~~1.7.1.4.androidx~~（此版本的ext-horizontal包存在严重BUG，请升级版本到1.7.1.5.androidx,实在抱歉）
> 修复#118, 考虑到很多场景下，使用者可能在视图已从视图树中移除情况下仍触发操作视图，不再手动置空布局管理器。
## 1.7.1.3.androidx
> 修复#115，未考虑到触发刷新同步调用结束刷新导致刷新视图无法操作问题。   
> 添加扩展模块 `ext-dynamic-rebound`, 使用基于物理的动画实现越界回弹，使回弹效果更贴近真实。理论上该扩展模块可直接替换SRL使用，无需修改代码。    
## 1.7.1.2.androidx
> 在 `VRefreshLayoutManager` 添加方法 `setHeaderBackgroundColor`、`setFooterBackgroundColor`。    
> 添加方法 `getLayoutManager` 方法。    
> 添加接口 `OnCalculateBounceCallback` ，用以重载计算越界距离和时间函数，不喜欢本库自带越界回弹效果的话，可使用该接口实现自己的越界计算函数，配合`setMinOverScrollDuration`、`setMaxOverScrollDuration`、`setFlingBackDuration`、`setSpringInterpolator`、`setSpringBackInterpolator` 可以实现几乎所有的越界回弹效果。    
> 添加方法 `setOnCalculateBounceCallback`。    
> 添加方法 `setFlingBackDuration`。    
## 1.7.1.1.androidx
> 修改 `ViewCatcherUtil` 类实现， 完善 `CoordinatorLayout` 各种布局下的适配性。    
> 修改只在纵向模式下才动态搜索 `AppBarLayout`。    
## 1.7.1.androidx
> 重构布局相关代码，封装出 `LayoutManager`，已实现默认刷新布局管理器和拉伸布局管理器，支持自定义布局管理器以实现不同需求下的效果。    
> 修改 `RefreshCompleteHook`类的`onHookComplete`方法定义，添加`immediatelyNoScrolling`参数，用以标记是否需要立刻回置到起始位置。    
> 删除方法 `getDurationToCloseHeader`、`getDurationToCloseFooter`，该方法原来在`MaterialHeader`中有使用，修改实现后已不再需要故删除。    
> 修改 `AutoRefreshUtil` 类实现。    
> 删除接口 `OnNestedScrollChangedListener`，以及相关方法，起初添加该方法是为了实现自动刷新工具检测内部滚动事件用，实际使用中发现和 `OnUIPositionChangedListener`接口有较多重合，效率偏低同时由于修改了自动刷新工具已不再需要该接口故而删除。    
> 删除方法 `setEnableSmoothRollbackWhenCompleted`、`isEnabledSmoothRollbackWhenCompleted`，改为默认开启，原因为刷新或者加载更多完成后，添加数据会导致视图边界变动，如果此时处于触摸中，之前的逻辑是直接让刷新视图回置到起始位置，但会引起和未处于触摸中的回滚起始位置动画体验不一致的感觉，故而统一为刷新完成不管是否触摸中一律不能中断刷新完成回滚动画让刷新视图回滚到起始位置，保持体验一致。    
> 删除方法 `setEnableNextPtrAtOnce`、`isEnabledNextPtrAtOnce`，由于上一点配置后导致本配置失效故而删除。    
> 添加方法 `forceRefresh`、`forceLoadMore`，将直接忽略内部状态强制进行刷新，该方法不会触发滚动相关逻辑，只是修改内部状态为刷新态。可用来支撑点击进行重新刷新/加载功能。    
> 删除方法 `setHeaderBackgroundColor`、`setFooterBackgroundColor`，封装布局管理器后，背景色填充改为用户在刷新视图层实现。    
## 1.7.0.androidx
> 修改支持 `NestedScrollingParent3`、`NestedScrollingChild3`，此版本开始不再更新`android.support`对应包，只更新`androidx`包。    
> 移动 `SRReflectUtil` 到 `util` 包下，`core` 包不再包含兼容低版本的反射代码，如需使用请自行继承`SmoothRefreshLayout`重写相应函数自行调用`SRReflectUtil`对应方法进行使用。    
> 重命名 `OnLoadMoreScrollCallback` 接口为 `OnSyncScrollCallback`，同时实现滚动到顶自动刷新的同步滚动逻辑。    
> 删除方法 `setEnableDynamicEnsureTargetView`  和  `isEnabledDynamicEnsureTargetView`，改为默认就是动态识别滚动内容视图，不再可配置，如果需要指定滚动内容视图请使用`setScrollTargetView`。    
> 删除方法 `setEnableCheckInsideAnotherDirectionView` 和  `isEnableCheckInsideAnotherDirectionView`，考虑到已动态识别滚动视图，不再浪费效率判断手指按下时是否处于其他方向View中。    
> 删除回调类 `OnInsideAnotherDirectionViewCallback` 以及其设置方法 `setOnInsideAnotherDirectionViewCallback`，原因同上。
> 删除方法 `setLoadingMinTime`， 没什么用，改由用户控制最小加载时间。    
> 删除方法 `mapTheInverseMatrix`，使用隐藏的 `Android 5.1` 以上系统版本的访问浅灰名单级方法 `transformPointToViewLocal` 进行处理，针对 `Android 5.1` 以下没有该方法的情况做了兼容处理。    
> 优化动态搜索 `AppBarLayout` 逻辑，避免无法识别外层嵌套。    
## 1.6.6.8
> 调整优化刷新视图自定样式效率，在`STYLE_SCALE`/`STYLE_FOLLOW_SCALE`/`STYLE_FOLLOW_CENTER`下不再使用`requestLayout`进行重布局，考虑到SRL高度固定情况下，直接在内部进行计算，大幅提高布局的效率。    
> 修复#95，感谢@tbxyxs 的反馈。    
> 移除无用attr。    
## 1.6.6.7
> 添加自动二级刷新功能。
## 1.6.6.6
> 修改嵌套滚动Fling消耗问题，感谢@dnwalter 的反馈，后续准备实现NestedScrollingParent3和NestedScrollingChild3接口，提高嵌套滚动下的Fling顺滑度。    
## 1.6.6.5
> 修复横向刷新，当内部视图为ViewPager时，Fling传递问题。感谢@renzhenfei 的反馈。    
## 1.6.6.4
> 优化触摸事件处理。    
## 1.6.6.3
> 修复当SRL高度为自适应同时内部视图高度也为自适应时高度计算有误的问题。感谢@Lalways 的反馈。    
> 修改AppBarUtil实现。    
## 1.6.6.2
> 修复 `setEnableNoMoreData` 功能异常。感谢@seemelala 的反馈。
## 1.6.6.1
> 修复由于实现 `NestedScrollingChild2`和 `NestedScrollingParent2` 接口所带来的Fling嵌套滚动传递未正常终结引起的短暂点击失效问题。感谢@dahai2070 的反馈。    
## 1.6.6
> 删除 `ViscousFluidInterpolator`。    
> 整合`ext-util`包的`QuickConfigAppBarUtil`到 `core`包中，并重命名为AppBarUtil，内置自动适配逻辑以适应越来越流行的嵌套特性。    
> 重命名类: `ext-util`包中的 `QuickConfigAutoRefreshUtil`为 `AutoRefreshUtil`。    
> 优化布局逻辑，提高效率。    
> 默认插值器修改。   
## 1.6.5
> 删除状态布局功能。    
> 添加方法:`setEnableDynamicEnsureTargetView` 方法即动态搜索布局功能，开启后理论上不需要手动指定边界判断视图就能适配某些特殊场景，例如：ViewPager共用一个刷新控件。    
> 修改接口:`IRefreshViewCreator` 的方法返回参数，避免漏设刷新视图。    
> 添加接口:`OnPerformAutoRefreshCallBack` 和对应的设置方法:`setOnPerformAutoRefreshCallBack`。    
> 修复`isEnabledNoSpringBackWhenNoMoreData`第二次不起作用的问题。   
> 添加接口:`OnNestedScrollChangedListener`和对应的设置方法: `addOnNestedScrollChangedListener` 和 `removeOnNestedScrollChangedListener`。用以当本视图以及内部的自视图发生滚动时触发监听。    
> 添加接口: `OnStatusChangedListener`用以监听状态改变。    
> 添加方法: `addOnStatusChangedListener` 和 `removeOnStatusChangedListener`，用以监听状态改变。    
> 删除方法: `equalsOnHookHeaderRefreshCompleteCallback`  、`equalsOnHookFooterRefreshCompleteCallback` 、`setOverScrollInterpolator`。     
> 扩展实现: `NestedScrollingChild2` 、`NestedScrollingParent2` 接口，因而`Android Support Library`版本必须大于`26.1.0`，以完善Fling。    
> 添加方法: `isEnabledPerformFreshWhenFling` 和 `setEnablePerformFreshWhenFling` ， 默认情况下当正在拖动刷新视图时，如果是向收回刷新视图方向甩动并触发了惯性甩动（Fling），即使松手时的高度大于等于触发刷新高度仍然不会触发刷新，这个时候如果想触发刷新则需要打开本开关。    
> 删除部分反射逻辑，应对Android P。    
> 综合考虑性能和逻辑复杂度后删除了通过`ViewTreeObserver`来监听滚动逻辑，改用重载`computeScroll`方法，理论上效率会轻微降低，但复杂度会降低不少。    
> 添加方法: `isEnabledOldTouchHandling` 和 `setEnableOldTouchHandling` 方法，至此版本开始，支持2种触摸处理方式，一种为老版本的拦截处理（触摸事件只且当刷新视图收回后才由内容视图向下传递），一种为新版本的透传处理（触摸事件将从触发到终止均向下传递，传递时剔除消耗部分，特殊场景下做差值处理）。老版本的处理逻辑由于是拦截传递，必然导致视觉上的割裂感，主要体现在拉出刷新视图再收回视图情况下，当刷新视图回到顶部后缓慢滑动会导致内容视图触发按下效果。新版本由于是差值透传所以不会产生这个问题。使用者可按需进行切换调整。默认使用新版本的处理逻辑。    
> 修改实现: 考虑到越界回弹是iOS和macOS特有，实现上参考iOS的越界回弹效果，重新实现了越界回弹的逻辑，尽量接近iOS和macOS上的效果。    
> 添加方法: `setSpringBackInterpolator` ， 设置回滚时的滚动插值器，`setSpringInterpolator` ， 意思为设置滑出时的滚动插值器。    
> 添加方法: `getScrollMode`， 用以获取当前滚动的模式，有6个模式，`SCROLLER_MODE_NONE` 未滚动、`SCROLLER_MODE_PRE_FLING` 缩回或者拉出刷新视图手势下的滚动模式、`SCROLLER_MODE_CALC_FLING` 内容视图滚动中计算加速度模式 、`SCROLLER_MODE_FLING` 越界回弹弹出滚动模式、`SCROLLER_MODE_FLING_BACK`越界回弹缩回滚动模式、`SCROLLER_MODE_SPRING` 主动弹出滚动模式、`SCROLLER_MODE_SPRING_BACK`释放缩回滚动模式。    
> 修改方法: `setLifecycleObserver` 为 `addLifecycleObserver`。    
> 添加方法: `removeLifecycleObserver` 。    
> 添加方法: `getIndicator`。    
> 删除方法: `isInStartPosition`。    
> 完善部分代码逻辑。    
> 调整越界回弹弹出的最小时间值为`100`。    
> 迁移仓库，由JitPack迁移到JCenter（太蠢了，手残删除了2个包的厂库，导致2个包的引用名有修改）。    
> 修复部分代码逻辑错误。   
## 1.6.4.3
> 修复`MODE_SCALE` 模式下的拉伸BUG。    
## 1.6.4.2
> 修正`MODE_SCALE` 模式下仍能调用自动刷新的问题。    
> 修正Fling动作某些情况下的BUG。    
> 优化Fling在开启OverScroll情况下的逻辑。    
## 1.6.4.1
> 修复横向刷新错误设置偏移的问题。    
> 优化`MODE_SCALE`模式下部分场景，如果内部视图为ScrollView或者NestedScrollView或者HoriztonalScrollView，拉伸对象将为内部视图内的第一个视图，以达到更好的效果。    
> 减少反射逻辑。    
> 新增方法:`setAutomaticSpringInterpolator`，用来设置自动刷新时的滚动插值器。    
## 1.6.4
> 删除方法:`setEnableHideHeaderView`，改用 `getHeaderView().getView().setVisibility` 进行替代。    
> 删除方法:`setEnableHideFooterView`，改用 `getFooterView().getView().setVisibility` 进行替代。    
> 删除方法:`isEnabledHideHeaderView`，改用 `getHeaderView().getView().getVisibility` 进行替代。    
> 删除方法:`isEnabledHideFooterView`，改用 `getFooterView().getView().getVisibility` 进行替代。    
> 新增方法:`getFooterView`。    
> 新增方法:`getHeaderView`。    
> 删除方法:`getDefaultHeader`(位于 `MaterialSmoothRefreshLayout`和`ClassicSmoothRefreshLayout`)。    
> 删除方法:`getDefaultFooter`(位于 `MaterialSmoothRefreshLayout`和`ClassicSmoothRefreshLayout`)。    
> 删除类:`OverScrollerChecker`，合并滚动逻辑到 `ScrollerChecker` 类中，对滚动逻辑进行整合。    
> 修改方法:`setLoadMoreScrollTargetView` -> `setScrollTargetView`。    
> 修改方法:`getLoadMoreScrollTargetView` -> `getScrollTargetView`。    
> 修改Xml属性`sr_content`实现，支持遍历查找内部所有层级的资源Id。    
> 合并整合横向刷新和纵向刷新触摸事件逻辑。    
> 优化部分代码逻辑。    
> 修正部分参数设置错误。    
## 1.6.3.3
> 分割合并部分代码逻辑。    
> 拆分 `IIndicator` 为 `IIndicator` 和 `IIndicatorSetter` 2个接口，避免非Frame层调用相关setter方法。    
> 删除部分代码逻辑。    
## 1.6.3.2
> 删除多余部分方法（PS:Fuck JitPack）.
## 1.6.3
> 重命名Xml属性和部分开放API，使Xml属性命名更规范，同时尽量避免过长API名导致的代码冗余.    
> 修改Xml属性名:`style_follow_scale` -> `style_followScale`.    
> 修改Xml属性名:`style_follow_pin` -> `style_followPin`.    
> 修改Xml属性名:`style_follow_center` -> `style_followCenter`.    
> 修改Xml属性名:`sr_resistance_of_footer` -> `sr_resistanceOfFooter`.    
> 修改Xml属性名:`sr_resistance_of_header` -> `sr_resistanceOfHeader`.    
> 修改Xml属性名:`sr_ratio_of_refresh_height_to_refresh` -> `sr_ratioToRefresh`.    
> 修改Xml属性名:`sr_ratio_of_header_height_to_refresh` -> `sr_ratioOfHeaderToRefresh`.    
> 修改Xml属性名:`sr_ratio_of_footer_height_to_refresh` -> `sr_ratioOfFooterToRefresh`.    
> 修改Xml属性名:`sr_offset_ratio_to_keep_refresh_while_Loading` -> `sr_ratioToKeep`.    
> 修改Xml属性名:`sr_offset_ratio_to_keep_header_while_Loading` -> `sr_ratioToKeepHeader`.    
> 修改Xml属性名:`sr_offset_ratio_to_keep_footer_while_Loading` -> `sr_ratioToKeepFooter`.    
> 修改Xml属性名:`sr_can_move_the_max_ratio_of_refresh_height` -> `sr_maxMoveRatio`.    
> 修改Xml属性名:`sr_can_move_the_max_ratio_of_header_height` -> `sr_maxMoveRatioOfHeader`.    
> 修改Xml属性名:`sr_can_move_the_max_ratio_of_footer_height` -> `sr_maxMoveRatioOfFooter`.   
> 修改Xml属性名:`sr_duration_to_close_of_refresh` -> `sr_closeDuration`.    
> 修改Xml属性名:`sr_duration_to_close_of_header` -> `sr_closeHeaderDuration`.    
> 修改Xml属性名:`sr_duration_to_close_of_footer` -> `sr_closeFooterDuration`.    
> 修改Xml属性名:`sr_duration_of_back_to_keep_refresh_pos` -> `sr_backToKeepDuration`.    
> 修改Xml属性名:`sr_duration_of_back_to_keep_header_pos` -> `sr_backToKeepHeaderDuration`.    
> 修改Xml属性名:`sr_duration_of_back_to_keep_footer_pos` -> `sr_backToKeepFooterDuration`.    
> 修改Xml属性名:`sr_enable_pull_to_refresh` -> `sr_enablePullToRefresh`.    
> 修改Xml属性名:`sr_enable_over_scroll` -> `sr_enableOverScroll`.    
> 修改Xml属性名:`sr_enable_keep_refresh_view` -> `sr_enableKeep`.    
> 修改Xml属性名:`sr_enable_pin_content` -> `sr_enablePinContent`.    
> 修改Xml属性名:`sr_enable_refresh` -> `sr_enableRefresh`.    
> 修改Xml属性名:`sr_enable_load_more` -> `sr_enableLoadMore`.    
> 修改Xml属性名:`sr_empty_layout` -> `sr_emptyLayout`.    
> 修改Xml属性名:`sr_error_layout` -> `sr_errorLayout`.    
> 修改Xml属性名:`sr_custom_layout` -> `sr_customLayout`.    
> 修改Xml属性名:`sr_header_background_color` -> `sr_headerBackgroundColor`.    
> 修改Xml属性名:`sr_footer_background_color` -> `sr_footerBackgroundColor`.    
> 修改Xml属性名:`sr_enable_two_level_refresh` -> `sr_enableTwoLevelRefresh`.     
> 支持黏贴头部，添加对应Xml属性:`sr_stickyHeader`和对应的方法 `setStickyHeaderResId` 用以设置黏贴头部的ResId，当SRL处于移动头部视图时该黏贴头部会跟随Target视图进行移动.   
> 添加Xml属性:`sr_backToKeep2Duration` 用以设置回滚到保持二级刷新头部处于二级刷新过程中的时长.    
> 添加Xml属性:`sr_closeHeader2Duration` 用以设置关闭二级刷新头部的时长.    
> 修改方法:`setRatioOfRefreshViewHeightToRefresh` -> `setRatioToRefresh`.     
> 修改方法:`setRatioOfHeaderHeightToRefresh` -> `setRatioOfHeaderToRefresh`.    
> 修改方法:`setRatioOfFooterHeightToRefresh` -> `setRatioOfFooterToRefresh`.    
> 修改方法:`setOffsetRatioToKeepRefreshViewWhileLoading` -> `setRatioToKeep`.    
> 修改方法:`setOffsetRatioToKeepHeaderWhileLoading` -> `setRatioToKeepHeader`.    
> 修改方法:`setOffsetRatioToKeepFooterWhileLoading` -> `setRatioToKeepFooter`.    
> 修改方法:`setDurationOfBackToKeepRefreshViewPosition` -> `setDurationOfBackToKeep`.    
> 修改方法:`getDurationOfBackToKeepHeaderPosition` -> `getDurationOfBackToKeepHeader`.    
> 修改方法:`setDurationOfBackToKeepHeaderPosition` -> `setDurationOfBackToKeepHeader`.    
> 修改方法:`getDurationOfBackToKeepFooterPosition` -> `getDurationOfBackToKeepFooter`.    
> 修改方法:`setDurationOfBackToKeepFooterPosition` -> `setDurationOfBackToKeepFooter`.    
> 修改方法:`setCanMoveTheMaxRatioOfRefreshViewHeight` -> `setMaxMoveRatio`.    
> 修改方法:`setCanMoveTheMaxRatioOfHeaderHeight` -> `setMaxMoveRatioOfHeader`.    
> 修改方法:`setCanMoveTheMaxRatioOfFooterHeight` -> `setMaxMoveRatioOfFooter`.    
> 修改方法:`isEnableCheckFingerInsideAnotherDirectionView` -> `isEnableCheckInsideAnotherDirectionView`.    
> 修改方法:`setEnableCheckFingerInsideAnotherDirectionView` -> `setEnableCheckInsideAnotherDirectionView`.    
> 修改方法:`isEnabledLoadMoreNoMoreData` -> `isEnabledNoMoreData`.    
> 修改方法:`setEnableLoadMoreNoMoreData` -> `setEnableNoMoreData`.    
> 修改方法:`isEnabledLoadMoreNoMoreDataNoNeedSpringBack` -> `isEnabledNoSpringBackWhenNoMoreData`.    
> 修改方法:`setEnableLoadMoreNoMoreDataNoNeedSpringBack` -> `setEnableNoSpringBackWhenNoMoreData`.    
> 修改方法:`isEnabledCanNotInterruptScrollWhenRefreshCompleted` -> `isEnabledSmoothRollbackWhenCompleted`.    
> 修改方法:`setEnableCanNotInterruptScrollWhenRefreshCompleted` -> `setEnableSmoothRollbackWhenCompleted`.    
> 修改方法:`isEnabledScrollToBottomAutoLoadMore` -> `isEnabledAutoLoadMore`.    
> 修改方法:`setEnableScrollToBottomAutoLoadMore` -> `setEnableAutoLoadMore`.    
> 修改方法:`isEnabledScrollToTopAutoRefresh` -> `isEnabledAutoRefresh`.    
> 修改方法:`setEnableScrollToTopAutoRefresh` -> `setEnableAutoRefresh`.    
> 修改方法:`setDurationOfBackToKeepTwoLevelHeaderViewPosition` -> `setDurationOfBackToKeepTwoLevel`.    
> 修改方法:`setDurationToCloseTwoLevelHeader` -> `setDurationToCloseTwoLevel`.    
> 修改方法:`setRatioOfHeaderHeightToHintTwoLevelRefresh` -> `setRatioOfHeaderToHintTwoLevel`.    
> 修改方法:`setRatioOfHeaderHeightToTwoLevelRefresh` -> `setRatioOfHeaderToTwoLevel`.    
> 修改方法:`setOffsetRatioToKeepTwoLevelHeaderWhileLoading` -> `setRatioToKeepTwoLevelHeader`.    
> 修改方法:`setOnChildNotYetInEdgeCannotMoveHeaderCallBack` -> `setOnHeaderEdgeDetectCallBack`.    
> 修改方法:`setOnChildNotYetInEdgeCannotMoveFooterCallBack` -> `setOnFooterEdgeDetectCallBack`.    
> 修改方法:`setOnFingerInsideAnotherDirectionViewCallback` -> `setOnInsideAnotherDirectionViewCallback`.    
> 重命名接口: `OnChildNotYetInEdgeCannotMoveHeaderCallBack` -> `OnHeaderEdgeDetectCallBack`.    
> 重命名接口: `OnChildNotYetInEdgeCannotMoveFooterCallBack` -> `OnFooterEdgeDetectCallBack`.    
> 重命名接口: `OnFingerInsideAnotherDirectionViewCallback` -> `OnInsideAnotherDirectionViewCallback`.    
## 1.6.2
> 支持拉伸内部视图功能，`Mode` 为 `MODE_DEFAULT`时为刷新控件用以操作Header/Footer，`Mode` 为 `MODE_SCALE`时为拉伸收缩控件用以操作内部视图（PS:竖向靠SmoothRefreshLayout支持/横向开HorizontalSmoothRefreshLayout支持，效果类似小米设置页拉伸效果）.    
> 添加了 `sr_mode`、 `mode_default`、`mode_scale` Xml属性.   
> 将外部可配置的常量和注解整合.    
> 支持只触发刷新动画.    
## 1.6.1.4
> 修复 `setErrorLayoutResId` 方法移除了错误的视图问题.    
> 重命名 `setEnabledCanNotInterruptScrollWhenRefreshCompleted` 为 `setEnableCanNotInterruptScrollWhenRefreshCompleted` 方法.    
> 重命名 `setEnabledInterceptEventWhileLoading` 为 `setEnableInterceptEventWhileLoading` 方法.    
> 添加 `setDisableLoadMoreWhenContentNotFull` 和 `isDisabledLoadMoreWhenContentNotFull` 方法，用以控制当内容视图未满屏时禁用加载更多.    
## 1.6.1.3
> 修复Attr冲突问题.    
> 添加自动刷新辅助工具到 `ext-utils` 包.    
> 添加了 `setEmptyLayoutResId` 和 `setErrorLayoutResId` 和 `setCustomLayoutResId` 方法.    
> 添加了 `OnFingerDownListener` 接口.    
> 修改内部实现，如果TargetView为空将不再抛出异常.
## 1.6.1.2
> 修改当刷新完成时，回滚动作是否能被触摸事件打断的逻辑为触发刷新完成（1.6.1.1条件为真实刷新完成才触发逻辑即:内部状态更改为刷新完成触发）.    
> 新增 `ILifecycleObserver` 接口，用以观察视图生命周期，为后续可能的工具预留接口.    
> 新增 `ext-utils` 包，现阶段添加了快速适配`AppBarLayout`的工具(`QuickConfigAppBarUtil`)，如视图为 `CoordinatorLayout` + `AppBarLayout` + `RecyclerView、AbsListView、ScrollView` 这类视图结构，现在只需使用本工具，配置少量代码即可完成配置.
## 1.6.1.1
> 添加 `isEnabledCanNotInterruptScrollWhenRefreshCompleted` 和 `setEnabledCanNotInterruptScrollWhenRefreshCompleted` 方法，用以控制当刷新完成时，回滚动作是否能被触摸事件打断.    
> 修复Fling的BUG.    
## 1.6.1
> 修改 `IRefreshView` 接口定义，当 `getCustomHeight` 方法返回值大于0时，Srl会以该值作为视图的高度进行后续操作.当值等于-1（MATCH_PARENT）时，Srl会将视图铺满布局.当值等于0时候不做处理以布局自适应.    
> 修改 `IRefreshView` 接口，统一 `indicator` 参数均为继承 `IIndicator` 的泛型 `T`.    
> 删除 `addView`相关的final关键字，修改相关实现.    
> 修改 `StoreHouseHeader` 的绘图问题.    
> 优化手势传递，使甩动手势更自然.   
> 修复刷新视图有Margin时，未正确布局的问题.     
> 修复当刷新视图样式不为 `STYLE_DEFAULT` 和 `STYLE_FOLLOW_CENTER` ，并且当前的移动距离大于Srl的高度时仍改变移动距离的问题.    
## 1.6.0
> 对现有项目进行分割，考虑到绝大多数都是自定义Header和Footer故拆分Core自带的2种风格Header和Footer到2个包（ext-classic和ext-material），至此Core包不再包含Header和Footer.如果需要可自行依赖.    
> 修改手势传递实现，使效果更自然.    
> 删除 `ext-horizontal` 包中的 `HorizontalMaterialHeader` 和 `HorizontalMaterialFooter`，将其移动到了demo中.    
> 添加了 `flingCompat` 方法，用以向下传递Fling动作.    
> 删除 `setOverScrollDurationRatio` 方法.    
> 修改越界回弹实现，优化效果.    
> 修改关闭Header和关闭Footer刷新功能实现，关闭时会检查是否处于刷新中，如果处于刷新中会重置默认状态.    
> 修改 `ClassicHeader` 和 `ClassicFooter` 实现，不再使用XML布局构建，直接使用代码生成布局结构，整合共有属性.
## 1.5.1.1
> 修改从Window移除时不再移除Handler内的所有消息.    
## 1.5.1
> 修复嵌套布局下手势失效问题.    
> 支持刷新视图可见情况下，手势向下传递.即当刷新视图高度未达到触发刷新高度时，手势能将刷新视图隐藏的同时继续传递手势到内容视图.       
## 1.5.0.5
> 修复未开启越界回弹情况下，手势甩动关闭刷新视图不起作用问题.    
## 1.5.0.3
> 修复多层嵌套下和多方向布局下事件处理可能的异常.    
## 1.5.0.2
> 修复嵌套滚动下可能存在的加载更多同步滚动被错误触发问题.    
> 添加 `setEnableCompatLoadMoreScroll` 方法，用以设置是否开启加载更多同步滚动，默认开启.    
> 添加 `getHeaderBackgroundColor` 方法，用以获取Header刷新高度区域内的背景色.    
> 添加 `setHeaderBackgroundColor` 方法，用以设置Header刷新高度区域内的背景色.    
> 添加 `sr_header_background_color`属性，用以设置Header刷新高度区域内的背景色.    
> 添加 `getFooterBackgroundColor` 方法，用以获取Footer刷新高度区域内的背景色.    
> 添加 `setFooterBackgroundColor` 方法，用以设置Footer刷新高度区域内的背景色.    
> 添加 `sr_footer_background_color`属性，用以设置Footer刷新高度区域内的背景色.    
> 修改方法 `setOnChildAlreadyInEdgeCanMoveHeaderCallBack` 为 `setOnChildNotYetInEdgeCannotMoveHeaderCallBack` ，保持含义和作用都对应原 `setOnChildScrollUpCallback` 方法.    
> 修改方法 `setOnChildAlreadyInEdgeCanMoveFooterCallBack` 为 `setOnChildNotYetInEdgeCannotMoveFooterCallBack` ，保持含义和作用都对应原 `setOnChildScrollDownCallback` 方法.    
> 修改接口 `OnChildAlreadyInEdgeCanMoveHeaderCallBack` 为 `OnChildNotYetInEdgeCannotMoveHeaderCallBack`，保持含义和作用都对应原 `OnChildScrollUpCallback` 接口.    
> 修改接口 `OnChildAlreadyInEdgeCanMoveFooterCallBack` 为 `OnChildNotYetInEdgeCannotMoveFooterCallBack`，保持含义和作用都对应原 `OnChildScrollDownCallback` 接口.     
> 修改方法 `isChildAlreadyInEdgeCanMoveHeader` 为 `isChildNotYetInEdgeCannotMoveHeader`，保持含义和作用都对应原 `canChildScrollUp` 方法.     
> 修改方法 `isChildAlreadyInEdgeCanMoveFooter` 为 `isChildNotYetInEdgeCannotMoveFooter`，保持含义和作用都对应原 `canChildScrollDown` 方法.    
## 1.5.0.1
> 修复布局Footer时错误的检查了Header的样式.    
> 优化移动刷新视图在 `STYLE_FOLLOW_PIN` 样式下的效率.
## 1.5.0
> 对现有项目进行分割，将二极刷新功能模块放入专门的包(ext-two-level)，新添加支持横向刷新模块(ext-horizontal)，原有核心模块作为基础模块引入(core).从而削减了库的大小.使用者可以根据具体业务需求依赖扩展模块.    
> 修改方法 `canChildScrollUp` 为 `isChildAlreadyInEdgeCanMoveHeader`.    
> 修改方法 `canChildScrollDown` 为 `isChildAlreadyInEdgeCanMoveFooter`.    
> 修改方法 `setOnChildScrollUpCallback` 为 `setOnChildAlreadyInEdgeCanMoveHeaderCallBack`.    
> 修改方法 `setOnChildScrollDownCallback` 为 `setOnChildAlreadyInEdgeCanMoveFooterCallBack`.    
> 修改接口 `OnChildScrollUpCallback` 为 `OnChildAlreadyInEdgeCanMoveHeaderCallBack`.    
> 修改接口 `OnChildScrollDownCallback` 为 `OnChildAlreadyInEdgeCanMoveFooterCallBack`.    
> 修改接口 `OnFingerInsideHorViewCallback` 为 `OnFingerInsideAnotherDirectionViewCallback`.    
> 修改方法 `setOnFingerInsideHorViewCallback` 为 `setOnFingerInsideAnotherDirectionViewCallback`.    
> 修改方法 `isEnableCheckFingerInsideHorView` 为 `isEnableCheckFingerInsideAnotherDirectionView`.    
> 修改方法 `setEnableCheckFingerInsideHorView` 为 `setEnableCheckFingerInsideAnotherDirectionView`.    
> 修改方法 `isDisabledWhenHorizontalMove` 为 `isDisabledWhenAnotherDirectionMove`.    
> 修改方法 `setDisableWhenHorizontalMove` 为 `setDisableWhenAnotherDirectionMove`.    
> 修改方法 `checkHorizontalViewUnInterceptedEvent` 为 `checkAnotherDirectionViewUnInterceptedEvent`.    
> 修改方法 `updateYPos` 为 `updatePos`.    
> 修改方法 `updateXPos` 为 `updateAnotherDirectionPos`.    
> 添加方法 `createIndicator`.
> 添加方法 `setIndicatorOffsetCalculator`，用以设置自定义便宜计算器实现更强的阻尼效果.   
> 添加方法 `isEnabledLoadMoreNoMoreDataNoNeedSpringBack`.     
> 添加方法 `setEnableLoadMoreNoMoreDataNoNeedSpringBack`，用以开启当无再多数据时是否不再回弹停留在最后移动位置.      
> 修改 `IIndicator` 接口中的方法 `getOffsetY` 为 `getOffset`.    
> 修改 `IIndicator` 接口中的方法 `getLastPosY` 为 `getLastPos`.    
> 修改 `IIndicator` 接口中的方法 `getCurrentPosY` 为 `getCurrentPos`.    
> 修改 `IIndicator` 接口中的方法 `setCurrentPosY` 为 `setCurrentPos`.    
> 修改 `IIndicator` 接口中的添加 `setOffsetCalculator（IOffsetCalculator calculator）` 方法.    
> 修改 `IIndicator` 接口中的添加 `IOffsetCalculator` 接口，用以设置自定义便宜计算器实现更强的阻尼效果.    
> 其他内部参数名的修改.
## 1.4.8.1
> 修正参数错误.分包前的最后一个版本.    
## 1.4.8
> 修改 `IRefreshView` 接口，新增 `STYLE_PIN`、`STYLE_FOLLOW_SCALE`、`STYLE_FOLLOW_PIN`、`STYLE_FOLLOW_CENTER` 四种新的样式.    
  `STYLE_PIN`:如果是Header则会固定在顶部，如果是Footer则会固定在底部.    
  `STYLE_FOLLOW_SCALE`:先纵向跟随TargetView移动，当移动的位置大于视图高度就动态改变视图高度.    
  `STYLE_FOLLOW_PIN`:先纵向跟随TargetView移动，当移动的位置大于视图高度就固定住.    
  `STYLE_FOLLOW_CENTER`:先纵向跟随TargetView移动，当移动的位置大于视图高度就让刷新视图处于中间位置但不改变视图大小.     
> 修改 `setEnableHeaderDrawerStyle` 和 `setEnableFooterDrawerStyle` 的实现.新版本需要之前的版本效果需要改变刷新视图的样式为 `STYLE_PIN`.    
> 添加 `IChangeStateAnimatorCreator` 接口和 `setChangeStateAnimatorCreator` 方法.现在可以自定义切换状态动画.    
> 修复 `DefaultIndicator` 中的参数错误.    
> 强化对同时有横向和纵向滚动视图时的滑动处理.    
> 将 `WaveHeader` 和 `WaveSmoothRefreshLayout` 从Lib中移除，移动到App中.    
> 精简了部分代码.    
## 1.4.7.3
> 修复setEnableOverScroll功能部分失效问题， #25.    
## 1.4.7.2
> 修复RecyclerView未满屏幕情况下Footer滑动问题， #25.    
## 1.4.7.1
> 修改二级刷新，添加 `autoTwoLevelRefreshHint(boolean smoothScroll， int stayDuration， boolean canBeInterrupted)`方法，可以设置提示是否能被打断.    
> 修复某些嵌套布局下触摸事件处理没有非常好的工作问题.    
## 1.4.7
> 修改二级刷新，修复 `IIndicator` 转换错误.    
> 删除支援Margin特性，提高效率.    
> 修复某些嵌套布局下触摸事件处理没有非常好的工作问题.    
> 重命名了一些内部方法名.    
## 1.4.6.1
> 修复嵌套布局下，Fling处理逻辑问题导致惯性消失问题.    
> 添加 `setOnFingerInsideHorViewCallback` 方法.    
> 添加 `setEnableCheckFingerInsideHorView` 方法.    
> 添加 `isEnableCheckFingerInsideHorView` 方法.    
> 添加 `OnFingerInsideHorViewCallback` 回调接口.    
> 以上添加的方法和接口用于检测手指按下时，触摸点是否位于水平滚动视图内.开启 `setDisableWhenHorizontalMove`和 `setEnableCheckFingerInsideHorView`后，将根据手指按下的触摸点是否位于水平滚动视图内进行触摸事件拦截处理，如果在内部，就拦截横向滑动，如果不在就拦截处理所有滑动事件.
## 1.4.6
> 实现即使Header或者Footer中的事务还没完成（例如Header处于刷新中），仍然可以滑动其他刷新视图.原逻辑为如果Header处于刷新中就无法再拉起Footer.    
> 在 `IRefreshView` 接口中添加 `onPureScrollPositionChanged`方法，用于当刷新视图事务未完成，移动其它视图会回调该方法，可以根据具体需求实现该状态下的视图展现.    
> 优化优化回弹效果.   
> 修复某些特殊情况下触摸事件没有很好的处理问题.     
> 合并部分重复逻辑代码.    
> 添加 `setSpringInterpolator` 方法，用以设置默认的滚动插值器.    
> 添加 `setOverScrollInterpolator` 方法，用以设置越界回弹时的滚动插值器.    
## 1.4.5
> 修改二级刷新，修改 `TwoLevelSmoothRefreshLayout` styleable `sr_enable_two_level_pull_to_refresh` 为 `sr_enable_two_level_refresh`.     
> 修改二级刷新，删除 `isEnabledTwoLevelPullToRefresh` 方法，添加 `isDisabledTwoLevelRefresh` 方法.    
> 修改二级刷新，删除 `setEnableTwoLevelPullToRefresh` 方法，添加 `isDisabledTwoLevelRefresh` 方法.    
> 修改二级刷新，修复 `setDurationOfBackToKeepTwoLeveHeaderViewPosition`方法名，应为 `setDurationOfBackToKeepTwoLevelHeaderViewPosition`.    
> 修改二级刷新，添加 `autoTwoLevelRefreshHint` 相关方法.    
> 修复 `WaveTextRefreshView` 中动画显示错位问题(7.0系统BUG);    
> 修复部分逻辑错误.    
## 1.4.4.1
> 修改 `onNestedPreScroll`方法实现，合并逻辑，提高效率和可读性.    
> 修改 `onNestedPreFling` 方法实现，提高效率.
## 1.4.4
> 优化优化回弹效果.    
> 修复某些情况下 `ViewTreeObserver` 的 `OnScrollChangedListener` 没有完全移除问题.    
> 修改 `MaterialSmoothRefreshLayout` 限制Header最大移动距离为1.5倍.    
> 删除 `setOverScrollDistanceRatio` 方法和 `mOverScrollDistanceRatio` 属性.    
> 添加 `setMinOverScrollDuration` 方法.    
> 修复某些特殊情况下，越界回弹未正常终止问题.    
> 删除RefreshCompleteHook中的弱引用，避免某些特殊情况下，引用被回收导致RefreshLayout内部状态异常问题.    
## 1.4.3
> 修改越界回弹实现，优化回弹效果.    
> 修复移动视图过程中触发刷新刷新逻辑问题（PS:2个判断都少写了括号）.    
> 修复偶尔 `WaveHeader` 圆环刷新时坐标异常问题.    
> 添加 `setOverScrollDurationRatio` 方法，用于设置回弹时长比例.    
> 添加 `setMaxOverScrollDuration` 方法，用于设置最大回弹时长.    
> 添加 `isEnabledScrollToTopAutoRefresh` 方法.    
> 添加 `setEnableScrollToTopAutoRefresh` 方法.用于开启到顶自动刷新.    
> 修复加载更多无更多数据没有正确设置问题（PS:即MASK的值错了，应为`0x07<<10`而不是`0x05<<10`）.    
> 修复若干触摸事件处理BUG.
## 1.4.1
> 修复状态视图填充LayoutParams不一致问题.    
> 修改包名为`me.dkzwm.widget.srl`，使包名更符合规范.    
> 修改二级刷新，删除 `setEnableBackToStartPosAtOnce`方法.    
> 修改二级刷新，添加 `setDurationOfBackToKeepTwoLeveHeaderViewPosition` 方法.    
> 修改二级刷新，添加 `setDurationToCloseTwoLevelHeader` 方法.    
> 修改二级刷新，添加 `setOffsetRatioToKeepTwoLevelHeaderWhileLoading` 方法.    
> 修改二级刷新，添加 `getOffsetToKeepTwoLevelHeaderWhileLoading` 方法.    
> 删除 `SmoothRefreshLayout` styleable中的 `sr_enable_two_level_pull_to_refresh` 属性.    
> 修改二级刷新，添加 `TwoLevelSmoothRefreshLayout` styleable，并添加 `sr_enable_two_level_pull_to_refresh` 属性.    
> 修改二级刷新，修改 `ITwoLevelIndicator` 继承 `IIndicator` 接口.    
> 修改二级刷新，在 `ITwoLevelIndicator` 中添加 `setOffsetRatioToKeepTwoLevelHeaderWhileLoading` 方法.    
> 修改二级刷新，在 `ITwoLevelIndicator` 中添加 `getOffsetToKeepTwoLevelHeaderWhileLoading` 方法.    
> 修改二级刷新，删除 `TwoLevelRefreshView` 接口参数中的 `IIndicator` 参数.    
> 添加 `WaveTextRefreshView`，存放于Demo的header中.    
> 更新英语Readme文件.    
> 修复静态刷新视图构造器在某些特定情况下未很好的工作问题.    
> 修复 `WaveHeader` 某些场景设置插值器不对问题.    
> 修复自动刷新可能被打断问题.    
## 1.4.0  
> 删除了MODE属性，功能性和其他属性有重复，导致逻辑复杂度直线上升，去掉后使用其他属性组合替代.    
> 修复未处于刷新状态调用refreshComplete()方法导致内部视图偏移问题.    
> 去掉多余资源，减小包体积.    
> 修改`setResistanceOfPullUp`方法为`setResistanceOfFooter`，使含义更明确.    
> 修改`setResistanceOfPullDown`方法为`setResistanceOfHeader`，使含义更明确.    
> 修改Xml `sr_resistance_of_pull_up`属性为 `sr_resistance_of_footer`，使含义更明确.    
> 修改Xml `sr_resistance_of_pull_down`属性为 `sr_resistance_of_header`，使含义更明确.    
> 修改`setDurationOfBackToRefreshViewHeight`方法为`setDurationOfBackToKeepRefreshViewPosition`，使含义更明确.    
> 修改Xml `sr_duration_of_back_to_refresh_height`属性为 `sr_duration_of_back_to_keep_refresh_pos`，使含义更明确.    
> 修改`getDurationOfBackToHeaderHeight`方法为`getDurationOfBackToKeepHeaderPosition`，使含义更明确.    
> 修改`setDurationOfBackToHeaderHeight`方法为`setDurationOfBackToKeepHeaderPosition`，使含义更明确.    
> 修改Xml `sr_duration_of_back_to_header_height`属性为 `sr_duration_of_back_to_keep_header_pos`，使含义更明确.    
> 修改`getDurationOfBackToFooterHeight`方法为`getDurationOfBackToKeepFooterPosition`，使含义更明确.    
> 修改`setDurationOfBackToFooterHeight`方法为`setDurationOfBackToKeepFooterPosition`，使含义更明确.    
> 修改Xml `sr_duration_of_back_to_footer_height`属性为 `sr_duration_of_back_to_keep_footer_pos`，使含义更明确.    
> 修改Xml `sr_layout_gravity`属性为 `layout_gravity`.    
> 删除Xml `sr_mode`属性.    
> 添加 `sr_enable_refresh`属性，设置是否启用下拉刷新.默认启用    
> 添加 `sr_enable_load_more`属性，设置是否启用加载更多.默认不启用.    
> 添加`setEnableHideHeaderView`方法.    
> 添加`isEnabledHideHeaderView`方法.   
> 添加`isEnabledHideFooterView`方法.   
> 添加`setEnableHideFooterView`方法.   
> 原有Mode对应改用如下属性组合替代.`NONE`->`setDisableRefresh(true)`+`setDisableLoadMore(true)`+`setEnableOverScroll(false)`;`REFRESH`->`setDisableRefresh(false)`+`setDisableLoadMore(true)`;
`LOAD_MORE`->`setDisableRefresh(true)`+`setDisableLoadMore(false)`;`OVER_SCROLL`->`setDisableRefresh(false)`+`setDisableLoadMore(false)`+`setDisablePerformRefresh(true)`+`setDisablePerformLoadMore(true)`+`setEnableHideHeaderView(true)`+`setEnableHideFooterView(true)`;`BOTH`->`setDisableRefresh(false)`+`setDisableLoadMore(false)`

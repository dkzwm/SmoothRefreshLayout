# SmoothRefreshLayout

[![Release](https://img.shields.io/badge/JCenter-1.6.6.8-brightgreen.svg)](https://bintray.com/dkzwm/maven/core)
![Methods](https://img.shields.io/badge/Methods%20%7C%20Size-737%20%7C%2076%20KB-e91e63.svg)
[![MinSdk](https://img.shields.io/badge/MinSdk-11-blue.svg)](https://developer.android.com/about/versions/android-3.0.html)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/LICENSE)

## English | [中文](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/README.md)

A highly efficient refresh library for Android.Can support all Views.It's easy to use and more powerful than SwipeRefreshLayout.    
Part of the open source library comes from [android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh).    
Thank him for providing such a great open source library ！

## Features:
 - Can support all Views, according to the specific needs to adaptation.
 - Support Multi-Touch.
 - Support Nested-Scroll feature, implemented the NestedScrollingChild2, NestedScrollingParent2 interface.
 - Extends the ViewGroup, has excellent performance,support similar FameLayout feature（Gravity、Margin).
 - Support Auto-Refresh、Auto-LoadMore、Scroll to bottom to Auto-LoadMore（Recommended to use the Adapter while use ListView/GridView/RecyclerView）、Scroll to top to Auto-Refresh.
 - Support Cross boundary rebound(OverScroll).
 - Support Horizontal refresh.
 - Support Drawer-Style(Refresh view below the Content view and Refresh view has been pinned).
 - Support Refresh view has six styles: STYLE_DEFAULT(Do not change the height by default)、 STYLE_SCALE(Dynamically change the height)、 STYLE_PIN(Pinned the refresh view)、 STYLE_FOLLOW_SCALE(When the moved position large than the view height, SmoothRefreshLayout will dynamically change the height)、 STYLE_FOLLOW_PIN(When the moved position large than the view height，pinned the refresh view)、 STYLE_FOLLOW_CENTER(When the moved position large than the view height, make refresh view in center) .    
 - Support Two-Level refresh（TwoLevelSmoothRefreshLayout）, PS:TaoBao-Senond-Floor、JD-Activity.
 - Support ListView, GridView, RecyclerView on LoadMore to smooth scrolling.
 - Support synchronous Fling gestures.
 - Support Scale effect, PS:XiaoMi phone settings page effects.    
 - Support Hrizontal Scale effect.    
 - Many callback interface and debugging information.
 
 ## Installation
 Add the following dependency to your build.gradle file:
 ```
 dependencies {
     implementation 'me.dkzwm.widget.srl:core:1.6.6.8'
     implementation 'me.dkzwm.widget.srl:ext-util:1.6.6.8'
     implementation 'me.dkzwm.widget.srl:ext-material:1.6.6.8'
     implementation 'me.dkzwm.widget.srl:ext-horizontal:1.6.6.8'
     implementation 'me.dkzwm.widget.srl:ext-classics:1.6.6.8'
     implementation 'me.dkzwm.widget.srl:ext-two-level:1.6.6.8'
    
     //androidX version
     implementation 'me.dkzwm.widget.srl:core:1.6.6.8.androidx'
     implementation 'me.dkzwm.widget.srl:ext-util:1.6.6.8.androidx'
     implementation 'me.dkzwm.widget.srl:ext-material:1.6.6.8.androidx'
     implementation 'me.dkzwm.widget.srl:ext-horizontal:1.6.6.8.androidx'
     implementation 'me.dkzwm.widget.srl:ext-classics:1.6.6.8.androidx'
     implementation 'me.dkzwm.widget.srl:ext-two-level:1.6.6.8.androidx'
 }
 ```

## Demo
Download [Demo.apk](https://raw.githubusercontent.com/dkzwm/SmoothRefreshLayout/master/apk/demo.apk)    

## Snapshots
- Test Scale effect    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_scale_effect.gif)

- Test Horizontal Refresh    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_horizontal_refresh.gif)

- Test Multi Direction Views    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_multi_direction_views.gif)

- Test QQ-WebBrowser Style    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_web_browser_style.gif)

- Test QQ-Activity Style    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_style.gif)

- Test Two-Level Refresh    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_two_level_refresh.gif)

- With RecyclerView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_recyclerView.gif)

- With CoordinatorLayout    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_recyclerView_in_coordinatorLayout.gif)

- Test Cross-Boundary-Rebound(OverScroll)     
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_overScroll.gif)

- Test Nested-Scroll   
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_nested_scroll.gif)

## How to use   
#### Use Xml to config
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
####  Use Java code to config
```
SmoothRefreshLayout refreshLayout = (SmoothRefreshLayout)findViewById(R.id.smoothRefreshLayout);
refreshLayout.setHeaderView(new ClassicHeader(this));
refreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
	@Override
	public void onRefreshing() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshLayout.refreshComplete();
			}
		}, 4000);
	}
});
```

#### Custom refresh view
##### Interface define
```
public interface IRefreshView<T extends IIndicator> {

    byte TYPE_HEADER = 0;
    byte TYPE_FOOTER = 1;

    byte STYLE_DEFAULT = 0;
    byte STYLE_SCALE = 1;
    //desc start
    //added in version 1.4.8
    byte STYLE_PIN = 2;
    byte STYLE_FOLLOW_SCALE = 3;
    byte STYLE_FOLLOW_PIN = 4;
    byte STYLE_FOLLOW_CENTER = 5;
    //desc end

    /**
     * Get the view type.
     *
     * @return type {@link #TYPE_HEADER}, {@link #TYPE_FOOTER}.
     */
    @RefreshViewType
    int getType();

    /**
     * Get the view style. If return {@link #STYLE_SCALE} SmoothRefreshLayout will dynamically
     * change the height, so the performance will be reduced. If return {@link #STYLE_FOLLOW_SCALE}
     * , when the moved position large than the view height, SmoothRefreshLayout will dynamically
     * change the height, so the performance will be reduced.
     *
     * @return style {@link #STYLE_DEFAULT}, {@link #STYLE_SCALE}, {@link #STYLE_PIN},
     * {@link #STYLE_FOLLOW_SCALE}, {@link #STYLE_FOLLOW_PIN}, {@link #STYLE_FOLLOW_CENTER}.
     */
    @RefreshViewStyle
    int getStyle();

    /**
     * Get the custom height,  When the return style is {@link #STYLE_SCALE} or
     * {@link #STYLE_FOLLOW_SCALE} , you must return a accurate height<br/>
     * Since version 1.6.1, If you want the height equal to the srl height, you can return `-1`
     * {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}
     *
     * @return Custom height
     */
    int getCustomHeight();

    /**
     * Get the target view.
     *
     * @return The returned view must be the view that will be added to the Layout
     */
    @NonNull
    View getView();

    /**
     * This method will be triggered when the touched finger is lifted.
     *
     * @param layout    The layout {@link SmoothRefreshLayout}
     * @param indicator The indicator {@link IIndicator}
     */
    void onFingerUp(SmoothRefreshLayout layout, T indicator);

    /**
     * This method will be triggered when the refresh state is reset to
     * {@link SmoothRefreshLayout#SR_STATUS_INIT}.
     *
     * @param layout The layout {@link SmoothRefreshLayout}
     */
    void onReset(SmoothRefreshLayout layout);

    /**
     * This method will be triggered when the frame is ready to refreshing.
     *
     * @param layout The layout {@link SmoothRefreshLayout}
     */
    void onRefreshPrepare(SmoothRefreshLayout layout);

    /**
     * This method will be triggered when the frame begin to refresh.
     *
     * @param layout    The layout {@link SmoothRefreshLayout}
     * @param indicator The indicator {@link IIndicator}
     */
    void onRefreshBegin(SmoothRefreshLayout layout, T indicator);

    /**
     * This method will be triggered when the frame is refresh completed.
     *
     * @param layout       The layout {@link SmoothRefreshLayout}
     * @param isSuccessful The layout refresh state
     */
    void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful);

    /**
     * This method will be triggered when the position of the refresh view changes.
     *
     * @param layout    The layout {@link SmoothRefreshLayout}
     * @param status    Current status @see{@link SmoothRefreshLayout#SR_STATUS_INIT},
     *                  {@link SmoothRefreshLayout#SR_STATUS_PREPARE},
     *                  {@link SmoothRefreshLayout#SR_STATUS_REFRESHING},
     *                  {@link SmoothRefreshLayout#SR_STATUS_LOADING_MORE},
     *                  {@link SmoothRefreshLayout#SR_STATUS_COMPLETE}.
     * @param indicator The indicator {@link IIndicator}
     */
    void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, T indicator);

    /**
     * Before the transaction of the refresh view has not yet been processed completed。
     * This method will be triggered when the position of the other refresh view changes.<br/>
     * <p>
     * Added in version 1.4.6
     *
     * @param layout    The layout {@link SmoothRefreshLayout}
     * @param status    Current status @see{@link SmoothRefreshLayout#SR_STATUS_INIT},
     *                  {@link SmoothRefreshLayout#SR_STATUS_PREPARE},
     *                  {@link SmoothRefreshLayout#SR_STATUS_REFRESHING},
     *                  {@link SmoothRefreshLayout#SR_STATUS_LOADING_MORE},
     *                  {@link SmoothRefreshLayout#SR_STATUS_COMPLETE}.
     * @param indicator The indicator {@link IIndicator}
     */
    void onPureScrollPositionChanged(SmoothRefreshLayout layout, byte status, T indicator);

}
```

##### Add custom refresh view
- Global static code construction    
```    
        SmoothRefreshLayout.setDefaultCreator(new IRefreshViewCreator() {
            @Override
            public IRefreshView<IIndicator> createHeader(SmoothRefreshLayout layout) {
                ClassicHeader header = new ClassicHeader(layout.getContext());
                return header;
            }

            @Override
            public IRefreshView<IIndicator> createFooter(SmoothRefreshLayout layout) {
                ClassicFooter footer = new ClassicFooter(layout.getContext());
                return footer;
            }
        });
```   

- Use Java code to config
```    
        ClassicHeader header = new ClassicHeader(mRefreshLayout.getContext());
        mRefreshLayout.setHeaderView(header);
        ClassicFooter footer = new ClassicFooter(mRefreshLayout.getContext());
        mRefreshLayout.setFooterView(footer);
```    

- Please write directly to the Xml file,SmoothRefreshLayout will find the view that impl the IRefreshView interface to added inside.
 
#### Impl the QQ pull down damping effect(Custom offset calculator)
 ```
        mRefreshLayout.setIndicatorOffsetCalculator(new IIndicator.IOffsetCalculator() {
            @Override
            public float calculate(@IIndicator.MovingStatus int status, int currentPos, float offset) {
                if (status == IIndicator.MOVING_HEADER) {
                    if (offset < 0) {
                        return offset;
                    }
                    return (float) Math.pow(Math.pow(currentPos / 2, 1.28d) + offset, 1 / 1.28d) * 2 - currentPos;
                } else if (status == IIndicator.MOVING_FOOTER) {
                    if (offset > 0) {
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
 #### Xml attr 
 ##### SmoothRefreshLayout 
 |Name|Format|Desc|
 |:---:|:---:|:---:|
 |sr_content|reference|Specifies the resource ID of the content view|
 |sr_resistance|float|The resistance while you are moving（Default: `1.65f`）|
 |sr_resistanceOfFooter|float|The resistance while you are moving Footer（Default: `1.65f`）|
 |sr_resistanceOfHeader|float|The resistance while you are moving Header（Default: `1.65f`）|
 |sr_ratioToRefresh|float|Trigger refresh of the height ratio（Default: `1f`）|
 |sr_ratioOfHeaderToRefresh|float|Trigger refresh of the Header height ratio（Default: `1f`）|
 |sr_ratioOfFooterToRefresh|float|Trigger refresh of the Footer height ratio（Default: `1f`）|
 |sr_ratioToKeep|float|The offset of keep view in refreshing occupies the height ratio of the refresh view（Default: `1f`）|
 |sr_ratioToKeepHeader|float|The offset of keep Header in refreshing occupies the height ratio of the Header（Default: `1f`）|
 |sr_ratioToKeepFooter|float|The offset of keep Footer in refreshing occupies the height ratio of the Footer（Default: `1f`）|
 |sr_maxMoveRatio|float|The max can move offset occupies the height ratio of the refresh view（Default: `0f`，meaning that it will never work）|
 |sr_maxMoveRatioOfHeader|float|The max can move offset occupies the height ratio of the Header（Default: `0f`，meaning that it will never work）|
 |sr_maxMoveRatioOfFooter|float|The max can move offset occupies the height ratio of the Footer（Default: `0f`，meaning that it will never work）|
 |sr_closeDuration|integer|The duration of return to the start position（Default: `350`）|
 |sr_closeHeaderDuration|integer|The duration of return to the start position when Header moves（Default: `350`）|
 |sr_closeFooterDuration|integer|The duration of return to the start position when Footer moves（Default: `350`）|
 |sr_backToKeepDuration|integer|The duration of return to the keep refresh view position（Default: `200`）|
 |sr_backToKeepHeaderDuration|integer|The duration of return to the keep refresh view position when Header moves（Default: `200`）|
 |sr_backToKeepFooterDuration|integer|The duration of return to the keep refresh view position when Footer moves（Default: `200`）|
 |sr_enablePinContent|boolean|Pinned the content view（Default: `false`）|
 |sr_enableKeep|boolean|Keep refresh view in refreshing（Default: `true`）|
 |sr_enablePullToRefresh|boolean|Pull to refresh（Default: `false`，meaning release to refresh）|
 |sr_enableOverScroll|boolean|OverScroll（Default: `true`|
 |sr_enableRefresh|boolean|Enable Header refresh（Default: `ture`）|
 |sr_enableLoadMore|boolean|Enable Footer refresh（Default: `false`）|
 |sr_headerBackgroundColor|color|Set the background color of the height of the Header view|
 |sr_footerBackgroundColor|color|Set the background color of the height of the Footer view|
 |sr_mode|enum|Set current mode (Default:`MODE_DEFAULT` as refresh layout)|
 |sr_stickyHeader|reference|Specifies the resource ID of the sticky header|
 |sr_stickyFooter|reference|Specifies the resource ID of the sticky footer|

 ##### TwoLevelSmoothRefreshLayout
 |Name|Format|Desc|
 |:---:|:---:|:---:|
 |sr_enableTwoLevelRefresh|boolean|Enable Two-Level refresh（Default: `true`）|
 |sr_backToKeep2Duration|boolean|Set the duration of to keep Two-Level refresh view position when Header moves（Default: `500`）|
 |sr_closeHeader2Duration|boolean|Set the duration for closing the Two-Level refresh（Default: `500`）|

 ##### The other views in SmoothRefreshLayout 
 |Name|Format|Desc|
 |:---:|:---:|:---:|
 |layout_gravity|flag|Layout gravity (not targetView、not refreshView)|
 
 #### SmoothRefreshLayout methods
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |setHeaderView|IRefreshView|Set Header|
 |setFooterView|IRefreshView|Set Footer|
 |setContentView|View|Set content view|
 |setMode|int|Set current mode|
 |setDisableWhenAnotherDirectionMove|boolean|Set whether to filter another direction moves（Default: `false`）|
 |setEnableNextPtrAtOnce|boolean|Set whether user can perform next PTR at once|
 |setMaxOverScrollDuration|int|Set the max duration for Cross-Boundary-Rebound(OverScroll)（Default: `350`）|
 |setMinOverScrollDuration|int|Set the min duration for Cross-Boundary-Rebound(OverScroll)（Default: `100`）|
 |setResistance|float|The resistance while you are moving（Default: `1.65f`）|
 |setResistanceOfFooter|float|The resistance while you are moving Footer（Default: `1.65f`）|
 |setResistanceOfHeader|float|The resistance while you are moving Header（Default: `1.65f`）|
 |setRatioToRefresh|float|Trigger refresh of the height ratio（Default: `1.1f`）|
 |setRatioOfHeaderToRefresh|float|Trigger refresh of the Header height ratio（Default: `1.1f`）|
 |setRatioOfFooterToRefresh|float|Trigger refresh of the Footer height ratio（Default: `1.1f`）|
 |setRatioToKeep|float|The offset of keep view in refreshing occupies the height ratio of the refresh view（Default: `1f`）|
 |setRatioToKeepHeader|float|The offset of keep Header in refreshing occupies the height ratio of the Header（Default: `1f`）|
 |setRatioToKeepFooter|float|The offset of keep Header in refreshing occupies the height ratio of the Footer（Default: `1f`）|
 |setMaxMoveRatio|float|The max can move offset occupies the height ratio of the refresh view（Default: `0f`，meaning that it will never work）|
 |setMaxMoveRatioOfHeader|float|The max can move offset occupies the height ratio of the Header（Default: `0f`，meaning that it will never work）|
 |setMaxMoveRatioOfFooter|float|The max can move offset occupies the height ratio of the Footer（Default: `0f`，meaning that it will never work）|
 |setDurationToClose|int|The duration of return to the start position（Default: `350`）|
 |setDurationToCloseHeader|int|The duration of return to the start position when Header moves（Default: `350`）|
 |setDurationToCloseFooter|int|The duration of return to the start position when Footer moves（Default: `350`）|
 |setDurationOfBackToKeep|integer|The duration of return to the keep refresh view position（Default: `200`）|
 |setDurationOfBackToKeepHeader|integer|The duration of return to the keep refresh view position when Header moves（Default: `200`）|
 |setDurationOfBackToKeepFooter|integer|The duration of return to the keep refresh view position when Footer moves（Default: `200`）|
 |setEnablePinContentView|boolean|Pinned the content view（Default: `false`）|
 |setEnablePullToRefresh|boolean|Pull to refresh（Default: `false`，meaning release to refresh）|
 |setEnableOverScroll|boolean|Enable OverScroll（Default: `true`）|
 |setEnableInterceptEventWhileLoading|boolean|Enable intercept the touch event while loading（Default: `false`）|
 |setEnableHeaderDrawerStyle|boolean|Enable Header below the content view（Default: `false`）|
 |setEnableFooterDrawerStyle|boolean|Enable Footer below the content view（Default: `false`）|
 |setDisablePerformRefresh|boolean|Disable Header perform refresh（Default: `false`）|
 |setDisablePerformLoadMore|boolean|Disable Footer perform refresh（Default: `false`）|
 |setEnableNoMoreData|boolean|Set the Footer without more data（Default: `false`）|
 |setEnableNoSpringBackWhenNoMoreData|boolean|Set when Footer has no more data to no longer need spring back|
 |setDisableRefresh|boolean|Disable Header refresh（Default: `false`）|
 |setDisableLoadMore|boolean|Disable Footer refresh（Default: `false`）|
 |setEnableKeepRefreshView|boolean|Keep refresh view in refreshing（Default: `true`）|
 |setEnableAutoLoadMore|boolean|When content view scrolling to bottom, It will be perform load more（Default: `false`）|
 |setEnablePinRefreshViewWhileLoading|boolean|The refresh view will pinned at the keep refresh position（Default: `false`）|
 |setSpringInterpolator|Interpolator|Set spring interpolator|
 |setOverScrollInterpolator|Interpolator|Set OverScroll interpolator|
 |setEnableCheckInsideAnotherDirectionView|boolean|Enable whether the finger pressed point is inside another direction view，you must set `setDisableWhenAnotherDirectionMove(true)`|
 |setEnableCompatLoadMoreScroll|boolean|Set whether to turn on the synchronized scroll when Footer loading（Default: `true`）|
 |setHeaderBackgroundColor|int|Set the background color of the height of the Header view|
 |setFooterBackgroundColor|int|Set the background color of the height of the Footer view|
 |setEnableSmoothRollbackWhenCompleted|boolean|Set the scroller rollback can not be interrupted when refresh completed|
 |setDisableLoadMoreWhenContentNotFull|boolean|Load more will be disabled when the content is not full|
 |setEnableDynamicEnsureTargetView|boolean|Dynamic search the target view|
 |setEnableOldTouchHandling|boolean|Enabled the old touch handling logic|
 |setLoadMoreScrollTargetView|View|Set Footer refresh scroll target view|
 
 #### SmoothRefreshLayout callbacks
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |setOnRefreshListener|T extends OnRefreshListener|Set the listener to be notified when a refresh is triggered|
 |addLifecycleObserver|ILifecycleObserver|Add a lifecycle callback|
 |addOnStatusChangedListener|OnStatusChangedListener|Set the listener to be notified when the Status changed|
 |addOnUIPositionChangedListener|OnUIPositionChangedListener|Add a listener to listen the views position change event|
 |setOnLoadMoreScrollCallback|OnLoadMoreScrollCallback|Set a scrolling callback when loading more|
 |setOnPerformAutoRefreshCallBack|OnPerformAutoRefreshCallBack|Set a callback to make sure you need to customize the specified trigger the auto refresh rule|
 |setOnPerformAutoLoadMoreCallBack|OnPerformAutoLoadMoreCallBack|Set a callback to make sure you need to customize the specified trigger the auto load more rule| |setOnHeaderEdgeDetectCallBack|OnHeaderEdgeDetectCallBack|Set a callback to check if the content view is in edge can move Header| |setOnFooterEdgeDetectCallBack|OnFooterEdgeDetectCallBack|Set a callback to check if the content view is in edge can move Footer|
 |setOnHookHeaderRefreshCompleteCallback|OnHookUIRefreshCompleteCallBack|Set a hook callback when the Header refresh complete event be triggered|
 |setOnHookFooterRefreshCompleteCallback|OnHookUIRefreshCompleteCallBack|Set a hook callback when the Footer refresh complete event be triggered|
 |setOnInsideAnotherDirectionViewCallback|OnInsideAnotherDirectionViewCallback|Set a callback to check the finger pressed point whether inside another direction view|
 
 #### SmoothRefreshLayout others
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |setDefaultCreator（static）|IRefreshViewCreator|Set the static refresh view creator|
 |refreshComplete|None|Refresh complete|
 |refreshComplete|boolean|Refresh complete, parameter: whether the refresh was completed successfully|
 |refreshComplete|boolean,long|Refresh complete, parameter1: whether the refresh was completed successfully, parameter2: set the time for the delay to reset the refresh state|
 |refreshComplete|long|Refresh complete, parameter: set the time for the delay to reset the refresh state|
 |setLoadingMinTime|long|Set the minimum time difference between the start refresh and the end refresh (Default: `500`)|
 |autoRefresh|None|Auto trigger Header refresh|
 |autoRefresh|boolean|Auto trigger Header refresh, parameter: trigger immediately|
 |autoRefresh|boolean,boolean|Auto trigger Header refresh, parameter1: trigger immediately, parameter2: whether use scroll|
 |autoLoadMore|None|Auto trigger Footer refresh|
 |autoLoadMore|boolean|Auto trigger Footer refresh, parameter: trigger immediately|
 |autoLoadMore|boolean,boolean|Auto trigger Footer refresh, parameter1: trigger immediately, parameter2: whether use scroll|

 #### TwoLevelSmoothRefreshLayout methods
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |setRatioOfHeaderToHintTwoLevel|float|Set the height ratio of Header to trigger Two-Level refresh hint|
 |setRatioOfHeaderToTwoLevel|float|Set the height ratio of Header to trigger Two-Level refresh|
 |setRatioToKeepTwoLevelHeader|float|The offset of keep Header in Two-Level refreshing occupies the height ratio of the Header（Default: `1f`）|
 |setDisableTwoLevelRefresh|boolean|Whether disable Two-Level refresh（Default: `false`）|
 |setDurationOfBackToKeepTwoLevel|int|The duration of return to the keep Two-Level refresh view position when Header moves（Default: `500`）|
 |setDurationToCloseTwoLevel|int|The duration of return to the start position when Header moves（Default: `500`）|

 #### TwoLevelSmoothRefreshLayout others
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |autoTwoLevelRefreshHint|None|Auto trigger Two-Level refresh|
 |autoTwoLevelRefreshHint|int|Auto trigger Two-Level refresh，parameter: how long to stay|
 |autoTwoLevelRefreshHint|boolean|Auto trigger Two-Level refresh，parameter: whether use scroll|
 |autoTwoLevelRefreshHint|boolean,int|Auto trigger Two-Level refresh, parameter1: whether use scroll，parameter2: how long to stay|
 |autoTwoLevelRefreshHint|boolean,int,boolean|Auto trigger Two-Level refresh, parameter1: whether use scroll，parameter2: how long to stay，parameter3: whether it can be interrupted by touch|
 
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
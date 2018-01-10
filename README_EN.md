# SmoothRefreshLayout

[![Release](https://jitpack.io/v/com.github.dkzwm/SmoothRefreshLayout.svg)](https://jitpack.io/#dkzwm/SmoothRefreshLayout)
[![Methods](https://img.shields.io/badge/Methods%20%7C%20Size-732%20%7C%2073%20KB-e91e63.svg)](http://www.methodscount.com/?lib=com.github.dkzwm.SmoothRefreshLayout%3Acore%3A1.6.1.3)
[![MinSdk](https://img.shields.io/badge/MinSdk-11-blue.svg)](https://developer.android.com/about/versions/android-3.0.html)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/LICENSE)

## English | [中文](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/README.md)

A highly efficient refresh library for Android.Can support all Views.It's easy to use and more powerful than SwipeRefreshLayout.    
Part of the open source library comes from [android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh).    
Thank him for providing such a great open source library ！

## Features:
 - Can support all Views, according to the specific needs to adaptation.
 - Support Multi-Touch.
 - Support Nested-Scroll feature, implemented the NestedScrollingChild, NestedScrollingParent interface.
 - Extends the ViewGroup, has excellent performance,support similar FameLayout feature（Gravity、Margin).
 - Support Auto-Refresh、Auto-LoadMore、Scroll to bottom to Auto-LoadMore（Recommended to use the Adapter while use ListView/GridView/RecyclerView）.
 - Support Cross boundary rebound(OverScroll).
 - Support Horizontal refresh.
 - Support Drawer-Style(Refresh view below the Content view and Refresh view has been pinned).
 - Support Refresh view has six styles: STYLE_DEFAULT(Do not change the height by default)、 STYLE_SCALE(Dynamically change the height)、 STYLE_PIN(Pinned the refresh view)、 STYLE_FOLLOW_SCALE(When the moved position large than the view height, SmoothRefreshLayout will dynamically change the height)、 STYLE_FOLLOW_PIN(When the moved position large than the view height，pinned the refresh view)、 STYLE_FOLLOW_CENTER(When the moved position large than the view height, make refresh view in center) .    
 - Support Two-Level refresh（TwoLevelSmoothRefreshLayout）, PS:TaoBao-Senond-Floor、JD-Activity.
 - Support ListView, GridView, RecyclerView on LoadMore to smooth scrolling.
 - Support Multi-State: STATE_CONTENT(Default state)、 STATE_ERROR(Error state)、 STATE_EMPTY(Empty state)、 STATE_CUSTOM(Custom state).
 - Support synchronous Fling gestures.
 - Many callback interface and debugging information.

## Demo
Download [Demo.apk](https://raw.githubusercontent.com/dkzwm/SmoothRefreshLayout/master/apk/demo.apk)    

## Snapshots
- Test Horizontal Refresh（Added since version 1.5.0）    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_horizontal_refresh.gif)

- Test Horizontal RecyclerView Refresh（Added since version 1.5.0）    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_horizontal_recyclerView.gif)

- Test multi direction views    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_multi_direction_views.gif)

- Test QQ-WebBrowser style    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_web_browser_style.gif)

- Test QQ-Activity style    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_style.gif)

- Test Two-Level refresh    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_two_level_refresh.gif)
 
- With ListView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_listView.gif)

- With GridView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_gridView.gif)

- With RecyclerView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_recyclerView.gif)

- With ViewPager    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_viewPager.gif)

- With CoordinatorLayout    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_recyclerView_in_coordinatorLayout.gif)

- Test cross boundary rebound(OverScroll)     
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_overScroll.gif)

- Test Nested-Scroll   
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_nested_scroll.gif)

## How to use   
#### Gradle
```
repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    //The most basic core library
    compile 'com.github.dkzwm.SmoothRefreshLayout:core:1.6.1.3'
    //Default Classic-Style impl
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-classic:1.6.1.3'
    //Default Material-Style impl
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-material:1.6.1.3'
    //Uitls library
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-material:1.6.1.3'
    //Support Two-Level-Refresh feature
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-two-level:1.6.1.3'
    //Support horizontal refresh feature
    compile 'com.github.dkzwm.SmoothRefreshLayout:ext-horizontal:1.6.1.3'
}
```
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
 |sr_resistance_of_footer|float|The resistance while you are moving Footer（Default: `1.65f`）|
 |sr_resistance_of_header|float|The resistance while you are moving Header（Default: `1.65f`）|
 |sr_ratio_of_refresh_height_to_refresh|float|Trigger refresh of the height ratio（Default: `1.1f`）|
 |sr_ratio_of_header_height_to_refresh|float|Trigger refresh of the Header height ratio（Default: `1.1f`）|
 |sr_ratio_of_footer_height_to_refresh|float|Trigger refresh of the Footer height ratio（Default: `1.1f`）|
 |sr_offset_ratio_to_keep_refresh_while_Loading|float|The offset of keep view in refreshing occupies the height ratio of the refresh view（Default: `1f`）|
 |sr_offset_ratio_to_keep_header_while_Loading|float|The offset of keep Header in refreshing occupies the height ratio of the Header（Default: `1f`）|
 |sr_offset_ratio_to_keep_footer_while_Loading|float|The offset of keep Footer in refreshing occupies the height ratio of the Footer（Default: `1f`）|
 |sr_can_move_the_max_ratio_of_refresh_height|float|The max can move offset occupies the height ratio of the refresh view（Default: `0f`，meaning that it will never work）|
 |sr_can_move_the_max_ratio_of_header_height|float|The max can move offset occupies the height ratio of the Header（Default: `0f`，meaning that it will never work）|
 |sr_can_move_the_max_ratio_of_footer_height|float|The max can move offset occupies the height ratio of the Footer（Default: `0f`，meaning that it will never work）|
 |sr_duration_to_close_of_refresh|integer|The duration of return to the start position（Default: `500`）|
 |sr_duration_to_close_of_header|integer|The duration of return to the start position when Header moves（Default: `500`）|
 |sr_duration_to_close_of_footer|integer|The duration of return to the start position when Footer moves（Default: `500`）|
 |sr_duration_of_back_to_keep_refresh_pos|integer|The duration of return to the keep refresh view position（Default: `200`）|
 |sr_duration_of_back_to_keep_header_pos|integer|The duration of return to the keep refresh view position when Header moves（Default: `200`）|
 |sr_duration_of_back_to_keep_header_pos|integer|The duration of return to the keep refresh view position when Footer moves（Default: `200`）|
 |sr_enable_pin_content|boolean|Pinned the content view（Default: `false`）|
 |sr_enable_keep_refresh_view|boolean|Keep refresh view in refreshing（Default: `true`）|
 |sr_enable_pull_to_refresh|boolean|Pull to refresh（Default: `false`，meaning release to refresh）|
 |sr_enable_over_scroll|boolean|OverScroll（Default: `true`|
 |sr_empty_layout|reference|Specifies the layout resource ID in Empty state|
 |sr_error_layout|reference|Specifies the layout resource ID in Error state|
 |sr_custom_layout|reference|Specifies the layout resource ID in Custom state|
 |sr_state|enum|Set current state （Default: `STATE_CONTENT`）|
 |sr_enable_refresh|boolean|Enable Header refresh（Default: `ture`）|
 |sr_enable_load_more|boolean|Enable Footer refresh（Default: `false`）|
 |sr_header_background_color|color|Set the background color of the height of the Header view|
 |sr_footer_background_color|color|Set the background color of the height of the Footer view|

 ##### TwoLevelSmoothRefreshLayout
 |Name|Format|Desc|
 |:---:|:---:|:---:|
 |sr_enable_two_level_refresh|boolean|Enable Two-Level refresh（Default: `true`）|

 ##### The other views in SmoothRefreshLayout 
 |Name|Format|Desc|
 |:---:|:---:|:---:|
 |layout_gravity|flag|Layout gravity (not targetView、not refreshView)|
 
 #### SmoothRefreshLayout methods
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |setHeaderView|IRefreshView|Set Header|
 |setFooterView|IRefreshView|Set Footer|
 |setContentView|int,View|Set content view, parameter1: set current State, parameter2: set the State content view|
 |setState|int|Set current State|
 |setState|int,boolean|Set current State, parameter1: set current State, parameter2: whether use animate|
 |setDisableWhenAnotherDirectionMove|boolean|Set whether to filter another direction moves（Default: `false`）|
 |setEnableNextPtrAtOnce|boolean|Set whether user can perform next PTR at once|
 |setMaxOverScrollDuration|int|Set the max duration for Cross-Boundary-Rebound(OverScroll)（Default: `500`）|
 |setMinOverScrollDuration|int|Set the min duration for Cross-Boundary-Rebound(OverScroll)（Default: `150`）|
 |setResistance|float|The resistance while you are moving（Default: `1.65f`）|
 |setResistanceOfFooter|float|The resistance while you are moving Footer（Default: `1.65f`）|
 |setResistanceOfHeader|float|The resistance while you are moving Header（Default: `1.65f`）|
 |setRatioOfRefreshViewHeightToRefresh|float|Trigger refresh of the height ratio（Default: `1.1f`）|
 |setRatioOfHeaderHeightToRefresh|float|Trigger refresh of the Header height ratio（Default: `1.1f`）|
 |setRatioOfFooterHeightToRefresh|float|Trigger refresh of the Footer height ratio（Default: `1.1f`）|
 |setOffsetRatioToKeepRefreshViewWhileLoading|float|The offset of keep view in refreshing occupies the height ratio of the refresh view（Default: `1f`）|
 |setOffsetRatioToKeepHeaderWhileLoading|float|The offset of keep Header in refreshing occupies the height ratio of the Header（Default: `1f`）|
 |setOffsetRatioToKeepFooterWhileLoading|float|The offset of keep Header in refreshing occupies the height ratio of the Footer（Default: `1f`）|
 |setCanMoveTheMaxRatioOfRefreshViewHeight|float|The max can move offset occupies the height ratio of the refresh view（Default: `0f`，meaning that it will never work）|
 |setCanMoveTheMaxRatioOfHeaderHeight|float|The max can move offset occupies the height ratio of the Header（Default: `0f`，meaning that it will never work）|
 |setCanMoveTheMaxRatioOfFooterHeight|float|The max can move offset occupies the height ratio of the Footer（Default: `0f`，meaning that it will never work）|
 |setDurationToClose|int|The duration of return to the start position（Default: `500`）|
 |setDurationToCloseHeader|int|The duration of return to the start position when Header moves（Default: `500`）|
 |setDurationToCloseFooter|int|The duration of return to the start position when Footer moves（Default: `500`）|
 |setDurationOfBackToKeepRefreshViewPosition|integer|The duration of return to the keep refresh view position（Default: `200`）|
 |setDurationOfBackToKeepHeaderPosition|integer|The duration of return to the keep refresh view position when Header moves（Default: `200`）|
 |setDurationOfBackToKeepFooterPosition|integer|The duration of return to the keep refresh view position when Footer moves（Default: `200`）|
 |setEnablePinContentView|boolean|Pinned the content view（Default: `false`）|
 |setEnabledPullToRefresh|boolean|Pull to refresh（Default: `false`，meaning release to refresh）|
 |setEnableOverScroll|boolean|Enable OverScroll（Default: `true`）|
 |setEnabledInterceptEventWhileLoading|boolean|Enable intercept the touch event while loading（Default: `false`）|
 |setEnableHeaderDrawerStyle|boolean|Enable Header below the content view（Default: `false`）|
 |setEnableFooterDrawerStyle|boolean|Enable Footer below the content view（Default: `false`）|
 |setDisablePerformRefresh|boolean|Disable Header perform refresh（Default: `false`）|
 |setDisablePerformLoadMore|boolean|Disable Footer perform refresh（Default: `false`）|
 |setEnableLoadMoreNoMoreData|boolean|Set the Footer without more data（Default: `false`）|
 |isEnabledLoadMoreNoMoreDataNoNeedSpringBack|boolean|Set when Footer has no more data to no longer need spring back|
 |setDisableRefresh|boolean|Disable Header refresh（Default: `false`）|
 |setDisableLoadMore|boolean|Disable Footer refresh（Default: `false`）|
 |setEnableKeepRefreshView|boolean|Keep refresh view in refreshing（Default: `true`）|
 |setEnableScrollToBottomAutoLoadMore|boolean|When content view scrolling to bottom, It will be perform load more（Default: `false`）|
 |setEnablePinRefreshViewWhileLoading|boolean|The refresh view will pinned at the keep refresh position（Default: `false`）|
 |setSpringInterpolator|Interpolator|Set spring interpolator|
 |setOverScrollInterpolator|Interpolator|Set OverScroll interpolator|
 |setEnableCheckFingerInsideAnotherDirectionView|boolean|Enable whether the finger pressed point is inside another direction view，you must set `setDisableWhenAnotherDirectionMove(true)`|
 |setEnableCompatLoadMoreScroll|boolean|Set whether to turn on the synchronized scroll when Footer loading（Default: `true`）|
 |setHeaderBackgroundColor|int|Set the background color of the height of the Header view|
 |setFooterBackgroundColor|int|Set the background color of the height of the Footer view|
 |setEnabledCanNotInterruptScrollWhenRefreshCompleted|boolean|Set the scroller rollback can not be interrupted when refresh completed|
 
 #### SmoothRefreshLayout callbacks
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |setOnRefreshListener|T extends OnRefreshListener|Set the listener to be notified when a refresh is triggered|
 |setOnStateChangedListener|OnStateChangedListener|Set the listener to be notified when the State changed|
 |setChangeStateAnimatorCreator|IChangeStateAnimatorCreator|Set the change State animator creator|
 |addOnUIPositionChangedListener|OnUIPositionChangedListener|Add a listener to listen the views position change event|
 |removeOnUIPositionChangedListener|OnUIPositionChangedListener|Remove the listener to listen the views position change event|
 |setOnLoadMoreScrollCallback|OnLoadMoreScrollCallback|Set a scrolling callback when loading more|
 |setOnPerformAutoLoadMoreCallBack|OnPerformAutoLoadMoreCallBack|Set a callback to make sure you need to customize the specified trigger the auto load more rule| |setOnChildNotYetInEdgeCannotMoveHeaderCallBack|OnChildNotYetInEdgeCannotMoveHeaderCallBack|Set a callback to check if the content view is in edge can move Header| |setOnChildNotYetInEdgeCannotMoveFooterCallBack|OnChildNotYetInEdgeCannotMoveFooterCallBack|Set a callback to check if the content view is in edge can move Footer|
 |setOnHookHeaderRefreshCompleteCallback|OnHookUIRefreshCompleteCallBack|Set a hook callback when the Header refresh complete event be triggered|
 |setOnHookFooterRefreshCompleteCallback|OnHookUIRefreshCompleteCallBack|Set a hook callback when the Footer refresh complete event be triggered|
 |setOnFingerInsideAnotherDirectionViewCallback|OnFingerInsideAnotherDirectionViewCallback|Set a callback to check the finger pressed point whether inside another direction view|
 
 #### SmoothRefreshLayout others
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |debug（static）|boolean|Debug|
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
 |setLoadMoreScrollTargetView|View|Set Footer refresh scroll target view|

 #### TwoLevelSmoothRefreshLayout methods
 |Name|Params|Desc|
 |:---:|:---:|:---:|
 |setRatioOfHeaderHeightToHintTwoLevelRefresh|float|Set the height ratio of Header to trigger Two-Level refresh hint|
 |setRatioOfHeaderHeightToTwoLevelRefresh|float|Set the height ratio of Header to trigger Two-Level refresh|
 |setOffsetRatioToKeepTwoLevelHeaderWhileLoading|float|The offset of keep Header in Two-Level refreshing occupies the height ratio of the Header（Default: `1f`）|
 |setDisableTwoLevelRefresh|boolean|Whether disable Two-Level refresh（Default: `false`）|
 |setDurationOfBackToKeepTwoLevelHeaderViewPosition|int|The duration of return to the keep Two-Level refresh view position when Header moves（Default: `500`）|
 |setDurationToCloseTwoLevelHeader|int|The duration of return to the start position when Header moves（Default: `500`）|

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
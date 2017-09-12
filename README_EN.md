# SmoothRefreshLayout
## English | [中文](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/README.md)

A highly efficient refresh library for Android.Can support all Views.It's easy to use and more powerful than SwipeRefreshLayout.    
Part of the open source library comes from [android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh).    
Thank him for providing such a great open source library ！

## Features:
 - Can support all Views, according to the specific needs to adaptation.
 - Support Nested-Scroll feature, implemented the NestedScrollingChild, NestedScrollingParent interface.
 - Extends the ViewGroup,has excellent performance,support similar FameLayout featrue（Gravity、Margin).
 - Support Auto-Refresh、Auto-LoadMore、Scroll to bottom to Auto-LoadMore（Recommended to use the Adapter while use ListView/GridView/RecyclerView）.
 - Support Cross boundary rebound(OverScroll).
 - Support Drawer-Style(Refresh view below the Content view and Refresh view has been pinned).
 - Support Refresh view has two styles:STYLE_DEFAULT(Do not change the height by default)、STYLE_SCALE(Dynamically change the height)
 - Support Two-Level refresh（TwoLevelSmoothRefreshLayout）,PS:TaoBao-Senond-Floor、JD-Activity.
 - Support ListView,GridView,RecyclerView on LoadMore to smooth scrolling.
 - Support Margin,PS:No margin effect in scrolling？SmoothRefreshLayout no such problem.
 - Support Multi-State:STATE_CONTENT(Default state)、STATE_ERROR(Error state),STATE_EMPTY(Empty state),STATE_CUSTOM(Custom state).
 - Many callback interface and debugging information.

## Demo
下载 [Demo.apk](https://raw.githubusercontent.com/dkzwm/SmoothRefreshLayout/master/apk/demo.apk)    

## Snapshots
- Test QQ-WebBrowser style    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_web_browser_style.gif)

- Test QQ-Activity style    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_qq_style.gif)

- Test Two-Level refresh    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/test_two_level_refresh.gif)

- Width FrameLayout    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_frameLayout.gif)

- With TextView     
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_textView.gif)

- With ListView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_listView.gif)

- With GridView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_gridView.gif)

- With RecyclerView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_recyclerView.gif)

- With ViewPager    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_viewPager.gif)

- With WebView    
![](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/snapshot/with_webView.gif)

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
    compile 'com.github.dkzwm:SmoothRefreshLayout:1.4.6'
}
```
#### Use Xml to config
#####  Since v1.4.1
```
<?xml version="1.0" encoding="utf-8"?>
<me.dkzwm.widget.srl.SmoothRefreshLayout
	xmlns:android="http://schemas.android.com/apk/res/android"
	xmlns:app="http://schemas.android.com/apk/res-auto"
	android:id="@+id/smoothRefreshLayout"
	android:layout_width="match_parent"
	android:layout_height="match_parent">
	<TextView
		android:layout_width="match_parent"
		android:layout_height="match_parent"/>
</me.dkzwm.widget.srl.SmoothRefreshLayout>
```
##### Before v1.4.1
```
<?xml version="1.0" encoding="utf-8"?>
<me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout
	xmlns:android="http://schemas.android.com/apk/res/android"
	xmlns:app="http://schemas.android.com/apk/res-auto"
	android:id="@+id/smoothRefreshLayout"
	android:layout_width="match_parent"
	android:layout_height="match_parent">
	<TextView
		android:layout_width="match_parent"
		android:layout_height="match_parent"/>
</me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout>
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
public interface IRefreshView <T extends IIndicator> {    

    byte TYPE_HEADER = 0;
    byte TYPE_FOOTER = 1;

    byte STYLE_DEFAULT = 0;
    byte STYLE_SCALE = 1;

    /**
     * Get the view's type.
	 * @return type {@link #TYPE_HEADER}, {@link #TYPE_FOOTER}.
     */
    int getType();

    /**
     * Get the target view.
     *
     * @return The returned view must be the view that will be added to the Layout
     */
    View getView();

    /**
     * Get the view's style. If return {@link #STYLE_SCALE} SmoothRefreshLayout will dynamically
     * change the height, so the performance will be reduced.
     *
     * @return style {@link #STYLE_DEFAULT}, {@link #STYLE_SCALE}.
     */
    int getStyle();

    /**
     * Get the custom height, If style is {@link #STYLE_SCALE} should return a custom height.
     *
     * @return Custom height
     */
    int getCustomHeight();

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
    void onRefreshComplete(SmoothRefreshLayout layout,boolean isSuccessful);

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
- Use Java code to config
```    
    setHeaderView(@NonNull IRefreshView header);
    setFooterView(@NonNull IRefreshView footer);
```    

- Please write directly to the Xml file,SmoothRefreshLayout will find the view that impl the IRefreshView interface to added inside.
 
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
# SmoothRefreshLayout
## [English](https://github.com/dkzwm/SmoothRefreshLayout/blob/master/README.md) | 中文
一个高效的Android刷新库，理论上支持所有的视图，比官方的SwipeRefreshLayout更强大且使用方便.    
本开源库的部分代码来自[android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh).    
非常感谢他提供的这么棒的开源项目！    

## 特性
 1.支持5种模式: refresh(刷新),loadMore(加载更多),overScroll(越界回弹),both(刷新和加载更多),none;    
 2.支持嵌套滑动;    
 3.支持所有的视图;   
 
## 快照
* 包含FrameLayout    
   <div class='row'>
       <img src='snapshot/with_frameLayout.png' width="300px"/>
   </div>
* 包含TextView     
	<div class='row'>
    	<img src='snapshot/with_textView.png' width="300px"/>
	</div>
* 包含ListView    
	<div class='row'>
    	<img src='snapshot/with_listView.png' width="300px"/>
	</div>
* 包含GridView    
	<div class='row'>
		<img src='snapshot/with_gridView.png' width="300px"/>
	</div>
* 包含RecyclerView    
	<div class='row'> 
   		<img src='snapshot/with_recyclerView.png' width="300px"/>
	</div>
* 包含ViewPager    
	<div class='row'> 
	   	<img src='snapshot/with_viewPager.png' width="300px"/>
	</div>
* 包含WebView    
	<div class='row'> 
    	<img src='snapshot/with_webView.png' width="300px"/>
	</div>
* CoordinatorLayout里面嵌套RecyclerView
	<div class='row'> 
    	<img src='snapshot/with_recyclerView_in_coordinatorLayout.png' width="300px"/>
	</div>
* 越界回弹模式    
	<div class='row'> 
    	<img src='snapshot/test_overScroll.png' width="300px"/>
	</div>
## 使用    
编辑中


	MIT License

	Copyright (c) 2017 dkzwm

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
package me.dkzwm.smoothrefreshlayout.utils;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by dkzwm on 2017/5/25.
 * Smooth scroll to load more
 *
 * @author dkzwm
 */
public class LoadMoreScrollCompat {
    public static void scrollCompact(View view, float deltaY) {
        if (view != null) {
            if (view instanceof AbsListView) {
                AbsListView listView = (AbsListView) view;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    listView.scrollListBy(Math.round(deltaY));
                } else {
                    try {
                        Method method = AbsListView.class.getDeclaredMethod("trackMotionScroll",
                                int.class, int.class);
                        if (method != null) {
                            method.setAccessible(true);
                            method.invoke(listView, -Math.round(deltaY), -Math.round(deltaY));
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            } else if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                recyclerView.scrollBy(0, Math.round(deltaY));
            }
        }
    }
}

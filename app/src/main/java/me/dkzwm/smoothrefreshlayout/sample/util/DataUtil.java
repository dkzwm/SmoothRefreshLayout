package me.dkzwm.smoothrefreshlayout.sample.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */

public class DataUtil {
    public  static List<String> createList(int count,int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(String.valueOf(count + i));
        }
        return list;
    }
}

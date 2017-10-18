package me.dkzwm.widget.srl.sample.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */

public class DataUtil {
    private static List<String> sUrls = new ArrayList<>();

    static {
        sUrls.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2770691011,100164542&fm=27&gp=0.jpg");
        sUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508325599929&di=66fa1178688bfc77b44cb4edbf2db7f2&imgtype=0&src=http%3A%2F%2Fimg.dongqiudi.com%2Fuploads%2Favatar%2F2015%2F07%2F25%2FQM387nh7As_thumb_1437790672318.jpg");
        sUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508325599927&di=389e03784785e34bb333b65e67ab2de0&imgtype=0&src=http%3A%2F%2Fimg.mp.itc.cn%2Fupload%2F20160706%2F95b9d87f089c4c3e959475584cb12148.jpg");
        sUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508325663850&di=a5bcb74d9dd5257b1cf40f31677cf647&imgtype=jpg&src=http%3A%2F%2Fimg0.imgtn.bdimg.com%2Fit%2Fu%3D4079384686%2C3997373627%26fm%3D214%26gp%3D0.jpg");
        sUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508325701432&di=233f6d3da9d53158690dabadab402606&imgtype=0&src=http%3A%2F%2Fimg4.duitang.com%2Fuploads%2Fitem%2F201407%2F22%2F20140722183209_KEQms.jpeg");
        sUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508326471704&di=e6a8459b8cc041cf8e66f7ecf30b3d60&imgtype=0&src=http%3A%2F%2Fimg3.a0bi.com%2Fupload%2Fttq%2F20150418%2F1429356614113.jpg");
        sUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508325701429&di=26499a340f41a6d03686151944f0e26f&imgtype=0&src=http%3A%2F%2Fd.hiphotos.baidu.com%2Fzhidao%2Fwh%253D600%252C800%2Fsign%3D007336919245d688a357baa294f25126%2F91ef76c6a7efce1b5e983768af51f3deb58f65e5.jpg");
        sUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508325745159&di=15cc4586d679884655418e2bf39967b9&imgtype=jpg&src=http%3A%2F%2Fimg4.imgtn.bdimg.com%2Fit%2Fu%3D3993012754%2C3747813528%26fm%3D214%26gp%3D0.jpg");
        sUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508325769929&di=b2f4dcf016acbe10df9a3568aaddfd87&imgtype=0&src=http%3A%2F%2Felf-work-4.qiniudn.com%2F1399116203yHJDXI3sUT_300.jpeg");
    }

    public static List<String> createList(int count, int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(sUrls.get((count + i) % sUrls.size()));
        }
        return list;
    }
}

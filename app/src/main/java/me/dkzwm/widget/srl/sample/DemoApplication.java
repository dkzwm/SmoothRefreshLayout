package me.dkzwm.widget.srl.sample;

import android.app.Application;
import me.dkzwm.widget.srl.IRefreshViewCreator;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * Created by dkzwm on 2017/6/28.
 *
 * @author dkzwm
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SmoothRefreshLayout.setDefaultCreator(
                new IRefreshViewCreator() {
                    @Override
                    public IRefreshView<IIndicator> createHeader(SmoothRefreshLayout layout) {
                        ClassicHeader<IIndicator> header = new ClassicHeader<>(layout.getContext());
                        header.setLastUpdateTimeKey("header_last_update_time");
                        return header;
                    }

                    @Override
                    public IRefreshView<IIndicator> createFooter(SmoothRefreshLayout layout) {
                        ClassicFooter<IIndicator> footer = new ClassicFooter<>(layout.getContext());
                        footer.setLastUpdateTimeKey("footer_last_update_time");
                        return footer;
                    }
                });
    }
}

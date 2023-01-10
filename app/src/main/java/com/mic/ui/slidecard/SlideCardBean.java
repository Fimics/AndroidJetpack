package com.mic.ui.slidecard;

import java.util.ArrayList;
import java.util.List;

public class SlideCardBean {
    private int postition;
    private String url;
    private String name;

    public SlideCardBean(int postition, String url, String name) {
        this.postition = postition;
        this.url = url;
        this.name = name;
    }

    public int getPostition() {
        return postition;
    }

    public SlideCardBean setPostition(int postition) {
        this.postition = postition;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public SlideCardBean setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getName() {
        return name;
    }

    public SlideCardBean setName(String name) {
        this.name = name;
        return this;
    }

    public static List<SlideCardBean> initDatas() {
        List<SlideCardBean> datas = new ArrayList<>();
        int i = 1;
        datas.add(new SlideCardBean(i++, "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202106%2F09%2F20210609081952_51ef5.thumb.1000_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1675956993&t=e3e40ac3172310e935d5e85156c76714", "美女1"));
        datas.add(new SlideCardBean(i++, "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202106%2F09%2F20210609081952_51ef5.thumb.1000_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1675956993&t=e3e40ac3172310e935d5e85156c76714", "美女2"));
        datas.add(new SlideCardBean(i++, "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202106%2F09%2F20210609081952_51ef5.thumb.1000_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1675956993&t=e3e40ac3172310e935d5e85156c76714", "美女3"));
        datas.add(new SlideCardBean(i++, "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202106%2F09%2F20210609081952_51ef5.thumb.1000_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1675956993&t=e3e40ac3172310e935d5e85156c76714", "美女4"));
        datas.add(new SlideCardBean(i++, "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202106%2F09%2F20210609081952_51ef5.thumb.1000_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1675956993&t=e3e40ac3172310e935d5e85156c76714", "美女5"));
        datas.add(new SlideCardBean(i++, "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202106%2F09%2F20210609081952_51ef5.thumb.1000_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1675956993&t=e3e40ac3172310e935d5e85156c76714", "美女6"));
        datas.add(new SlideCardBean(i++, "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202106%2F09%2F20210609081952_51ef5.thumb.1000_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1675956993&t=e3e40ac3172310e935d5e85156c76714", "美女7"));
        datas.add(new SlideCardBean(i++, "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202106%2F09%2F20210609081952_51ef5.thumb.1000_0.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1675956993&t=e3e40ac3172310e935d5e85156c76714","美女8"));
        return datas;
    }
}

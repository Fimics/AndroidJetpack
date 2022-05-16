package com.lawaken.image;

public class Builder {
    //设置图片是否展示占位图
    private int placeholderId;
    //设置图片是否展示错误图
    private int errorImageId;
    //设置图片是否展示网络错误图片
    private int fallbackImageId;

    private boolean isMemoryCache = false;
    //设置是否开启磁盘缓存
    private boolean isDiskCache = true;

    //使用个框架
    private Strategy strategy = Strategy.GLIDE;

    public int getPlaceholderId() {
        return placeholderId;
    }

    public int getErrorImageId() {
        return errorImageId;
    }

    public int getFallbackImageId() {
        return fallbackImageId;
    }

    public boolean isMemoryCache() {
        return isMemoryCache;
    }

    public boolean isDiskCache() {
        return isDiskCache;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public Builder placeholderId(int placeholderId) {
        this.placeholderId = placeholderId;
        return this;
    }

    public Builder errorImageId(int errorImageId) {
        this.errorImageId = errorImageId;
        return this;
    }

    public Builder fallbackImageId(int fallbackImageId) {
        this.fallbackImageId = fallbackImageId;
        return this;
    }

    public Builder memoryCache(boolean memoryCache) {
        isMemoryCache = memoryCache;
        return this;
    }

    public Builder diskCache(boolean diskCache) {
        isDiskCache = diskCache;
        return this;
    }

    public Builder strategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }
}

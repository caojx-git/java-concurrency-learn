package com.caojx.javaconcurrencylearn.example.other;

import java.util.List;

/**
 * 商机管理线索实体
 *
 * @author suqh
 * @date 2019/6/27 14:21
 */
public class BusinessDiscoverClues {
    String id;
    String companyName;
    String appName;
    String crawledDate;
    String publisher;
    List<DownloadCount> appstore_downloadCount;
    List<DownloadCount> android_downloadCount_total;
    List<DownloadCount> android_downloadCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCrawledDate() {
        return crawledDate;
    }

    public void setCrawledDate(String crawledDate) {
        this.crawledDate = crawledDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public List<DownloadCount> getAppstore_downloadCount() {
        return appstore_downloadCount;
    }

    public void setAppstore_downloadCount(List<DownloadCount> appstore_downloadCount) {
        this.appstore_downloadCount = appstore_downloadCount;
    }

    public List<DownloadCount> getAndroid_downloadCount_total() {
        return android_downloadCount_total;
    }

    public void setAndroid_downloadCount_total(List<DownloadCount> android_downloadCount_total) {
        this.android_downloadCount_total = android_downloadCount_total;
    }

    public List<DownloadCount> getAndroid_downloadCount() {
        return android_downloadCount;
    }

    public void setAndroid_downloadCount(List<DownloadCount> android_downloadCount) {
        this.android_downloadCount = android_downloadCount;
    }
}

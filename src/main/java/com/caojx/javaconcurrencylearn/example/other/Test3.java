package com.caojx.javaconcurrencylearn.example.other;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Test3 {

    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");

    private static Lock lock = new ReentrantLock();

    private static String jsonString = FileUtils.ReadFile("/Users/caojx/code/java-concurrency-learn/src/main/java/com/caojx/javaconcurrencylearn/example/other/data.txt");

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, BusinessDiscoverClues.class);
        List<BusinessDiscoverClues> data = mapper.readValue(jsonString, javaType);

        //得到前一天
        String yestDay = getYestDay();

        List<BusinessDiscoverClues> businessList = new ArrayList<>();
        String yestDownloads = "500<x && x<=1000";
        long startTime = System.currentTimeMillis();
        log.info("start---");
        if (StringUtils.isNotBlank(yestDownloads) && !"null".equals(yestDownloads)) {
            data.stream().parallel().forEach(data1 -> {
                int androidCount = 0;
                int appStoreCount = 0;

                //android昨日下载量
                List<DownloadCount> androidDownloadList = data1.getAndroid_downloadCount();
                if (!CollectionUtils.isEmpty(androidDownloadList)) {
                    for (int j = 0; j < androidDownloadList.size(); j++) {
                        if (yestDay.equals(androidDownloadList.get(j).getDate())) {
                            androidCount = Integer.parseInt(androidDownloadList.get(j).getDownloadCount());
                        }
                    }
                }
                //appstore昨日下载量
                List<DownloadCount> appstoreDownloadList = data1.getAppstore_downloadCount();
                if (!CollectionUtils.isEmpty(appstoreDownloadList)) {
                    for (int j = 0; j < appstoreDownloadList.size(); j++) {
                        if (yestDay.equals(appstoreDownloadList.get(j).getDate())) {
                            String count = appstoreDownloadList.get(j).getDownloadCount();
                            String countString = StringUtils.isNotBlank(count) ? count : "0";
                            appStoreCount = Integer.parseInt(countString);
                        }
                    }
                }

                //对可能出现并发问题的代码加锁
                lock.lock();
                try {
                    long sum = androidCount + appStoreCount;
                    engine.put("x", sum);
                    Object result = engine.eval(yestDownloads);
                    if ((boolean) result) {
                        businessList.add(data1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock(); //解锁
                }

            });
            data = businessList;
        }
        log.info("endTime---{}", System.currentTimeMillis() - startTime);
        System.out.println(data.size());
    }

    public static String getYestDay() {
        //得到前一天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String yestDay = df.format(date);
        return yestDay;
    }
}

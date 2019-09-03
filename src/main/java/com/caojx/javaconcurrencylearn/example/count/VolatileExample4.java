package com.caojx.javaconcurrencylearn.example.count;

/**
 * volatile特别适合作为状态标记
 * https://blog.csdn.net/ttt_12345/article/details/83588418
 *
 *
 * @author caojx
 * @version $Id: VolatileExample4.java,v 1.0 2019-08-29 20:45 caojx
 * @date 2019-08-29 20:45
 */
public class VolatileExample4 implements Runnable {

    private volatile boolean isRunFlag = true;

    public void setRunFlag(boolean runFlag){
        this.isRunFlag = runFlag;
    }

    @Override
    public void run() {
        while (isRunFlag == true){
            System.out.println("运行中。。。。");
            try{
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileExample4 volatileExample4 = new VolatileExample4();
        new Thread(volatileExample4).start();
        Thread.sleep(1000);
        System.out.println("下面终止运行");
        volatileExample4.setRunFlag(false);
    }
}

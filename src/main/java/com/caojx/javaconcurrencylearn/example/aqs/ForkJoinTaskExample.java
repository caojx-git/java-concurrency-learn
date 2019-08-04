package com.caojx.javaconcurrencylearn.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * ForkJoinTask示例
 * <p>
 * 1+2+3+4...+100=5050
 * <p>
 * 这里通常需要继承一个类，叫做RecursiveTask，这里我们给的是一个整形值，计算的任务需要返回一个整形。
 * RecursiveTask字面上其实就是递归的意思，我们他大任务不断的拆分成小任务，其实就是一个递归拆分任务的一个过程，
 * 同时我们需要复写RecursiveTask里边的compute()方法，我们具体来看下这个例子，我们任务是要做一个加和运算（1加到100），
 * 我们假设每次做相加这个操作会很耗时，当我们提交了任务之后，就会调用compute方法，compute他会真的去做fork和jion这个操作，
 * 我们问具体来看下这个操作，首先，他判断了一下加操作（boolean canCompute = (end - start) <= threshold;）
 * 两端的值是否已经距离很近了，比如最开始的时候是100-1，默认是如果<=2的时候才开始执行，因此，最开始时都很难能命中if (canCompute) 判断。
 * 他相当于是，比如正常1~3的时候，会执行if (canCompute) 里边的操作，1~100的时候，他的任务可能特别重这个时候他就不做了，
 * 这个我们之前说了有前提是我们家的每次相加是一个很繁重的任务需要拆分，当然了1~100口算都算出来的，这就不符合我刚才说的前提了，
 * 我们是以简单的运算来模拟复杂的场景，我们这里就假设相加操作就是一个特别耗时的操作。每一次相加操作都尽量让他拆分到小任务里做，
 * 这里只有当他小道一定范围之后，才允许直接做if (canCompute) 里边的加操作。如果他们的距离比较远的时候呢，我们从当前的两端取出中间值，
 * 然后分别定义两条线去递归子任务，任务拆分到什么情况呢，取决我们这里的if (canCompute) 条件什么时候成立。
 * 这样就相当于把一个大任务拆分成了多个小任务，通过递归的方式，拆分之后调用leftTask.fork() 、rightTask.fork()方法，
 * 然后子任务开始执行。子任务执行完成后通过leftTask.join()、rightTask.join() 把两端的子任务都合并起来，
 * 最后将我们两条递归线程的结果继续合并（sum = leftResult + rightResult）得到最终的结果。这段代码其实并不长，
 * 他很好的演示了我们刚才说的ForkJoin框架处理的流程，首先是才拆分子任务，然后子任务各自执行，子任务执行完成之后我们通过jion的方式，
 * 他子任务的结果合并起来，这里fork()和、jion()底层具体是怎么工作的，我们知道他其实是根据工作窃取的算法来做就可以了，不用过分的追究。
 */
@Slf4j
public class ForkJoinTaskExample extends RecursiveTask<Integer> {


    public static final int threshold = 2;
    private int start;
    private int end;

    public ForkJoinTaskExample(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;

        //如果任务足够小就计算任务
        boolean canCompute = (end - start) <= threshold;
        if (canCompute) {
            for (int i = start; i <= end; i++) {
                sum += i;
            }
        } else {
            // 如果任务大于阈值，就分裂成两个子任务计算
            int middle = (start + end) / 2;
            ForkJoinTaskExample leftTask = new ForkJoinTaskExample(start, middle);
            ForkJoinTaskExample rightTask = new ForkJoinTaskExample(middle + 1, end);

            // 执行子任务
            leftTask.fork();
            rightTask.fork();

            // 等待任务执行结束合并其结果
            int leftResult = leftTask.join();
            int rightResult = rightTask.join();

            // 合并子任务
            sum = leftResult + rightResult;
        }
        return sum;
    }

    public static void main(String[] args) {
        ForkJoinPool forkjoinPool = new ForkJoinPool();

        //生成一个计算任务，计算1+2+3+4...+100
        ForkJoinTaskExample task = new ForkJoinTaskExample(1, 100);

        //执行一个任务，当我们提交了任务之后，就会调用compute方法
        Future<Integer> result = forkjoinPool.submit(task);

        try {
            log.info("result:{}", result.get());
        } catch (Exception e) {
            log.error("exception", e);
        }
    }
}

package demo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.utils.util.Dates;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * https://mp.weixin.qq.com/s?__biz=MzI3NzE0NjcwMg==&mid=2650122732&idx=1&sn=2e49fcf3b88623f7415162be2d099e1e&chksm=f36bb4cdc41c3ddbcc3389489bb465f33f8a52f6c39a3e920b7d983738ea2523d1462c18e458&mpshare=1&scene=1&srcid=1205QXNwrERT4cWRBIUUdLBH#rd
 *
 * @author 谢长春 2018/12/5 .
 */
public class DateTest {

    /**
     * 定义一个全局的SimpleDateFormat
     */
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 使用ThreadFactoryBuilder定义一个线程池
     */
    private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();

    private static ExecutorService pool = new ThreadPoolExecutor(5, 200,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    /**
     * 定义一个CountDownLatch，保证所有子线程执行完之后主线程再执行
     */
    private static CountDownLatch countDownLatch = new CountDownLatch(100);

    @SneakyThrows
    public static void main(String[] args) {
        if(false){
            //定义一个线程安全的HashSet
            Set<String> dates = Collections.synchronizedSet(new HashSet<String>());
            for (int i = 0; i < 100; i++) {
                //获取当前时间
                Calendar calendar = Calendar.getInstance();
                int finalI = i;
                pool.execute(() -> {
                    //时间增加
                    calendar.add(Calendar.DATE, finalI);
                    //通过simpleDateFormat把时间转换成字符串
                    String dateString = simpleDateFormat.format(calendar.getTime());
                    System.out.println(dateString);
                    //把字符串放入Set中
                    dates.add(dateString);
                    //countDown
                    countDownLatch.countDown();
                });
            }
            //阻塞，直到countDown数量为0
            countDownLatch.await();
            //输出去重后的时间个数，理论上应该是恒定的100个时间，实际上数量小于100
            System.out.println(">"+dates.size());
        }
        if (true) {
            countDownLatch = new CountDownLatch(100);
            //定义一个线程安全的HashSet
            Set<String> dates = Collections.synchronizedSet(new HashSet<String>());
            Dates date = Dates.now();
            for (int i = 0; i < 100; i++) {
                pool.execute(() -> {
                    String dateString = date.addDay(1).formatDateTime();
                    //把字符串放入Set中
                    dates.add(dateString);
                    System.out.println(dateString);
                    //countDown
                    countDownLatch.countDown();
                });
            }
            //阻塞，直到countDown数量为0
            countDownLatch.await();
            //输出去重后的时间个数
            System.out.println(">"+dates.size());
        }
    }
}

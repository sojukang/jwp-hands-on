package concurrency.stage0;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스레드 풀은 무엇이고 어떻게 동작할까?
 * 테스트를 통과시키고 왜 해당 결과가 나왔는지 생각해보자.
 *
 * Thread Pools
 * https://docs.oracle.com/javase/tutorial/essential/concurrency/pools.html
 *
 * Introduction to Thread Pools in Java
 * https://www.baeldung.com/thread-pool-java-and-guava
 */
class ThreadPoolsTest {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolsTest.class);

    @Test
    void testNewFixedThreadPool() {
        // executor를 통해 Thead pool을 지정하는 경우 Connector의 설정을 무시하고 executor의 설정을 따른다.
        // 최대 Thread pool의 개수는 2개이므로 expected thread pool size = 2
        final var executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        // submit은 Runnuble을 구현한 task를 전달받아 execute한다. execute은 thread pool에서 thread를 받아
        // 이를 실행한다.
        executor.submit(logWithSleep("hello fixed thread pools"));
        executor.submit(logWithSleep("hello fixed thread pools"));
        // thread pool 개수인 2 개보다 많은 task가 execute을 시도할 경우 blocking 되어 큐에 할당된다.
        executor.submit(logWithSleep("hello fixed thread pools"));

        // 올바른 값으로 바꿔서 테스트를 통과시키자.
        final int expectedPoolSize = 2;
        final int expectedQueueSize = 1;

        assertThat(expectedPoolSize).isEqualTo(executor.getPoolSize());
        assertThat(expectedQueueSize).isEqualTo(executor.getQueue().size());
    }

    @Test
    void testNewCachedThreadPool() {
        // 새로운 Threads를 만들며 Thread pool을 초기화한다. 이미 만들어진 Threads가 있을 경우 이를 활용하여
        // Thread pool을 만든다.
        // 60초 동안 사용되지 않은 thread는 캐시에서 제거된다.
        final var executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for (int i = 0; i < 1000; i++) {
            executor.submit(logWithSleep("hello cached thread pools"));
        }
        executor.submit(logWithSleep("hello cached thread pools"));
        executor.submit(logWithSleep("hello cached thread pools"));

        // 올바른 값으로 바꿔서 테스트를 통과시키자.
        // Thread pool의 최대 크기는 Integer.MAX_VALUE 이고
        final int expectedPoolSize = 1002;
        // Thread Pool의 Max Size 이하 개의 Thread를 생성했기 때문에 queue를 이용하지 않는다.
        final int expectedQueueSize = 0;

        assertThat(expectedPoolSize).isEqualTo(executor.getPoolSize());
        assertThat(expectedQueueSize).isEqualTo(executor.getQueue().size());
    }

    private Runnable logWithSleep(final String message) {
        return () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info(message);
        };
    }
}

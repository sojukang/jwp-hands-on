package concurrency.stage0;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * 다중 스레드 환경에서 두 개 이상의 스레드가 변경 가능한(mutable) 공유 데이터를 동시에 업데이트하면 경쟁 조건(race condition)이 발생한다.
 * 자바는 공유 데이터에 대한 스레드 접근을 동기화(synchronization)하여 경쟁 조건을 방지한다.
 * 동기화된 블록은 하나의 스레드만 접근하여 실행할 수 있다.
 *
 * Synchronization
 * https://docs.oracle.com/javase/tutorial/essential/concurrency/sync.html
 */
class SynchronizationTest {

    /**
     * 테스트가 성공하도록 SynchronizedMethods 클래스에 동기화를 적용해보자.
     * synchronized 키워드에 대하여 찾아보고 적용하면 된다.
     *
     * Guide to the Synchronized Keyword in Java
     * https://www.baeldung.com/java-synchronized
     */
    @Test
    void testSynchronized() throws InterruptedException {
        var executorService = Executors.newFixedThreadPool(3);
        var synchronizedMethods = new SynchronizedMethods();

        IntStream.range(0, 1000)
            .forEach(count -> executorService.submit(synchronizedMethods::calculate));
        executorService.awaitTermination(500, TimeUnit.MILLISECONDS);

        assertThat(synchronizedMethods.getSum()).isEqualTo(1000);
    }

    private static final class SynchronizedMethods {

        private int sum = 0;

        /**
         * blocking이 안될 경우 시점 t1에서 thread1 이 읽은 값이 0, thread2가 읽은 값이 0이라면
         * calculate에 의해 두 thread 모두 sum에 1을 set한다. 이런 식으로 getSum()을 해오는 시점에 읽은 데이터가
         * 동기화되지 않은 경우 무시되는 합이 존재할 수 있다.
         *
         * calulate에 synchronized를 걸 경우
         * getSum() -> sum++ -> setSum() 과정동안 blocking이 되므로 다른 Thread에서 읽은 getSum()은 항상 setSum()까지
         * 반영된 상태이기 때문에 Thread Inference 문제가 발생하지 않는다.
         *
         * getSum()과 setSum()에 각각 synchronized를 걸 경우
         * 역시 하나의 완전한 작업이 완료될 때 까지 blocking 되는 게 아니므로 문제가 발생한다.
         * ThreadA getSum(): 1
         * ThreadB getSum(): 1
         * ThreadA setSum(): 2
         * ThreadB setSum(): 2
         *
         * 위와 같은 상황이 벌어질 수 있다.
         */
        public synchronized void calculate() {
            setSum(getSum() + 1);
        }

        /**
         * getSum에 synchronized를 걸 경우 실패한다.
         * ThreadA가 0을 읽고 나서 calculate이 완료되기 전 ThreadB가 0을 읽을 수 있기 때문이다.
         * @return int
         */
        public int getSum() {
            return sum;
        }

        /**
         * setSum에 synchronized걸어도 실패한다.
         * ThreadA가 1을 기록하고 ThreadB가 1을 기록할 수 있기 때문이다.
         * @param sum
         */
        public void setSum(int sum) {
            this.sum = sum;
        }
    }
}

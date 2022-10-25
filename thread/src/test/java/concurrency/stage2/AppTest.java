package concurrency.stage2;

import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    private static final AtomicInteger count = new AtomicInteger(0);

    /**
     * 1. App 클래스의 애플리케이션을 실행시켜 서버를 띄운다.
     * 2. 아래 테스트를 실행시킨다.
     * 3. AppTest가 아닌 App의 콘솔에서 SampleController가 생성한 http call count 로그를 확인한다.
     * 4. application.yml에서 설정값을 변경해보면서 어떤 차이점이 있는지 분석해본다.
     * - 로그가 찍힌 시간
     * - 스레드명(nio-8080-exec-x)으로 생성된 스레드 갯수를 파악
     * - http call count
     * - 테스트 결과값
     *
     * accept-count
     * 운영체제가 제공하는 최대 대기 큐.
     * 최대 커넥션에 도달한 이후 요청은 대기 큐에 할당된다.
     * 대기 큐가 가득 찬 이후 요청은 거부되든지 time-out된다.
     * 기본 값은 100이다.
     *
     * max-connections
     * 서버가 동시에 요청을 받아 처리할 수 있는
     * 최대 커넥션 개수이다.
     * maxConnections를 넘어서면 blocked 되어 대기 큐에 할당된다.
     * NIO/NIO2의 경우 기본 값은 10000, APR/native인 경우 기본 값은 8192dlek.
     *
     * 현재 연결된 실제 Connection 수가 아니라 현재 사용중인 socket fd(file descriptor)의 수이다.
     *
     * threads.max
     * 커넥터에 의해 생성되어 요청을
     * 처리하는 스레드의 최대 개수이다.
     * 동시에 처리할 수 있는 최대 요청을 의미한다.
     * 기본 값은 200이다. Excecutor와 Connector를 관련하여 실행하는 경우
     * Connector는 이 값과 내부 thread pool을 무시하고 Executor의 설정을 따른다.
     *
     * Connector
     * 동시 처리 가능한 요청은 thread.
     * 요청은 maxConnections에 의해 서버 소켓 내부의 큐에 할당되어 스레드에 의해 처리된다(Context Switching).
     * 그 이상은 Blocked 되어 운영체제가 관리하는 큐에 할당된다(acceptCount).
     */
    @Test
    void test() throws Exception {
        final var NUMBER_OF_THREAD = 10;
        var threads = new Thread[NUMBER_OF_THREAD];

        for (int i = 0; i < NUMBER_OF_THREAD; i++) {
            threads[i] = new Thread(() -> incrementIfOk(TestHttpUtils.send("/test")));
        }

        for (final var thread : threads) {
            thread.start();
            Thread.sleep(50);
        }

        for (final var thread : threads) {
            thread.join();
        }

        assertThat(count.intValue()).isEqualTo(2);
    }

    private static void incrementIfOk(final HttpResponse<String> response) {
        if (response.statusCode() == 200) {
            count.incrementAndGet();
        }
    }
}

package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 전파(Transaction Propagation)란?
 * 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 *
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다.
 * SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
 *
 * https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-propagation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage2Test {

    private static final Logger log = LoggerFactory.getLogger(Stage2Test.class);

    @Autowired
    private FirstUserService firstUserService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     */
    @Test
    void testRequired() {
        // 부모 트랜잭션이 있을 경우 참여하고, 없을 경우 새 트랜잭션을 생성한다. 하나의 물리적 트랜잭션으로 적용된다.
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     */
    @Test
    void testRequiredNew() {
        // 부모 트랜잭션이 있어도 자식 트랜잭션은 독립된 물리적, 논리적 트랜잭션을 생성한다.
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                    "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     */
    @Test
    void testRequiredNewWithRollback() {
        // required_new 에서 저장한 유저는 독립적으로 commit 되어 rollback 되지 않는다. 부모 트랜잭션에서
        // 발생한 exception 에 의해 부모 트랜잭션에서 저장한 유저는 rollback 된다.
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(0);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * supports 부모 트랜잭션이 있으면 참여하고 없으면 참여하지 않는다.
     */
    @Test
    void testSupports() {
        // 부모 메서드가 non-transactional 이고 여기서 supports 설정 된 method 를 호출할 경우
        // current transaction 을 형성하는데, 이 transaction 은 transaction synchronization 상태라서
        // 자원에 기반하지 않는다. 반면에 Required 나 Required_new 인 경우 자원에 기반하는 실제 transaction 을 수행할 수 있다.
        // 부모 트랜잭션이 존재할 경우 참여한다 -> 하나의 논리적 트랜잭션에 포함된다.
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     */
    @Test
    void testMandatory() {
        // 부모 트랜잭션이 없는 경우 Transaction Synchronization 자체를 형성하지 않는다. 따라서 actual Transaction 자체도 없다.
        // 부모 트랜잭션이 존재하는 경우(Actual Transaction 인 경우 한정) 부모의 논리적 트랜잭션에 포함
        // 부모 트랜잭션이 Transaction Synchronization 상태인 경우(Actual Transaction 이 아닌 경우) transaction 을 형성하지 않는다.
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    @Test
    void testNotSupported() {
        // NotSupported 로 지정된 경계 내부만 Transaction Synchronization 을 형성한다. actual Transaction 이 아니므로
        // 자원에 기반하지 않는다(물리적 트랜잭션 아님).
        // 부모 트랜잭션이 없는 경우 actual transaction: 물리적 트랜잭션의 개수는 0개.
        // 부모 트랜잭션이 물리적 트랜잭션인 경우(actual transaction 인 경우) NotSupported 내의 로직은 논리적 트랜잭션을 형성하나
        // 물리적 트랜잭션은 형성하지 않는다. 즉, Transaction Synchronization 을 지원하나 actual transaction 은 아니다.

        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                    "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * Nested 인 경우 부모 트랜잭션이 actual(물리적 트랜잭션)인 경우 별도의 save point를 지정하여 별도의 rollback 지점을 형성한다.
     * 부모가 actual transaction 이 아닌 경우 required_new 처럼 새로운 actual transaction을 형성한다(JPA에서는 동작 X).
     */
    @Test
    void testNested() {
        // 부모 트랜잭션이 없는 경우 Nested 경계 내에서 새로운 물리 트랜잭션(actual) 트랜잭션을 형성한다.
        // 부모 트랜잭션이 존재하는 경우 JPA 사양에서는 Nested 를 지원하지 않는다.
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * Never인 경우
     */
    @Test
    void testNever() {
        // 부모 트랜잭션이 actual(물리적)으로 존재할 경우 예외를 반환한다. IllegalTransactionStateException
        // 부모 트랜잭션이 존재하지 않을 경우 Never 경계 내부는 Transaction Synchronization 을 지원하나
        // 물리적 트랜잭션(actual transaction)을 형성하지는 않는다.
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}

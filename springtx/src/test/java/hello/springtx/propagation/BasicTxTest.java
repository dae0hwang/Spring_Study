package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.UnexpectedRollbackException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.transaction.TransactionDefinition;
import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {

        @Bean
        public PlatformTransactionManager transactionManager(DataSource
            dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }
    @Test
    void double_commit() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(tx1);
        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋");
        txManager.commit(tx2);
    }

    @Test
    void inner_commit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        //외부 트랜잭션 커밋이나 롤백이 끝나지 않았는데, 새로운 트랜잭션 시작
        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new
            DefaultTransactionAttribute());
        //새로운 트랜잭션이 아닌, 기존에 트랜잭션을 이어서 사용한다는 것을 확인함
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
//        log.info("내부 트랜잭션 커밋");
//        txManager.commit(inner);
        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);
    }

    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        //내부 트랜잭션은 커밋되었는데, 외부는 롤백
        //->결과적으로 물리 트랜잭션이 롤백된다.
        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outer);
    }

    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner);
        log.info("외부 트랜잭션 커밋");
        //외부 트랜잭션이 커밋될 때, 예외가 발생하는 것을 유의 깊게 보자.
        assertThatThrownBy(() -> txManager.commit(outer))
            .isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new
            DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());
        log.info("내부 트랜잭션 시작");

        //내부 트랜잭션을 시작할 때 전파 옵션인 propagationBehavior 에 PROPAGATION_REQUIRES_NEW
        //옵션을 주었다.
        //기존 트랜잭션에 참여하는 것이 아니라 새로운 물리 트랜잭션을 만들어서 시작하게 된다.
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = txManager.getTransaction(definition);
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner); //롤백
        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer); //커밋
    }


}
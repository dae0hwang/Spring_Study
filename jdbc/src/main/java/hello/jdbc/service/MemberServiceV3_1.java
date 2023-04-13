package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    //setAutoCommit(false)가 아닌 PlatformTransactionManager 사용
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws
        SQLException {
        //트랜잭션 시작
        //지금은 JDBC 기술을 사용하기 때문에 DataSourceTransactionManager 구현체를 주입 받아야 한다.
        //JPA 같은 기술로 변경되면 JpaTransactionManager 를 주입 받으면 된다.
        TransactionStatus status = transactionManager.getTransaction(new
            DefaultTransactionDefinition());
        try {
            //비즈니스 로직
            bizLogic(fromId, toId, money);

            //성공시 커밋
            transactionManager.commit(status);
        } catch (Exception e) {
            //실패시 롤백
            transactionManager.rollback(status);
            throw new IllegalStateException(e);
        }
    }

    private void bizLogic(String fromId, String toId, int money) throws
        SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
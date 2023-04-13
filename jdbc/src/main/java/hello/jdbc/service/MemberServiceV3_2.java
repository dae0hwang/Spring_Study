package hello.jdbc.service;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.SQLException;
/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {
    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;
    public MemberServiceV3_2(PlatformTransactionManager transactionManager,
        MemberRepositoryV3 memberRepository) {
        //TransactionTemplate을  transaction을 주입받아서 생성
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }
    public void accountTransfer(String fromId, String toId, int money) throws
        SQLException {
        //트랜잭션 템플릿의 기본 동작은 다음과 같다.
        //비즈니스 로직이 정상 수행되면 커밋한다.
        //언체크 예외가 발생하면 롤백한다. 그 외의 경우 커밋한다. (체크 예외의 경우에는 커밋하는데, 이
        //부분은 뒤에서 설명한다.)
        txTemplate.executeWithoutResult((status) -> {
            try {
                //비즈니스 로직
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                //해당 람다에서 체크 예외를 밖으로 던질 수 없기 때문에 언체크
                // 예외로 바꾸어 던지도록 예외를 전환했다.
                throw new IllegalStateException(e);
            }
        });
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
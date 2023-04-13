package hello.jdbc.service;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;
    public void accountTransfer(String fromId, String toId, int money) throws
        SQLException {
        //동일한 connection을 사용하기 위해서 서비스 코드 내부에서 connection 획득
        //repository에 해당 connection 동일하게 전달
        Connection con = dataSource.getConnection();
        try {
            //setAutoCommit(false)로 트랜잭션 시작
            con.setAutoCommit(false);

            //계좌 이체 비지니스 로직
            Member fromMember = memberRepository.findById(con, fromId);
            Member toMember = memberRepository.findById(con, toId);
            memberRepository.update(con, fromId, fromMember.getMoney() - money);
            //에러 발생 상황 연출
            if (toMember.getMemberId().equals("ex")) {
                throw new IllegalStateException("이체중 예외 발생");
            }
            memberRepository.update(con, toId, toMember.getMoney() + money);

            //성공시 커밋
            con.commit();
        } catch (Exception e) {
            //에러발생 - 실패시 롤백
            con.rollback();
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }
    }
    private void release(Connection con) {
        if (con != null) {
            try {
                //커넥션 풀 고려
                con.setAutoCommit(true);
                con.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }

    //    private void bizLogic(Connection con, String fromId, String toId, int
    //        money) throws SQLException {
    //        Member fromMember = memberRepository.findById(con, fromId);
    //        Member toMember = memberRepository.findById(con, toId);
    //        memberRepository.update(con, fromId, fromMember.getMoney() - money);
    //        validation(toMember);
    //        memberRepository.update(con, toId, toMember.getMoney() + money);
    //    }
    //    private void validation(Member toMember) {
    //        if (toMember.getMemberId().equals("ex")) {
    //            throw new IllegalStateException("이체중 예외 발생");
    //        }
    //    }


}
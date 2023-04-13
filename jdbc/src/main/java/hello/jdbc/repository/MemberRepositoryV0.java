package hello.jdbc.repository;

import static hello.jdbc.connection.DBConnectionUtil.*;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import java.sql.*;
/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?,?)";
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            //con은 h2드라이버에서 흭득한 connection이다.
            //h2 전용 커넥션으로 PreparedStatement 객체를 만들어 준다.
            //그렇게 되면 h2 드라이버가 구현한 PreparedStatement사용할 수 있는 것이다.
            pstmt = con.prepareStatement(sql);

            //sql를 설정한 pstmt에 파라미터를 지정하고, excuteUpdate()를 통해서, DB에 값 추가 요청을 한다.
            //DB 통신이 성공한다면, 에러 발생 없이, 해당 메소드가 종료된다.
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        }catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            //pstmt executeUpdate가 아니라, excuteQuery를 통해서, 조회 결과를 ResultSet으로 반환받는다.
            rs = pstmt.executeQuery();
            //결과가 담겨있는 resultSet에서 rs.get 메소드를 통해서 정보를 가져온다.
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" +
                    memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            //update 방법도 insert와 유사하다. sql에 원하는 값을 파라미터 바인딩 후에
            //excuteUpdate를 통해서 DB에 sql를 보낸다.
            //resultSize에는 영향 받은 컬럼의 수를 반환한다.
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }
    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            //sql 쿼리만 다르고 방식은 insert update와 같다.
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }
    private void close(Connection con, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }


}

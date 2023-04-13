package hello.jdbc.connection;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import com.zaxxer.hikari.HikariDataSource;

import static hello.jdbc.connection.ConnectionConst.*;
@Slf4j
public class ConnectionTest {

    @Test
    void DataSource_DriverManager() throws SQLException{
        //기존 DriverManager를 기존의 방식
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection1={}, class1={}", con1, con1.getClass());

        //DataSource 구현체 DriverManagerDataSource를 사용해서 커넥션 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL,
            USERNAME, PASSWORD);
        Connection con2 = dataSource.getConnection();
        log.info("connection2={}, class2={}", con2, con2.getClass());
    }
    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource - 항상 새로운 커넥션 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL,
            USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }


    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        //HikariCP - DataSource 인터페이스 구현, 커넥션 획득을 위한 HikariDataSource 생성
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10); //풀 내 커넥션 개수 정하기
        dataSource.setPoolName("MyPool"); //이름 지정

        //커넥션 획득하려고 하면, 커넥션을 얻기 위해 커넥션 풀을 채운다. 그리고 커넥션을 획득한다.
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
        //커넥션 풀에 10개의 커넥션이 찰 수 있도록 시간 벌어주기.
        Thread.sleep(2000);
    }
}

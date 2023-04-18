package jpabook.jpashop.domain;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;
    @Embedded
    private Address address;
    //일대일 양방향 읽기 전용 필드 등록
    //연관관계 주인 지정하기.
    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;
    //ENUM 타입을 DB 테이블에 스트링 값으로 저장하기
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

}

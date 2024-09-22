package io.hhplus.tdd.point;

import java.util.List;

public interface PointHistoryRepository {

    /**
     * database 패키지의 Table 클래스도 마찬가지로 하나의 시스템이며, 이 프로젝트가 List 라는 인 메모리 자료구조에 의존하고 있습니다.
     * 하지만 이후에 데이터 영속성을 제어하기 위해 어떤 시스템을 사용할지 모르기에 Repository 는 도메인 자체를 참조하도록 설계합니다.
     * Table 클래스에 도메인을 저장하는 서비스 로직이 궁금하신 분들은 PointTableService 를 참조하십시오.
     * @see io.hhplus.tdd.point.PointTableService
     */
    PointHistory save(PointHistory pointHistory);
    List<PointHistory> selectAllByUserId(long userId);
}

package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;

import java.util.List;

public interface PointService {

    /**
     * 포인트 조회
     * @param id 사용자 ID
     * @return 사용자 포인트
     */
    UserPoint point(long id);

    /**
     * 포인트 내역 조회
     * @param id 사용자 ID
     * @return 포인트 내역
     */
    List<PointHistory> history(long id);

    /**
     * 포인트 충전
     * @param id 사용자 ID
     * @param amount 충전 포인트
     * @return 충전 이후 최종 사용자 포인트
     */
    UserPoint charge(long id, long amount);

    /**
     * 포인트 사용
     * @param id 사용자 ID
     * @param amount 사용 포인트
     * @return 사용 이후 최종 사용자 포인트
     */
    UserPoint use(long id, long amount);
}

package io.hhplus.tdd.integration;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.exception.OutOfPointException;
import io.hhplus.tdd.infrastructure.UniqueUserIdHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PointControllerIntegrationTest extends TddApplicationControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    /*
     * 테스트 작성 이유 : 포인트를 충전할 수 있어야 합니다.
     */
    @DisplayName("포인트를 충전한다.")
    @Test
    void userCanChargePoint() throws Exception {
        // given
        long userId = UniqueUserIdHolder.next();
        long chargeAmount = 100;

        given(pointService.charge(userId, chargeAmount))
                .willReturn(new UserPoint(userId, chargeAmount, System.currentTimeMillis()));

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/point/{id}/charge", userId)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(chargeAmount))
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(chargeAmount));
    }

    /*
     * 테스트 작성 이유 : 포인트를 사용할 수 있어야 합니다.
     */
    @DisplayName("포인트를 사용한다.")
    @Test
    void userCanUsePoint() throws Exception {
        // given
        long userId = UniqueUserIdHolder.next();
        long useAmount = 100;
        int resultAmount = 0;

        given(pointService.use(userId, useAmount))
                .willReturn(new UserPoint(userId, resultAmount, System.currentTimeMillis()));

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/point/{id}/use", userId)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(useAmount))
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(resultAmount));
    }

    /*
     * 테스트 작성 이유 : 포인트를 조회할 수 있어야 합니다.
     */
    @DisplayName("포인트를 조회한다.")
    @Test
    void whenUserGetPoint_ThenSeeCurrentPoint() throws Exception {
        // given
        long userId = UniqueUserIdHolder.next();
        long currentPoint = 100;

        given(pointService.point(userId))
                .willReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/point/{id}", userId)
                        .contentType(APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(currentPoint));
    }

    /*
     * 테스트 작성 이유 : 포인트 내역을 조회할 수 있어야 합니다.
     */
    @DisplayName("포인트 내역을 조회한다.")
    @Test
    void whenUserGetPoint_ThenSeeAllHistories() throws Exception {
        // given
        long userId = UniqueUserIdHolder.next();
        List<PointHistory> histories = List.of(
                new PointHistory(1, userId, 100, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, userId, 50, TransactionType.USE, System.currentTimeMillis())
        );

        given(pointService.history(userId))
                .willReturn(histories);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/point/{id}/histories", userId)
                        .contentType(APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].amount").value(100))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].userId").value(userId))
                .andExpect(jsonPath("$[1].amount").value(50))
                .andExpect(jsonPath("$[1].type").value("USE"));
    }

    /*
     * 테스트 작성 이유 : 사용할 포인트 금액은 잔고보다 작아야 합니다. 이때, 응답 메시지로 code 와 message 가 반환되어야 합니다.
     */
    @DisplayName("잔고가 부족할 경우, 포인트 사용은 실패하여야 한다.")
    @Test
    void whenUserUsePoint_ThenFailIfOutOfPoint() throws Exception {
        // given
        long userId = UniqueUserIdHolder.next();
        long currentPoint = 50;
        long useAmount = 100;

        given(pointService.use(userId, useAmount))
                .willThrow(new OutOfPointException(userId, currentPoint));

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/point/{id}/use", userId)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(useAmount))
        );

        // then
        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("에러가 발생했습니다."));
    }
}

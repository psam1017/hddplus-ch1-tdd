package io.hhplus.tdd.integration;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PointControllerIntegrationTest extends TddApplicationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserPointRepository userPointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    /*
     * 테스트 작성 이유 : 포인트를 충전할 수 있어야 합니다.
     */
    @DisplayName("포인트를 충전한다.")
    @Test
    void charge() throws Exception {
        // given
        UserPoint userPoint = new UserPoint(1, 100, System.currentTimeMillis());
        userPointRepository.save(userPoint);
        long currentPoint = userPoint.point();
        long chargeAmount = 100;

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/point/{id}/charge", userPoint.id())
                        .contentType(APPLICATION_JSON)
                        .content(createJson(chargeAmount))
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userPoint.id()))
                .andExpect(jsonPath("$.point").value(currentPoint + chargeAmount));
    }

    /*
     * 테스트 작성 이유 : 포인트를 사용할 수 있어야 합니다.
     */
    @DisplayName("포인트를 사용한다.")
    @Test
    void use() throws Exception {
        // given
        UserPoint userPoint = new UserPoint(1, 100, System.currentTimeMillis());
        userPointRepository.save(userPoint);
        long currentPoint = userPoint.point();
        long useAmount = 100;

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/point/{id}/use", userPoint.id())
                        .contentType(APPLICATION_JSON)
                        .content(createJson(useAmount))
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userPoint.id()))
                .andExpect(jsonPath("$.point").value(currentPoint - useAmount));
    }

    /*
     * 테스트 작성 이유 : 포인트를 조회할 수 있어야 합니다.
     */
    @DisplayName("포인트를 조회한다.")
    @Test
    void point() throws Exception {
        // given
        UserPoint userPoint = new UserPoint(1, 100, System.currentTimeMillis());
        userPointRepository.save(userPoint);

        // when
        ResultActions resultActions = mockMvc.perform(get("/point/{id}", userPoint.id()));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userPoint.id()))
                .andExpect(jsonPath("$.point").value(userPoint.point()));
    }

    /*
     * 테스트 작성 이유 : 포인트 내역을 조회할 수 있어야 합니다.
     */
    @DisplayName("포인트 내역을 조회한다.")
    @Test
    void history() throws Exception {
        // given
        UserPoint userPoint = new UserPoint(1, 100, System.currentTimeMillis());
        userPoint = userPointRepository.save(userPoint);
        pointHistoryRepository.save(new PointHistory(0, userPoint.id(), 100, TransactionType.CHARGE, 0));
        pointHistoryRepository.save(new PointHistory(0, userPoint.id(), 50, TransactionType.USE, 0));

        // when
        ResultActions resultActions = mockMvc.perform(get("/point/{id}/histories", userPoint.id()));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(userPoint.id()))
                .andExpect(jsonPath("$[0].amount").value(100))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].userId").value(userPoint.id()))
                .andExpect(jsonPath("$[1].amount").value(50))
                .andExpect(jsonPath("$[1].type").value("USE"));
    }

    /*
     * 테스트 작성 이유 : 사용할 포인트 금액은 잔고보다 작아야 합니다.
     */
    @DisplayName("잔고가 부족할 경우, 포인트 사용은 실패하여야 한다.")
    @Test
    void whenUserUsePoint_ThenFailIfOutOfPoint() throws Exception {
        // given
        UserPoint userPoint = UserPoint.empty(1);
        userPoint = userPointRepository.save(userPoint);
        long useAmount = 100;

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/point/{id}/use", userPoint.id())
                        .contentType(APPLICATION_JSON)
                        .content(createJson(useAmount))
        );

        // then
        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("에러가 발생했습니다."));
    }
}

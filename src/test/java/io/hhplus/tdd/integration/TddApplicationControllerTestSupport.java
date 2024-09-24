package io.hhplus.tdd.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = {
                PointController.class
        }
)
public abstract class TddApplicationControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected PointService pointService;

    @Autowired
    private ObjectMapper om;

    public String createJson(Object object) {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

package io.hhplus.tdd.point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.controller.PointController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = {
                PointController.class
        }
)
public abstract class TddApplicationControllerSupport {

    @Autowired
    protected MockMvc mockMvc;

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

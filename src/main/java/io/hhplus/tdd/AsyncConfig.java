package io.hhplus.tdd;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "pointEventExecutor")
    public Executor singleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}

package com.fraction.payments.appConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author : Chandra sekhar Polavarapu
 * @Description: This allows us to customize the thread configuration. Even if we don't do this, we can still use Async feature
 * But, it's better to do it so that we can customize depending on our server/POD  capacity.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "Async task executor")
    public Executor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("workerThread- ");
        executor.initialize();
        return executor;
    }
}

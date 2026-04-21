package com.example.getoffer.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = false)
public class DemoDataRunner implements ApplicationRunner {

    private final DemoDataSeeder demoDataSeeder;

    public DemoDataRunner(DemoDataSeeder demoDataSeeder) {
        this.demoDataSeeder = demoDataSeeder;
    }

    @Override
    public void run(ApplicationArguments args) {
        demoDataSeeder.seedIfEmpty();
    }
}

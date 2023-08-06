package app.learning;

import app.learning.services.DistributedLockManager;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@QuarkusMain
@ApplicationScoped
public class AppRunner implements QuarkusApplication {

    @Inject
    DistributedLockManager distributedLockManager;

    public static void main(String[] args) {
        Quarkus.run(AppRunner.class, args);
    }
    @Override
    public int run(String... args) throws Exception {
        System.out.println("App started..");
        System.out.println("distributedLockManager.init() started..");
        distributedLockManager.init();
        System.out.println("distributedLockManager.init() completed..");
        Quarkus.waitForExit();
        return 0;
    }
}

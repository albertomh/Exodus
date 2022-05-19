import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.albertomh.exodus"})
public class YourApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(YourApplication.class, args);

        // Emit a `ContextStartedEvent`, which is picked up by the Exodus migration runner.
        applicationContext.start();
    }

}
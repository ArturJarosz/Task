package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
/*        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("liquibase.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            //System.out.println(properties.getProperty("test"));
            System.out.println("printing");
            properties.forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;*/
    }
}

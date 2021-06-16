package at.researchstudio.sat.mmsdesktop;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MerkmalserviceDesktopApplication {
    public static void main(String[] args) {
        Application.launch(UIApplication.class, args);
    }
}

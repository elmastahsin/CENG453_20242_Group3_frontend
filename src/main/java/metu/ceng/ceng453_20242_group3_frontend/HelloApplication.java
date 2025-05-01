package metu.ceng.ceng453_20242_group3_frontend;

        import javafx.application.Application;
        import javafx.application.Platform;
        import javafx.fxml.FXMLLoader;
        import javafx.scene.Scene;
        import javafx.stage.Stage;

        import java.io.IOException;

        public class HelloApplication extends Application {
            @Override
            public void start(Stage stage) throws IOException {
                // Set this system property to help with some macOS rendering issues
                System.setProperty("prism.order", "sw");

                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 320, 240);
                stage.setTitle("Hello!");
                stage.setScene(scene);

                // Add a graceful shutdown hook
                stage.setOnCloseRequest(e -> {
                    Platform.exit();
                    System.exit(0);
                });

                stage.show();
            }

            public static void main(String[] args) {
                // Set these properties before launching the app
                System.setProperty("glass.disableGrab", "true");
                System.setProperty("javafx.macosx.embedded", "true");
                launch(args);
            }
        }

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import share.controller.leecher.LeecherController;
import share.controller.seeder.SeederController;

public class Main extends Application {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		
		TabPane tabs= new TabPane();
		tabs.getTabs().add(new Tab("Seeder", new SeederController()));
		tabs.getTabs().add(new Tab("Leecher",new LeecherController()));
		tabs.getTabs().forEach(t->t.setClosable(false));
		
		
		primaryStage.setTitle("UDP P2P file sharing");
		primaryStage.setScene(new Scene(tabs,1000,640));
		primaryStage.show();
	}

}

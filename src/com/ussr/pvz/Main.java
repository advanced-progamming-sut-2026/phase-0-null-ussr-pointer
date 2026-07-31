import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.ussr.pvz.model.App;
import com.ussr.pvz.view.AppView;

import java.util.Scanner;

void main() {
    App.registerShutdownHook();
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("pvz-2 ussr");
    config.setWindowedMode(1280, 720);
    config.useVsync(true);
    config.setForegroundFPS(60);
    new Lwjgl3Application(new AppView(), config);
    AppView app = new AppView();
    Scanner sc = new Scanner(System.in);
    app.run(sc);
}

import com.ussr.pvz.model.App;
import com.ussr.pvz.view.AppView;

import java.util.Scanner;

void main() {
    App.registerShutdownHook();
    AppView app = new AppView();
    Scanner sc = new Scanner(System.in);
    app.run(sc);
}

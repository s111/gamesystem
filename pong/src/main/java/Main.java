import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

public class Main {
    public static void main(String[] args) {
        try {
            AppGameContainer app = new AppGameContainer(new Pong("Pong"));
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}

package hr.from.bkoruznjak.accello.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by bkoruznjak on 01/05/2017.
 */

public class GameHints {

    private static final String[] hintArray = {
            "#1 TIP - Bend your phone to move around, limber up bro!",
            "#2 TIP - Outgrowing the screen will not break it!",
            "#3 TIP - Collect teal orbs to keep in shape",
            "#4 TIP - Crimson orbs are bad for you, fast food lvl bad!",
            "#5 TIP - Collect green orbs to be like Bolt himself",
            "#6 TIP - Purple orbs make you walk funny...",
            "#7 TIP - There is no spoon",
            "#8 TIP - Chuck Norris held the highscore until he took an arrow to the knee",
            "#9 TIP - APM stands for actions per minute",
            "#10 TIP - Drugs are bad",
            "#11 TIP - I never asked for this",
            "#12 TIP - Stay awhile and listen..."
    };

    public static String getRandomGameHint() {
        int hintIndex = ThreadLocalRandom.current().nextInt(0, hintArray.length);
        return hintArray[hintIndex];
    }
}

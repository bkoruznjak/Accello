package hr.from.bkoruznjak.accello.util;


/**
 * Holds all the meaningfull geometry functions one would need
 * <p>
 * Created by bkoruznjak on 09/04/2017.
 */

public class GeometryUtil {

    /**
     * Method checks if two circles are intersection in 2D Descartes space
     *
     * @param firstX
     * @param firstY
     * @param firstRadius
     * @param secondX
     * @param secondY
     * @param secondRadius
     * @return true if circles intersect
     */
    public static boolean areCirclesIntersecting(int firstX, int firstY, float firstRadius, int secondX, int secondY, float secondRadius) {
        double distance = distanceBetweenTwoPoints(firstX, firstY, secondX, secondY);
        return distance < (firstRadius + secondRadius);
    }

    /**
     * Method checks if two circles are overlapping in 2D Descartes space
     *
     * @param firstX
     * @param firstY
     * @param firstRadius
     * @param secondX
     * @param secondY
     * @param secondRadius
     * @return true if circles intersect
     */
    public static boolean areCirclesOverlapping(int firstX, int firstY, float firstRadius, int secondX, int secondY, float secondRadius) {
        double distance = distanceBetweenTwoPoints(firstX, firstY, secondX, secondY);
        return distance < (firstRadius - secondRadius);
    }


    /**
     * Method gets the double distance between two points
     *
     * @param firstX
     * @param firstY
     * @param secondX
     * @param secondY
     * @return distance between points
     */
    public static double distanceBetweenTwoPoints(int firstX, int firstY, int secondX, int secondY) {
        return Math.sqrt((secondX - firstX) * (secondX - firstX) + (secondY - firstY) * (secondY - firstY));
    }
}

package coffee.khyonieheart.eden.flock.gui.snake;

import java.util.Random;


public enum SnakeBearing
{
    NORTH(-1),
    SOUTH(1),
    EAST(1),
    WEST(-1)
    ;

    private static final Random random = new Random();
    private int modifier;

    private SnakeBearing(int modifier)
    {
        this.modifier = modifier;
    }

    public boolean isHorizontal()
    {
        return this.equals(SnakeBearing.EAST) || this.equals(SnakeBearing.WEST);
    }

    public int getModifier()
    {
        return this.modifier;
    }

    public static SnakeBearing getRandomValidBearing(SnakeBearing current)
    {
        SnakeBearing[] possibleBearings = new SnakeBearing[3];
        possibleBearings[0] = current;

        if (current.isHorizontal())
        {
            possibleBearings[1] = SnakeBearing.NORTH;
            possibleBearings[2] = SnakeBearing.SOUTH;
            return possibleBearings[random.nextInt(3)];
        }

        possibleBearings[1] = SnakeBearing.EAST;
        possibleBearings[2] = SnakeBearing.WEST;

        return possibleBearings[random.nextInt(3)];
    }
}

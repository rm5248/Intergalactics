package igx.server;

import igx.shared.*;

import java.util.*;
import java.io.*;
import java.awt.*;

public class MapReader {

    public static final char HOME_PLANET = '*';
    public static final char EMPTY_SPACE = '.';

    private static Random generator = new Random();

    // Generates a pseudo random number synchronized across all clients in the game
    public static int pseudo(int min, int max) {
        int value = generator.nextInt();
        return value < 0 ? (-value) % (max - min + 1) + min : value % (max - min + 1) + min;
    }

    public static String[] getMaps(int numPlayers) {
        return new File("map" + File.separator + "" + numPlayers).list();
    }

    public static Point[] readMap(int numPlayers, String fileName) throws IOException {
        // Randomize the home planet positions
        int[] homes = new int[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            homes[i] = i;
        }
        for (int i = 0; i < numPlayers; i++) {
            int swap = pseudo(0, numPlayers - 1);
            int value = homes[i];
            homes[i] = homes[swap];
            homes[swap] = value;
        }
        BufferedReader br = new BufferedReader(new FileReader("map" + File.separator + ""
                + numPlayers + File.separator
                + fileName));
        String line = br.readLine();
        while ((line != null) && (line.length() > 0) && (line.charAt(0) == '#')) {
            line = br.readLine();
        }
        int homeNum = 0;
        int neutralNum = numPlayers;
        Point[] points = new Point[Params.PLANETS];
        for (int i = 0; i < Params.PLANETS; i++) {
            points[i] = new Point(-1, -1);
        }
        for (int i = 0; i < Params.MAPY; i++) {
            if ((line == null) || (line.length() < Params.MAPX)) {
                throw new IOException("Insufficient map data.");
            }
            int planetNum = 0;
            for (int j = 0; j < Params.MAPX; j++) {
                char c = line.charAt(j);
                if (c == EMPTY_SPACE) {
                    continue;
                } else if (c == HOME_PLANET) {
                    if (homeNum == numPlayers) {
                        throw new IOException("Too many home planets.");
                    }
                    planetNum = homes[homeNum];
                    homeNum++;
                } else {
                    planetNum = neutralNum;
                    neutralNum++;
                }
                if (planetNum == Params.PLANETS) {
                    throw new IOException("Too many neutral planets.");
                }
                points[planetNum] = new Point(j, i);
            }
            line = br.readLine();
        }
        return points;
    }

    public static void main(String[] args) {
        try {
            System.out.println("Maps: ");
            String[] maps = getMaps(8);
            for (int i = 0; i < maps.length; i++) {
                System.out.println("  " + maps[i]);
            }
            String mapName = "Claustrophobia";
            Point[] map = readMap(8, mapName);
            System.out.println("Map: " + mapName);
            for (int i = 0; i < map.length; i++) {
                System.out.println("  " + map[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

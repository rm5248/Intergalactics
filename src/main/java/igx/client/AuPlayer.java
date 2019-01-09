package igx.client;

import sun.audio.*;    //import the sun.audio package

import java.io.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Plays audio from our resources.
 */
public class AuPlayer {

    private static final Logger logger = LogManager.getLogger();

    private static Map<String, byte[]> audioFiles = new HashMap<>();

    public static void play(String filename) {
        if (!audioFiles.containsKey(filename)) {
            try {
                byte[] audioFile = IOUtils.resourceToByteArray("/" + filename);
                audioFiles.put(filename, audioFile);
            } catch (IOException ex) {
                logger.error(ex);
            }
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(audioFiles.get(filename));

        try {
            // Create an AudioStream object from the input stream.
            AudioStream as = new AudioStream(bis);

            // Use the static class member "player" from class AudioPlayer to play
            // clip.
            AudioPlayer.player.start(as);
        } catch (IOException ex) {
            logger.error(ex);
        }

    }
}

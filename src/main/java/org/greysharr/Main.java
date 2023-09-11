package org.greysharr;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Main {
    static String urlBase = "https://podcast.app/guillaume-radio-20-best-of-p139731/"; // ?limit=50&offset=1600
    static int offset = 3500;
    static int offsetMax = 4000;
    static List<String> liens = new ArrayList<>();
    static File destination = new File("F:\\podcasts");

    public static void main(String[] args) {
        System.out.println("Lancement de la récupération");
        System.out.println("URL base = " + urlBase);

        fetchLinks();
        saveLinks();
        downloadFiles();
    }

    public static void fetchLinks() {
        System.out.println("Début de la récupération des URLs...");

        while (offset < offsetMax) {
            System.out.println("Offset = " + offset);
            System.out.println("OffsetMax = " + offsetMax);
            String urlPage = urlBase + "?offset=" + offset;

            Document doc;
            try {
                doc = Jsoup.connect(urlPage).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Elements playElements = doc.select(".play-btn");

            for (Element play : playElements) {
                String mp3Link = play.attr("data-mp3");

                if (mp3Link.contains("complet")) {
                    liens.add(mp3Link);
                    System.out.println("MP3 = " + mp3Link);
                }
            }

            offset += 50;
        }
    }

    public static void saveLinks(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        File destinationListe = new File(destination + "/liste_fichiers_" + timeStamp + ".txt");

        try {
            FileUtils.writeStringToFile(destinationListe, liens.toString(), "ISO-8859-1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void downloadFiles() {
        System.out.println("Début de la récupération des MP3...");

        int compteur = 0;
        ZonedDateTime dateTimeStart = ZonedDateTime.now();

        for (String mp3 : liens) {
            URL url;

            System.out.println("Progression : " + compteur + "/" + liens.size());
            compteur++;

            try {
                url = new URL(mp3);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            String filename = mp3.substring(mp3.lastIndexOf("/"));
            String yearmonth = mp3.substring(mp3.indexOf("/201"), mp3.lastIndexOf("/"));
            File destinationFinale = new File(destination + yearmonth + filename);

            try {
                System.out.println("Téléchargement de : " + filename);
                FileUtils.copyURLToFile(url, destinationFinale);
                System.out.println("OK");

                ZonedDateTime fileDone = ZonedDateTime.now();
                Duration duration = Duration.between(dateTimeStart, fileDone);
                System.out.println("Temps passé : " + (int)duration.toSeconds()/3600 + "h" + (int)((duration.toSeconds()%3600)/60) + "m" + duration.toSeconds()%60 + "s");

                long tempsRestant = (duration.toSeconds() / compteur) * (liens.size() - compteur);
                System.out.println("Temps restant : " + (int)tempsRestant/3600 + "h" + (int)((tempsRestant%3600)/60) + "m" + tempsRestant%60 + "s");
                System.out.println("------------------------------------");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
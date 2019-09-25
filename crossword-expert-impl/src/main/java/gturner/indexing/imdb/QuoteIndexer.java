package gturner.indexing.imdb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import gturner.util.TrackingInputStream;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/24/13
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class QuoteIndexer {
    static final String MAIN_URL = "http://localhost:8983/solr";
    protected static final String RESOURCE_NAME = "imdb";
    static final String UPDATE_URL = RESOURCE_NAME + "/update/json";
    static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
    static final Client client = Client.create();

    static final int COMMIT_LIMIT = 5000;
    static final int PERMITS = 500;

    static final AtomicLong docId = new AtomicLong(0);
    static final AtomicLong count = new AtomicLong(0);

    public static void main(String[] args) {
        final ExecutorService service = Executors.newFixedThreadPool(2);

        GZIPInputStream compressedFile = null;

        final Pattern entityPattern = Pattern.compile("^([^:]+):.*");
        final Pattern titlePattern = Pattern.compile("^#(.*)\\(\\d{4}\\)");

        try {
            File imdb = new File("H:\\dev\\data_dumps\\imdb\\quotes.list.gz");
            final long fileTotalSize = imdb.length();
            FileInputStream fis = new FileInputStream(imdb);
            int bufferSize = 1024*16;
            final TrackingInputStream trackingInputStream = new TrackingInputStream(new BufferedInputStream(fis, bufferSize));
            compressedFile = new GZIPInputStream(trackingInputStream);

            Timer logTask = new Timer(true);
            logTask.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.format("%f%%, DocId:%d   Submitted:%d   %s%n", 100.0f * trackingInputStream.getBytesRead()/ fileTotalSize, docId.get(), count.get(), format.format(new Date()));
                }
            }, 2500, 2500);

            final Semaphore permits = new Semaphore(PERMITS);

            Reader decoder = new InputStreamReader(compressedFile);
            BufferedReader buffered = new BufferedReader(decoder);

            String readLine = "";
            while(readLine != null) {
                if(readLine.startsWith("=")) {
                    break;
                }
                readLine = buffered.readLine();
            }

            Set<String> entities = new LinkedHashSet<String>();
            String title = null;
            StringBuilder sb = new StringBuilder();

            while(readLine != null) {
                if(readLine.startsWith("#")) {
                    if(title != null) {
                        permits.acquire();

                        submitRecord(service, entities, title, sb, permits);
                    }
                    title = readLine.substring(2);
                    sb = new StringBuilder();
                    entities = new LinkedHashSet<String>();

                    Matcher matcher = titlePattern.matcher(title);
                    if(matcher.find()) {
                        entities.add(matcher.group(1));
                    }
                } else {
                    Matcher matcher = entityPattern.matcher(readLine);
                    if(matcher.find()) {
                        entities.add(matcher.group(1));
                    }

                    sb.append(readLine).append("\n");
                }
                readLine = buffered.readLine();
            }

            submitRecord(service, entities, title, sb, permits);

            service.shutdown();
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            QuoteIndexer.client.resource(QuoteIndexer.MAIN_URL).path(QuoteIndexer.UPDATE_URL).queryParam("commit", "true").get(String.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(compressedFile != null) {
                try {
                    compressedFile.close();
                } catch (IOException ignore) {
                }
            }
        }


    }

    private static void submitRecord(ExecutorService service, Set<String> entities, String title, StringBuilder sb, final Semaphore permits) {
        final HashMap<String, Object> jsonObj = new HashMap<String, Object>();
        jsonObj.put("id", RESOURCE_NAME + docId.incrementAndGet());
        jsonObj.put("title", title);
        jsonObj.put("text", sb.toString());
        if(!entities.isEmpty()) {
            jsonObj.put("keywords", new ArrayList<String>(entities));
        }
        jsonObj.put("resourcename", RESOURCE_NAME);

        service.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    WebResource resource = QuoteIndexer.client.resource(QuoteIndexer.MAIN_URL).path(QuoteIndexer.UPDATE_URL);
                    long docId = QuoteIndexer.count.incrementAndGet();
                    if(docId % QuoteIndexer.COMMIT_LIMIT == 0) {
                        resource = resource.queryParam("commit", "true");
                    }
                    resource.type(MediaType.APPLICATION_JSON_TYPE).
                             post(String.class, new ObjectMapper().writeValueAsString(Arrays.asList(jsonObj)));
                } finally {
                    permits.release();
                }
                return null;
            }
        });
    }
}

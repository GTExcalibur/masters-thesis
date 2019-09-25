package gturner.indexing.wikipedia;

import com.sun.jersey.api.client.*;
import gturner.expert.util.ConstraintPattern;
import gturner.util.TrackingInputStream;
import info.bliki.htmlcleaner.ContentToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.filter.WPList;
import info.bliki.wiki.filter.WPTable;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.model.WikiModel;
import info.bliki.wiki.tags.HTMLTag;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/16/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class WikipediaIndexer {
    static final String MAIN_URL = "http://localhost:8983/solr";
    protected static final String RESOURCE_NAME = "wikibooks";
    static final String UPDATE_URL = RESOURCE_NAME + "/update/json";
    static final Client client = Client.create();
    static final AtomicLong docId = new AtomicLong(0);
    static final AtomicLong count = new AtomicLong(0);
    static final boolean checkExists = false;
    static final boolean normalize = false;
    static final long minId = 0;
    static final long stopCheck = 0;
    static final int COMMIT_LIMIT = 5000;
    static final int PERMITS = 500;
    static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
//    static final ExecutorService postThread = Executors.newSingleThreadExecutor();
//    static final Semaphore postPermits = new Semaphore(PERMITS);

    static final Set<String> possibleAnswers = new HashSet<String>();


    public static void main(String[] args) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("F:\\dev\\thesis\\fill_in_the_blanks-distinct-answers.txt"));
        String line;
        while((line = bufferedReader.readLine()) != null) {
            possibleAnswers.add(line);
        }

        final ExecutorService service = Executors.newFixedThreadPool(8);

        CBZip2InputStream compressedFile = null;

        try {
            File wiki = new File("F:\\dev\\data_dumps\\enwiki-latest-pages-articles.xml.bz2");
//            File wiki = new File("H:\\dev\\data_dumps\\enwiktionary-20130225-pages-meta-current.xml.bz2");
//            File wiki = new File("H:\\dev\\data_dumps\\enwikiquote-20130218-pages-meta-current.xml.bz2");
//            File wiki = new File("H:\\dev\\data_dumps\\enwikinews-20130315-pages-meta-current.xml.bz2");
//            File wiki = new File("H:\\dev\\data_dumps\\enwikibooks-20130319-pages-meta-current.xml.bz2");
            final long fileTotalSize = wiki.length();
            FileInputStream fis = new FileInputStream(wiki);
            int bufferSize = 1024*16;
            final TrackingInputStream trackingInputStream = new TrackingInputStream(new BufferedInputStream(fis, bufferSize));
            // this looks weird, but the bzip compression always begins with 'bz'
            trackingInputStream.skip(2);
            compressedFile = new CBZip2InputStream(trackingInputStream);

            Timer logTask = new Timer(true);
            logTask.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.format("%f%%, DocId:%d   Submitted:%d   %s%n", 100.0f * trackingInputStream.getBytesRead()/ fileTotalSize, docId.get(), count.get(), format.format(new Date()));
                }
            }, 2500, 2500);

            final Semaphore permits = new Semaphore(PERMITS);
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(new WikipediaContentHandler() {
                @Override
                protected void handleDocument(String id, String title, StringBuilder content) {
                    try { permits.acquire(); } catch (InterruptedException ignore) {}
//                    service.submit(new WikiParserCallable(id, title, content){
                    service.submit(new WikiVisitorCallable(id, title, content){
                        @Override
                        public Void call() throws Exception {
                            Void call;
                            try {
                                call = super.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw e;
                            } finally {
                                permits.release();
                            }
                            return call;
                        }
                    });
                }
            });
            parser.parse(new InputSource(new BufferedReader(new InputStreamReader(compressedFile), bufferSize)));
            service.shutdown();
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            System.out.println(Arrays.toString(possibleAnswers.toArray()));

//            WikipediaIndexer.client.resource(WikipediaIndexer.MAIN_URL).path(WikipediaIndexer.UPDATE_URL).queryParam("commit", "true").get(String.class);
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

    protected static String normalizeString(String text) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("[^\\p{ASCII}]", "");
        return text;
    }
}

class WikipediaContentHandler extends org.xml.sax.helpers.DefaultHandler {
    private static enum TAG_STATE { title, text, ns }

    long ns = Long.MIN_VALUE;
    boolean isPage;
    boolean isRedirect;
    boolean skipCharacters = false;
    TAG_STATE state;
    String title;
    StringBuilder text = new StringBuilder();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(isPage) {
            if("title".equals(localName)) {
                state = TAG_STATE.title;
            } else if("text".equals(localName)) {
                state = TAG_STATE.text;
            } else if("ns".equals(localName)) {
                state = TAG_STATE.ns;
            } else if("redirect".equals(localName)) {
                isRedirect = true;
                state = null;
            } else {
                state = null;
            }
        } else {
            isPage = "page".equals(localName);
            if(isPage) {
                skipCharacters = !(WikipediaIndexer.minId <= WikipediaIndexer.docId.get());
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(skipCharacters) return;
        if(state == null) return;

        switch (state) {
            case title:
                title = new String(ch, start, length);
                break;
            case text:
                text.append(ch, start, length);
                break;
            case ns:
                ns = Long.parseLong(new String(ch, start, length));
                break;
            default:
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        state = null;
        if("page".equals(localName)) {
            WikipediaIndexer.docId.incrementAndGet();
            // we're finished ... submit it as a doc
            if(WikipediaIndexer.normalize) {
                title = WikipediaIndexer.normalizeString(title);
                if(!isRedirect && ns == 0 && title.matches("^[A-Za-z][A-Za-z ]{2,}$")) {
                    if(WikipediaIndexer.minId <= WikipediaIndexer.docId.get()) {
                        handleDocument(Long.toString(WikipediaIndexer.docId.get()), title, text);
                    }
                }
            } else {
                if(!isRedirect && ns == 0) {
                    if(WikipediaIndexer.minId <= WikipediaIndexer.docId.get()) {
                        handleDocument(Long.toString(WikipediaIndexer.docId.get()), title, text);
                    }
                }
            }

            text = new StringBuilder();
            ns = Long.MIN_VALUE;
            isPage = false;
            isRedirect = false;
        }
    }

    protected void handleDocument(String id, String title, StringBuilder content) { }
}

class WikiParserCallable implements Callable<Void> {
    private static final ThreadLocal<WikiModel> parser = new ThreadLocal<WikiModel>() {
        @Override
        protected WikiModel initialValue() {
            return new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
        }
    };

    private final String id;
    private final String title;
    private final StringBuilder content;

    private final Set<String> entities = new HashSet<String>();

    WikiParserCallable(String id, String title, StringBuilder content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    private static final Pattern lineReducer = Pattern.compile("\n{2,}");

    @Override
    public Void call() throws Exception {
        if(WikipediaIndexer.checkExists && Long.parseLong(id) <= WikipediaIndexer.stopCheck) {
            InputStream rawStream = WikipediaIndexer.client.resource(WikipediaIndexer.MAIN_URL).
                    path(WikipediaIndexer.RESOURCE_NAME).
                    path("select").
                    queryParam("q", "*:*").
                    queryParam("fq", "id:" + WikipediaIndexer.RESOURCE_NAME + id).
                    queryParam("wt", "json").
                    get(InputStream.class);
            Map<String, Object> rawData = new ObjectMapper().readValue(rawStream, Map.class);
            Map<String, Object> response = (Map<String, Object>)rawData.get("response");
            if(((Integer)response.get("numFound")) > 0) {
                return null;
            }
        }

        String contents = content.toString();
        contents = parser.get().render(new EntityExtractingConverter(entities), contents);


        final HashMap<String, Object> jsonObj = new HashMap<String, Object>();
        jsonObj.put("id", WikipediaIndexer.RESOURCE_NAME + id);
        jsonObj.put("title", title);
        jsonObj.put("text", lineReducer.matcher(contents).replaceAll("\n"));
        if(!entities.isEmpty()) {
            jsonObj.put("keywords", new ArrayList<String>(entities));
        }
        jsonObj.put("resourcename", WikipediaIndexer.RESOURCE_NAME);

        try {
            WebResource resource = WikipediaIndexer.client.resource(WikipediaIndexer.MAIN_URL).path(WikipediaIndexer.UPDATE_URL);
            long docId = WikipediaIndexer.count.incrementAndGet();
            if(docId % WikipediaIndexer.COMMIT_LIMIT == 0) {
                resource = resource.queryParam("commit", "true");
            }
            resource.type(MediaType.APPLICATION_JSON_TYPE).
                     post(String.class, new ObjectMapper().writeValueAsString(Arrays.asList(jsonObj)));
        } catch (UniformInterfaceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClientHandlerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }
}

class WikiVisitorCallable implements Callable<Void> {
    private static final ThreadLocal<WikiModel> parser = new ThreadLocal<WikiModel>() {
        @Override
        protected WikiModel initialValue() {
            return new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
        }
    };

    private final String id;
    private final String title;
    private final StringBuilder content;

    private final Set<String> entities = new HashSet<String>();

    WikiVisitorCallable(String id, String title, StringBuilder content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    private static final Pattern lineReducer = Pattern.compile("\n{2,}");

    @Override
    public Void call() throws Exception {
        String contents = content.toString();
        contents = parser.get().render(new EntityExtractingConverter(entities), contents);

        String contentsNoSpace = lineReducer.matcher(contents).replaceAll("\n");

        for(int i = 2; i < 22; i++) {
            Pattern searchPattern = ConstraintPattern.createConstraintPatternForConstraint(String.format("%" + i + "s", "").replace(' ', '\u0000'));
            for (String text: Arrays.asList(title, contentsNoSpace)) {
                Matcher matcher = searchPattern.matcher(text);
                while(matcher.find()) {
                    String match = matcher.group(0);
                    String answer = match.replaceAll("[^a-zA-Z]", "").toUpperCase();

                    if(!answer.isEmpty()) {
                        synchronized (WikipediaIndexer.possibleAnswers) {
                            if(WikipediaIndexer.possibleAnswers.remove(answer)) {
                                System.out.println("Found: " + answer + ", " + WikipediaIndexer.possibleAnswers.size());
                            }
                        }
                    }
                }
            }
        }


        return null;
    }
}

class EntityExtractingConverter implements ITextConverter {
    private final Set<String> entities;

    EntityExtractingConverter(Set<String> entities) {
        this.entities = entities;
    }

    public void nodesToText(List<? extends Object> nodes,
          Appendable resultBuffer, IWikiModel model) throws IOException {
        if (nodes != null && !nodes.isEmpty()) {
          try {
            int level = model.incrementRecursionLevel();

            if (level > Configuration.RENDERER_RECURSION_LIMIT) {
              resultBuffer
                  .append("Error - recursion limit exceeded rendering tags in PlainTextConverter#nodesToText().");
              return;
            }
            Iterator<? extends Object> childrenIt = nodes.iterator();
            while (childrenIt.hasNext()) {
              Object item = childrenIt.next();
              if (item != null) {
                if (item instanceof List) {
                  nodesToText((List) item, resultBuffer, model);
                } else if (item instanceof ContentToken) {
                  ContentToken contentToken = (ContentToken) item;
                  String content = contentToken.getContent();
                  Utils.escapeXmlToBuffer(content, resultBuffer, true, true, true);
                } else if (item instanceof WPList) {
                  ((WPList) item).renderPlainText(this, resultBuffer, model);
                } else if (item instanceof WPTable) {
                  ((WPTable) item).renderPlainText(this, resultBuffer, model);
                } else if (item instanceof HTMLTag) {
                    HTMLTag htmlTag = (HTMLTag) item;
                    StringBuilder builder = new StringBuilder();

                    if("a".equals(htmlTag.getName())) {
                        htmlTag.getBodyString(builder);
                        String entity = builder.toString();
                        if(entity.matches("^[A-Za-z][A-Za-z,. ]{2,}$")) {
                            entities.add(entity);
                        }
                        resultBuffer.append(builder);
                    }

                } else if (item instanceof TagNode) {
                  TagNode node = (TagNode) item;
                  Map<String, Object> map = node.getObjectAttributes();
                  if (map != null && map.size() > 0) {
                  } else {
                    node.getBodyString(resultBuffer);
                  }
                }
              }
            }
          } finally {
            model.decrementRecursionLevel();
          }
        }
      }

      public boolean noLinks() {
        return false;
      }

      public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat,
          Appendable resultBuffer, IWikiModel model) throws IOException {

      }
}

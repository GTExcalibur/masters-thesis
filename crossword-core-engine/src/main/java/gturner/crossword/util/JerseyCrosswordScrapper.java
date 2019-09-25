package gturner.crossword.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.core.util.ReaderWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/3/13
 * Time: 9:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class JerseyCrosswordScrapper {

    public static void main(String[] args) throws Exception {
//        about_com_scrape();
//        fleeting_image_com_scape();
//        xwordInfo_pre_1993();
        urban_dictionary_scrape();
    }

    private static void urban_dictionary_scrape() throws Exception {
        List<String> letters = Arrays.asList("S");

        Client client = Client.create();

        for(int i = 1200; i < 1230; i++) {

            for (String letter : letters) {
                System.out.println(letter+i);

                try {

                    InputStream inputStream = client.resource("http://www.urbandictionary.com/browse.php").
                            queryParam("character", letter).
                            queryParam("page", Integer.toString(i)).
                            get(InputStream.class);
                    FileOutputStream out = new FileOutputStream("C:\\Temp\\urbandictionary\\scrape\\" + letter+i + ".html");
                    ReaderWriter.writeTo(inputStream, out);
                    out.close();
                } catch (UniformInterfaceException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ClientHandlerException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                Thread.sleep(100);
            }
        }
    }

    private static void about_com_scrape() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMddyy");
        Calendar instance = Calendar.getInstance();
        instance.clear();
        instance.setTime(sdf.parse("Apr1310"));

        Client client = Client.create();

        for(int i = 0; i < 100000; i++) {
            instance.add(Calendar.DATE, 1);
            if(instance.getTimeInMillis() > System.currentTimeMillis()) {
                break;
            }

            int dayOfWeek = instance.get(Calendar.DAY_OF_WEEK);
            if(dayOfWeek < Calendar.MONDAY || dayOfWeek > Calendar.WEDNESDAY) {
                continue;
            }

            String resource = sdf.format(instance.getTime()) + ".puz";
            System.out.println(resource);

            try {

                InputStream inputStream = client.resource("http://puzzles.about.com/library/across/" + resource).get(InputStream.class);
                FileOutputStream out = new FileOutputStream("C:\\Temp\\puz\\" + resource);
                ReaderWriter.writeTo(inputStream, out);
                out.close();
            } catch (UniformInterfaceException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClientHandlerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Thread.sleep(100);
        }
    }

    private static void fleeting_image_com_scape() throws Exception {
        List<String> paths = Arrays.asList("bg110102.puz", "bg110109.puz", "bg110116.puz", "bg110123.puz", "bg110130.puz", "bg110206.puz", "bg110213.puz", "bg110220.puz", "bg110227.puz", "bg110306.puz", "bg110313.puz", "bg110320.puz", "bg110327.puz", "bg110403.puz", "bg110410.puz", "bg110417.puz", "bg110424.puz", "bg110501.puz", "bg110508.puz", "bg110515.puz", "bg110522.puz", "bg110529.puz", "bg110605.puz", "bg110612.puz", "bg110619.puz", "bg110626.puz", "bg110703.puz", "bg110710.puz", "bg110717.puz", "bg110724.puz", "bg110731.puz", "bg110807.puz", "bg110814.puz", "bg110821.puz", "bg110828.puz", "bg110904.puz", "bg110911.puz", "bg110918.puz", "bg110925.puz", "bg111002.puz", "bg111009.puz", "bg111016.puz", "bg111023.puz", "bg111020.puz", "bg111106.puz", "bg111113.puz", "bg111120.puz", "bg111127.puz", "bg111204.puz", "bg111211.puz", "bg111218.puz", "bg111225.puz", "wsj120106.puz", "wsj120113.puz", "wsj120120.puz", "wsj120127.puz", "wsj120203.puz", "wsj120210.puz", "wsj120217.puz", "wsj120224.puz", "wsj120302.puz", "wsj120309.puz", "wsj120316.puz", "wsj120323.puz", "wsj120330.puz", "wsj120406.puz", "wsj120413.puz", "wsj120420.puz", "wsj120427.puz", "wsj120504.puz", "wsj120511.puz", "wsj120518.puz", "wsj120525.puz", "wsj120601.puz", "wsj120608.puz", "wsj120615.puz", "wsj120622.puz", "wsj120629.puz", "wsj120706.puz", "wsj120713.puz", "wsj120720.puz", "wsj120727.puz", "wsj120803.puz", "wsj120810.puz", "wsj120817.puz", "wsj120824.puz", "wsj120831.puz", "wsj120907.puz", "wsj120914.puz", "wsj120921.puz", "wsj120928.puz", "wsj121005.puz", "wsj121012.puz", "wsj121019.puz", "wsj121026.puz", "wsj121102.puz", "wsj121109.puz", "wsj121116.puz", "wsj121123.puz", "wsj121130.puz", "wsj121207.puz", "wsj121214.puz", "wsj121221.puz", "wsj121228.puz", "wsj130104.puz", "wsj130111.puz", "wsj130118.puz", "wsj130125.puz", "wsj130201.puz", "wsj130208.puz", "wsj130215.puz", "wsj130222.puz", "wsj130301.puz", "wsj130308.puz", "wsj130315.puz", "wsj130322.puz", "wsj130329.puz", "wsj130405.puz", "wsj130412.puz", "wsj130419.puz", "wsj130426.puz", "wsj130503.puz", "wsj130510.puz", "wsj130517.puz", "wsj130524.puz", "wsj130531.puz", "wsj130607.puz", "wsj130614.puz", "wsj130621.puz", "wsj130628.puz", "wsj130705.puz", "wsj130712.puz", "wsj130719.puz", "wsj130726.puz", "wsj130802.puz", "wsj130809.puz", "wsj130816.puz", "wsj130823.puz", "wsj130830.puz", "wsj130906.puz", "wsj130913.puz", "wsj130920.puz", "wsj130927.puz", "wsj131004.puz", "wsj131011.puz", "wsj131018.puz", "wsj131025.puz", "wsj131101.puz", "wsj131108.puz", "wsj131115.puz", "wsj131122.puz", "wsj131129.puz", "wsj131206.puz", "wsj131213.puz", "wsj131220.puz", "wsj131227.puz");

        Client client = Client.create();

        for (String resource : paths) {

            System.out.println(resource);

            try {

                InputStream inputStream = client.resource("http://mazerlm.home.comcast.net/" + resource).get(InputStream.class);
                FileOutputStream out = new FileOutputStream("C:\\Temp\\puz\\" + resource);
                ReaderWriter.writeTo(inputStream, out);
                out.close();
            } catch (UniformInterfaceException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClientHandlerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Thread.sleep(100);
        }
    }

    private static void xwordInfo_pre_1993() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
        Calendar instance = Calendar.getInstance();
        instance.clear();
        instance.setTime(sdf.parse("11/20/1993"));

        Client client = Client.create();

        for(int i = 0; i < 100000; i++) {
            instance.add(Calendar.DATE, 1);
            if(instance.getTimeInMillis() > System.currentTimeMillis()) {
                break;
            }

            String resource = sdf.format(instance.getTime());
            System.out.println(resource);

            try {

                InputStream inputStream = client.resource("http://www.xwordinfo.com/PS?date=" + resource).
                        header("Cookie", "\t__utma=16936880.249669471.1303059810.1362332294.1362336918.8; ASP.NET_SessionId=1gzhwo4oe1civozeouuzy4za; __utmc=16936880; __utmz=16936880.1362332294.7.2.utmcsr=fleetingimage.com|utmccn=(referral)|utmcmd=referral|utmcct=/wij/xyzzy/nyt-links.html; __utmb=16936880.9.10.1362336918; .ASPXAUTH=2EFAAFC92C218B9A4045906A633112C4132857E1DC757B0FF9F48BBE42E814A8DE7DD44A83DD01266E7335CD157B963097CD00FDC2619422F005CA52F32F79A4492F5D91E1062B7CD261C1DBA8C1BA52E28CC80508EB77AB6F64AC97F305032A62D6DD71").
                        get(InputStream.class);
                FileOutputStream out = new FileOutputStream("C:\\Temp\\puz\\" + resource.replace("/", "-") + ".html");
                ReaderWriter.writeTo(inputStream, out);
                out.close();
            } catch (UniformInterfaceException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClientHandlerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Thread.sleep(100);
        }
    }

}

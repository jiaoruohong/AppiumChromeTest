package appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class ChromeTest {
    public static AppiumDriver<MobileElement> driver;
    public static DesiredCapabilities cap;
    public static WebDriverWait wait;
    public static URL url;
    public static boolean ans;
    public static String filePath;

    public static void init() throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String time=df.format(new Date()).toString();

        filePath=System.getProperty("user.dir") + "/out/log_"+time+".txt";
        File f=new File(filePath);
        f.createNewFile();
    }

    public static void emit(String msg) {
        System.out.println(msg);

        File f=new File(filePath);
        FileWriter fw=null;
        try {
            fw=new FileWriter(f,true);
            BufferedWriter out= new BufferedWriter(fw);
            out.write(msg.toString()+"\n");
            out.close();
        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        init();
        try {
            openChrome();
        }catch (Exception exp){
            System.out.println(exp.getCause());
            System.out.println(exp.getMessage());
            exp.printStackTrace();
        }
    }

    public static void openChrome() throws Exception {
        cap=new DesiredCapabilities();
        cap.setCapability("deviceName","sdk_gphone_x86");
        cap.setCapability("udid","emulator-5554");
        cap.setCapability("platformName","Android");
        cap.setCapability("platformVersion","10");
        cap.setCapability("appPackage","com.android.chrome");
        cap.setCapability("appActivity","org.chromium.chrome.browser.document.ChromeLauncherActivity");

        url= new URL("http://127.0.0.1:4723/wd/hub");
        driver = new AppiumDriver<MobileElement>(url,cap);
        wait = new WebDriverWait(driver,10000);

        emit("Application Started ...");

//        google login
        emit("google login");
        MobileElement noReports=waitAndBuildElement("class","android.widget.CheckBox");
        noReports.click();
        MobileElement goOn=waitAndBuildElement("id","com.android.chrome:id/terms_accept");
        goOn.click();
        MobileElement withoutSignIn=waitAndBuildElement("id","com.android.chrome:id/negative_button");
        withoutSignIn.click();
        MobileElement searchBox=waitAndBuildElement("id","com.android.chrome:id/search_box_text");
        searchBox.click();

//        visit baidu
        emit("visit baidu");
        MobileElement urlBar=waitAndBuildElement("id","com.android.chrome:id/url_bar");
        urlBar.sendKeys("www.baidu.com");
//        list view
        emit("list view");
        MobileElement omitBox=waitAndBuildElement("id","com.android.chrome:id/omnibox_results_container");

        List<MobileElement> list=omitBox.findElements(By.className("android.view.ViewGroup"));

        Integer sizeOfList=list.size();
        emit("size of list: "+sizeOfList/2);
        Integer count=0;
        for(int i=0;i<sizeOfList;i+=2){
            emit("####################");
            emit("item "+count+":");
            MobileElement item = list.get(i);
            List<MobileElement> contents=item.findElements(By.className("android.widget.TextView"));
            for(MobileElement text : contents){
                emit(text.getText());
            }
            count+=1;
        }
        list.get(0).click();
//        block location in baidu
        emit("block location in baidu");
        MobileElement locationBlock=waitAndBuildElement("class","android.widget.ScrollView");

        wait.until(presenceOfElementLocated(By.className("android.widget.Button")));
        List<MobileElement> blockBtn= locationBlock.findElements(By.className("android.widget.Button"));
        for(MobileElement btn : blockBtn){
            String str=btn.getText();
            if(str.equals("Block")){
                btn.click();
                break;
            }
        }

//        ###########################################
        emit("####################");
        emit("Queries:");
        List<List<String>> query=fetchQuery(System.getProperty("user.dir")+"/src/main/resources/query.csv",",");
        int szQuery=query.size();
        int szSuccess=0;
        for(List<String> item : query){
            ans=false;
            String schQuery=item.get(0);
            String tstToken=item.get(1);

            for(int i=0;i<5;i++) {
                MobileElement bdQueryBox = waitAndBuildElement("className", "android.widget.EditText");
                bdQueryBox.sendKeys(schQuery);

                MobileElement bdQueryBtn = waitAndBuildElement("className", "android.widget.Button");
                bdQueryBtn.click();

                try {
                    wait.until(presenceOfElementLocated(
                            By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout" +
                                    "/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout" +
                                    "/android.view.ViewGroup/android.widget.FrameLayout[1]/android.widget.FrameLayout[2]" +
                                    "/android.webkit.WebView/android.view.View/android.view.View[2]/android.view.View[1]" +
                                    "/android.view.View[1]")));
                    String pageSources = driver.getPageSource();
                    if (pageSources.contains(tstToken)) {
                        ans = true;
                        szSuccess+=1;
                        break;
                    }
                } catch (Exception exp) {
                    System.out.println(exp.getCause());
                    System.out.println(exp.getMessage());
                    exp.printStackTrace();
                }
            }

            emit("Query: "+schQuery+": "+tstToken+": "+(ans?"Success":"Failure"));
        }

        emit("Total Queries: "+szQuery+
                " Success : "+szSuccess+
                " Failure : "+(szQuery-szSuccess));

        driver.quit();
        emit("Completed ...");
    }

    public static List<List<String>> fetchQuery(String csvPath, String COMMA_DELIMITER){
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                records.add(Arrays.asList(values));
            }
        } catch (IOException exp) {
            exp.printStackTrace();
        }
        return records;
    }

    public static MobileElement waitAndBuildElement(String type, String id){
        if(type.toUpperCase().contains("ID")){
            wait.until(presenceOfElementLocated(By.id(id)));
            return driver.findElement(By.id(id));
        }else if(type.toUpperCase().contains("CLASS")){
            wait.until(presenceOfElementLocated(By.className(id)));
            return driver.findElement(By.className(id));
        }else if(type.toUpperCase().contains("XPATH")){
            wait.until(presenceOfElementLocated(By.xpath(id)));
            return driver.findElement(By.xpath(id));
        }else{
//            pass
        }
        return null;
    }
}

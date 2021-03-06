package websvr.action;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.util.Base64;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import websvr.bean.OARequest;
import websvr.bean.OAResponse;
import websvr.bean.ReceiveRequest;

import websvr.client.WebServiceClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class WebSvrAction extends MultiActionController {

    private static final String init_view = "initPage.jsp";

    Log log = LogFactory.getLog(WebSvrAction.class);
    // webservice 首页
    public ModelAndView goInitPage(HttpServletRequest request, HttpServletResponse response)  {
        response.setContentType("text/html;charset=utf-8");
        ModelAndView view = new ModelAndView(init_view);
        return view;
    }
    


  

    @RequestMapping(value="upload", method=RequestMethod.POST)
    public String upload(HttpServletRequest request ,HttpServletResponse response) throws Exception {
        response.setContentType("text/json;charset=utf-8");
        long  startTime=System.currentTimeMillis();
        //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(
                request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if(multipartResolver.isMultipart(request))
        {
            //将request变成多部分request
            MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
            //获取multiRequest 中所有的文件名
            Iterator iter=multiRequest.getFileNames();
            while(iter.hasNext())
            {
                //一次遍历所有文件
                MultipartFile file=multiRequest.getFile(iter.next().toString());
                if(file!=null)
                {
                    System.out.println(file.getOriginalFilename());
                    //获取输入流 CommonsMultipartFile 中可以直接得到文件的流
                    InputStream is=file.getInputStream();
                    long fileSize = file.getSize();
                    // 断点
                    long  startpost = 0;
                   String download = this.download(is, fileSize,startpost);
                   is.close();
                   
                   // System.out.println(download);
                    System.out.println("文件大小: "+ fileSize );
                    // webservice 发送参数
                    String fileID = UUID.randomUUID().toString().replaceAll("-", "");
                    String fileTypeID =  "0EECE637379440F4A6F31E62CC00B66F" ;
                    String fileObject  = download;
                    byte[] buffer = file.getBytes();
                    String fileMD5 =  getMD5(buffer);
                    String fileName = file.getOriginalFilename().toString();
                    String fileType = "审计通知书";
                    String id = UUID.randomUUID().toString();
                    String  prjID =  "b27828bb-5421-4692-948a-70eeb8159103";
                    String prjName = "test预算";
                    String submmitterID  = "35462100001";
                    String submmitterName  = "於鸣杰" ;
                    // 传递给webservice
                    WebServiceClient websvrClient  = WebServiceClient.getWebServiceClient();
                    
                    OARequest oaRequest = new OARequest();
                    
                    String  oaResponse = websvrClient.testClient(id ,id,prjID ,prjName,fileTypeID, fileType ,
                            submmitterID,submmitterName,fileID,fileName,fileObject , fileMD5 );
                  log.info(oaResponse);
                    
                    
                }

            }

        }
        long  endTime=System.currentTimeMillis();
        return "success";
    }

    public  String getMD5(byte[] input){
        StringBuffer sb = new StringBuffer("");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input);
            byte b[] = md.digest();
            int d;
            for (int i = 0; i < b.length; i++) {
                d = b[i];
                if (d < 0) {
                    d = b[i] & 0xff;
                    // 与上一行效果等同
                    // i += 256;
                }
                if (d < 16){
                    sb.append("0");}
                sb.append(Integer.toHexString(d)); }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString() ;
    }

    @RequestMapping("/down")
    public void down(HttpServletRequest request,HttpServletResponse response) throws Exception{
    

        String fileName = request.getSession().getServletContext().getRealPath("upload")+"/101.jpg";

        InputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)));

        String filename = "下载文件.jpg";

        filename = URLEncoder.encode(filename,"UTF-8");

        response.addHeader("Content-Disposition", "attachment;filename=" + filename);

        response.setContentType("multipart/form-data");

        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        int len = 0;
        while((len = bis.read()) != -1){
            out.write(len);
            out.flush();
        }
        out.close();
    }


    public String download(InputStream fis,long filesize, long startpost) throws Exception {
        //System.out.println("要下载的文件名是："+filename);
        int BUFFER_LENGTH = 1024 * 20;//一次性读入大小
        int SLEEP_TIME=250;//循环读次数
        int time=0;
        String ret=null;
        String str=null;
        fis.skip(startpost);//先定位
        try {
            StringBuffer sb = new StringBuffer();
            System.out.println("定位："+startpost);
            fis.skip(startpost);//先定位
            byte[] buffer = new byte[BUFFER_LENGTH];
            int count;
            while (time<SLEEP_TIME && (count = fis.read(buffer)) != -1 ) {
                sb.append(Base64.encode(buffer,0,count));
                time++;
            }
            ret = sb.toString();
            System.out.println("输出："+ret.length());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new Exception("出错啦！", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("出错啦！", e);
        } catch (Exception e) {
            throw new Exception("出错啦！", e);
        } finally {
            fis.close();
        }


        return ret;
    }
}

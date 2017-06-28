package qingning.common.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by 宫洪深 on 2017/2/28.
 * 上传东西到七牛服务器
 */
public class QiNiuUpUtils {
    private static Auth auth;

    static {//利用
        auth = Auth.create(MiscUtils.getConfigByKey("qiniu_AK"), MiscUtils.getConfigByKey("qiniu_SK"));
    }

    /**
     * 青牛服务器 字节上传
     *
     * @param uploadBytes 字节
     * @param fileName    文件名
     * @return
     */
    public static String uploadByIO(byte[] uploadBytes, String fileName) {
        String upToken = auth.uploadToken("qnzhixiang-test");
        Configuration cfg = new Configuration(Zone.zone0());
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            Response response = uploadManager.put(uploadBytes, fileName, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            String url = MiscUtils.getConfigByKey("images_space_test_name") + "/" + putRet.key;
            return url;
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
        return null;
    }

    public static String uploadByIo(ByteArrayInputStream byteInputStream, String fileName) {
        String url = null;
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        //String key = null;
        //uploadBytes = "hello qiniu cloud".getBytes("utf-8");
        //ByteArrayInputStream byteInputStream = new ByteArrayInputStream(uploadBytes);
        String upToken = auth.uploadToken(MiscUtils.getConfigByKey("image_space_test"),fileName);
        try {
            Response response = uploadManager.put(byteInputStream, fileName, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
            url = MiscUtils.getConfigByKey("images_space_test_name") + "/" + putRet.key;
        } catch (QiniuException ex) {
            ex.printStackTrace();
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
        return url;
    }
    public static String uploadByArray(byte[] uploadBytes, String fileName) {
        String url = null;
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //String upToken = auth.uploadToken(MiscUtils.getConfigByKey("images_space_test_name"));
        String upToken = auth.uploadToken("qnzhixiang-test",fileName);
        try {
            Response response = uploadManager.put(uploadBytes, fileName, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
            url = MiscUtils.getConfigByKey("images_space_test_name") + "/" + putRet.key;
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
        return url;
    }
    public static void main(String args[]) throws IOException {
        /*String file = readFileContent("E:\\ag10.jpg");
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(file.getBytes("utf-8"));
        System.out.println(uploadByIo(byteInputStream,"test.jpg"));*/
        //讲师头像
        String lecturer_avatar_address = "http://onk0xuj5w.bkt.clouddn.com/WEB_FILE/20170329140000c50d98ca35f64bf1bda227283eb74fee";
        //讲师名称
        String lecturer_name = "谷子和姜";
        //讲师头衔
        String lecturer_title = "VIP";
        //课程标题
        String course_title = "课程标题";
        //课程时长
        long time = 100L;
        //二维码内容:拼接分享链接
        String share_course_url_prefic = MiscUtils.getConfigByKey("share_course_url_prefic");	//从配置文件获取课程分享链接前缀

        StringBuilder urlSB = new StringBuilder(share_course_url_prefic).append("?course_id=")
                .append("d2260847f7c4e2c7f6849510dc072a0f").append("&shop_id=").append("00001e611658e38c48568e160bcbc4cb65a4");
        String courseUrl = HttpTookit.UrlEncode(urlSB.toString());
        String shareUrl = new StringBuilder(MiscUtils.getConfigByKey("share_imp_url")).append("?url=").append(courseUrl).toString();

        BufferedImage createCourseSharingPng = null;
        try {
            createCourseSharingPng = ZXingUtil.createCourseSharingPng(lecturer_avatar_address, lecturer_name,
                    lecturer_title, course_title, shareUrl, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //生成的图片位置
        String imagePath1= "E:/RoomDistributerPng1.png";
        ImageIO.write(createCourseSharingPng, imagePath1.substring(imagePath1.lastIndexOf(".") + 1), new File(imagePath1));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(createCourseSharingPng, "png", baos);
        byte[] bytes = baos.toByteArray();
        String qiniuUrl = QiNiuUpUtils.uploadByArray(bytes,"d2260847f7c4e2c7f6849510dc072a0f1"+".png");
        System.out.println(qiniuUrl);
    }
    private static String readFileContent(String fileName) throws IOException {

        File file = new File(fileName);

        BufferedReader bf = new BufferedReader(new FileReader(file));

        String content = "";
        StringBuilder sb = new StringBuilder();

        while(content != null){
            content = bf.readLine();

            if(content == null){
                break;
            }

            sb.append(content.trim());
        }

        bf.close();
        return sb.toString();
    }
}


package com.lxkj.aiim.lj.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.lxkj.aiim.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AppUtil {
    /**
     * 屏幕密度
     */
    public static float sDensity = 0f;


    /**
     * 获取 DisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获取View高度
     */
    public static int OutputView(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int high = view.getMeasuredHeight();
        return high;
    }

    public static int pixelToDp(Context context, int pixel) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return pixel < 0 ? pixel : Math.round(pixel / displayMetrics.density);
    }

    public static int dpToPixel(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return dp < 0 ? dp : Math.round(dp * displayMetrics.density);
    }

    /**
     * 根据百分比改变颜色透明度
     */
    public static int changeAlpha(int color, float fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = (int) (Color.alpha(color) * fraction);
        return Color.argb(alpha, red, green, blue);
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        return width;
    }

    public static Uri getTakePhotoUri() {
        File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        Uri imageUri = Uri.fromFile(file);
        return imageUri;
    }

    /**
     * 将图片转换成Base64编码的字符串
     *
     * @param path
     * @return base64编码的字符串
     */
    public static String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }


    public static float getDensity(Context context) {
        if (sDensity == 0f) {
            sDensity = getDisplayMetrics(context).density;
        }
        return sDensity;
    }

    /**
     * 单位转换: dp -> px
     */
    public static int dp2px(Context context, int dp) {
        return (int) (getDensity(context) * dp + 0.5);
    }

    public static void invokingBaidu(Activity activity, String nowla, String nowlo, String gola, String golo, String nowname, String goname) {
//com.baidu.BaiduMap

        if (!isAvilible(activity, "com.baidu.BaiduMap")) {
            showMarketDialogBD(activity);
            return;
        }

        try {
//            LngLat start = new LngLat(Double.parseDouble(nowlo), Double.parseDouble(nowla));
//            start = CoodinateCovertor.bd_encrypt(start);
//            LngLat end = new LngLat(Double.parseDouble(golo), Double.parseDouble(gola));
//            end = CoodinateCovertor.bd_encrypt(end);
            Intent i1 = new Intent();
            // 驾车路线规划
            i1.setData(Uri.parse("baidumap://map/direction?region=" + nowname + "&origin=" + nowla + "," + nowlo + "&destination=" + gola + "," + golo + "&mode=driving"));
            activity.startActivity(i1);
        } catch (Exception e) {
            Toast.makeText(activity, "导航失败~", Toast.LENGTH_SHORT).show();
        }
    }


    public static void invokingGD(Activity activity, String nowla, String nowlo, String gola, String golo, String nowname, String goname) {
        //  com.autonavi.minimap这是高德地图的包名
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("androidamap://navi?sourceApplication=com.autonavi.minimap&lat=" + "&dev=0"));
        intent.setPackage("com.autonavi.minimap");
        intent.setData(Uri.parse("amapuri://route/plan/?sid=BGVIS1&slat=" + nowla + "&slon=" + nowlo + "&sname=" + nowname + "&did=BGVIS2&dlat=" + gola + "&dlon=" + golo + "&dname=" + goname + "&dev=0&t=0"));
        if (isInstallByread("com.autonavi.minimap")) {
            activity.startActivity(intent);
        } else {
            showMarketDialog(activity);
        }
    }

    public static void showMarketDialog(final Activity activity) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setMessage("您没有安装高德地图客户端，请下载安装")
                .setNegativeButton("取消", null)
                .setPositiveButton("去下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadGaoDe(activity);
                    }
                }).create();
        dialog.show();
    }

    public static void showMarketDialogBD(final Activity activity) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setMessage("您没有安装百度地图客户端，请下载安装")
                .setNegativeButton("取消", null)
                .setPositiveButton("去下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadBaidu(activity);
                    }
                }).create();
        dialog.show();
    }


    public static void downloadGaoDe(Activity activity) {
        Uri uri = Uri.parse("http://a.app.qq.com/o/simple.jsp?pkgname=com.autonavi.minimap");
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(it);
    }

    public static void downloadBaidu(Activity activity) {
        Uri uri = Uri.parse("http://a.app.qq.com/o/simple.jsp?pkgname=com.baidu.BaiduMap");
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(it);
    }

    /**
     * 判断是否安装目标应用
     *
     * @param packageName 目标应用安装后的包名
     * @return 是否已安装目标应用
     */
    public static boolean isInstallByread(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    public static boolean isAvilible(Context context, String packageName) {
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }


    public static ArrayList<String> getMapValue(HashMap<String, String> map) {
        ArrayList<String> strings = new ArrayList<>();
        if (map == null)
            return strings;

        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            strings.add(map.get(key));
        }
        return strings;
    }


    /**
     * 获取随机验证码
     */
    public static String getNum() {
        StringBuilder sb = new StringBuilder();
        //随机生成6位数  发送到聚合
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int a = random.nextInt(10);
            sb.append(a);
        }
        return sb.toString();
    }

    public static String getHtmlData(String bodyHTML) {

        String head = "<head>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> "
                + "<style>img{max-width: 100%; width:auto; height:auto;}</style>"
                + "</head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }


    /**
     * 打开高德地图（公交出行，起点位置使用地图当前位置）
     * <p>
     * t = 0（驾车）= 1（公交）= 2（步行）= 3（骑行）= 4（火车）= 5（长途客车）
     *
     * @param dlat  终点纬度
     * @param dlon  终点经度
     * @param dname 终点名称
     */
    public static void openGaoDeMap(Context context, double dlat, double dlon, String dname) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage("com.autonavi.minimap");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("androidamap://route?sourceApplication=" + R.string.app_name
                + "&sname=我的位置&dlat=" + dlat
                + "&dlon=" + dlon
                + "&dname=" + dname
                + "&dev=0&m=0&t=1"));
        context.startActivity(intent);
    }

    /**
     * mode = transit（公交）、driving（驾车）、walking（步行）和riding（骑行）. 默认:driving
     *  * 当 mode=transit 时 ： sy = 0：推荐路线 、 2：少换乘 、 3：少步行 、 4：不坐地铁 、 5：时间短 、 6：地铁优先
     * @param dlat
     * @param dlon
     * @param dname
     */
    public static void openBaiduMap(Context context,double dlat, double dlon, String dname) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("baidumap://map/direction?origin=我的位置&destination=name:"
                + dname
                + "|latlng:" + dlat + "," + dlon
                + "&mode=transit&sy=3&index=0&target=1"));
        context.startActivity(intent);
    }

    /**
     * 打开腾讯地图（公交出行，起点位置使用地图当前位置）
     *
     * 公交：type=bus，policy有以下取值
     * 0：较快捷 、 1：少换乘 、 2：少步行 、 3：不坐地铁
     * 驾车：type=drive，policy有以下取值
     * 0：较快捷 、 1：无高速 、 2：距离短
     * policy的取值缺省为0
     *
     * @param dlat  终点纬度
     * @param dlon  终点经度
     * @param dname 终点名称
     */
    public static void openTencent(Context context,double dlat, double dlon, String dname) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("qqmap://map/routeplan?type=bus&from=我的位置&fromcoord=0,0"
                + "&to=" + dname
                + "&tocoord=" + dlat + "," + dlon
                + "&policy=1&referer=myapp"));
        context.startActivity(intent);
    }

}


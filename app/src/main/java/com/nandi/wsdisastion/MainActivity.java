package com.nandi.wsdisastion;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SceneView mMapView;
    private GraphicsOverlay graphicsOverlay;
    private List<DisasterPoint> disasterPoints;
    private ArcGISMapImageLayer imageLayer;
    private ArcGISSceneLayer sceneLayer;
    private ArcGISMapImageLayer shiLiangLayer;
    private ArcGISMapImageLayer dianZiLayer;
    private ArcGISMapImageLayer dengZXLayer;
    private ArcGISMapImageLayer ssylLayer;
    private ArcGISScene scene;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        graphicsOverlay = new GraphicsOverlay();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // inflate MapView from layout
        mMapView = (SceneView) findViewById(R.id.sceneView);
        // create a MapImageLayer with dynamically generated map images
        imageLayer = new ArcGISMapImageLayer(getResources().getString(R.string.world_elevation_service));
        sceneLayer = new ArcGISSceneLayer(getResources().getString(R.string.qinnxiesheying_url));
        shiLiangLayer = new ArcGISMapImageLayer(getResources().getString(R.string.shiliangtu_url));
        dianZiLayer = new ArcGISMapImageLayer(getResources().getString(R.string.dianziditu_url));
        dengZXLayer=new ArcGISMapImageLayer(getResources().getString(R.string.yuliang_url));
        ssylLayer=new ArcGISMapImageLayer(getResources().getString(R.string.ssyl_url));
        // create an empty map instance
        scene = new ArcGISScene();
        scene.setBasemap(Basemap.createImagery());
        mMapView.setScene(scene);
        mMapView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // get the screen point where user tapped
                android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());
                // identify graphics on the graphics overlay
                final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false, 2);

                identifyGraphic.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IdentifyGraphicsOverlayResult identifyGraphicsOverlayResult = identifyGraphic.get();
                            if (identifyGraphicsOverlayResult.getGraphics().size() > 0) {

                                int zIndex = identifyGraphicsOverlayResult.getGraphics().get(0).getZIndex();
                                showInfo(zIndex);
                            }
                        } catch (InterruptedException | ExecutionException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                return super.onSingleTapConfirmed(e);
            }
        });
    }

    private void showInfo(int zIndex) {
        OkHttpUtils.get().url(getResources().getString(R.string.get_disaster_info))
                .addParams("id", zIndex + "")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(getApplicationContext(), "网络连接失败！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<DisasterInfo>>() {
                        }.getType();
                        List<DisasterInfo> disasterInfos = gson.fromJson(response, type);
                        DisasterInfo disasterInfo = disasterInfos.get(0);
                        String info = "名称：" + disasterInfo.getDis_name() + "\n"
                                + "地点：" + disasterInfo.getDis_location() + "\n"
                                + "经纬度：" + disasterInfo.getDis_lon() + "," + disasterInfo.getDis_lat() + "\n"
                                + "灾害因素：" + disasterInfo.getDis_cause() + "\n"
                                + "受灾面积：" + disasterInfo.getDis_area() + "\n"
                                + "受灾体积：" + disasterInfo.getDis_volume() + "\n"
                                + "威胁户数：" + disasterInfo.getImperil_families() + "\n"
                                + "威胁人数：" + disasterInfo.getImperil_man() + "\n"
                                + "威胁房屋：" + disasterInfo.getImperil_house() + "\n"
                                + "威胁房屋面积：" + disasterInfo.getImperil_area() + "\n"
                                + "影响对象：" + disasterInfo.getMain_object() + "\n"
                                + "威胁财产：" + disasterInfo.getImperil_money() + "\n"
                                + "灾害等级：" + disasterInfo.getImperil_level() + "\n"
                                + "是否涉水：" + (disasterInfo.getDis_sfss() == 1 ? "是" : "否") + "\n"
                                + "告警号码:" + disasterInfo.getWarn_mobile() + "\n"
                                + "入库时间:" + disasterInfo.getCome_time() + "\n";
                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_disaster_info, null);
                        TextView tvInfo = (TextView) view.findViewById(R.id.dialog_text);
                        tvInfo.setText(info);
                        new AlertDialog.Builder(MainActivity.this)
                                .setView(view)
                                .show();
                    }
                });
    }


    private void setOverlay() {
        BitmapDrawable pinStarBlueDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.mipmap.ic_launcher);
        final PictureMarkerSymbol pinStarBlueSymbol = new PictureMarkerSymbol(pinStarBlueDrawable);
        //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
        //its appearance would then differ across devices with different resolutions.
        pinStarBlueSymbol.setHeight(20);
        pinStarBlueSymbol.setWidth(20);
        //Optionally set the offset, to align the base of the symbol aligns with the point geometry
        pinStarBlueSymbol.setOffsetY(
                11); //The image used for the symbol has a transparent buffer around it, so the offset is not simply height/2
        pinStarBlueSymbol.loadAsync();
        //[DocRef: END]
        pinStarBlueSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                for (DisasterPoint disasterPoint : disasterPoints) {
                    Point point = new Point(Double.valueOf(disasterPoint.getDis_lon()), Double.valueOf(disasterPoint.getDis_lat()), SpatialReferences.getWgs84());
                    Graphic graphic = new Graphic(point, pinStarBlueSymbol);
                    graphic.setZIndex(disasterPoint.getId());
                    graphicsOverlay.getGraphics().add(graphic);
                }
            }
        });
    }

    private void initData() {
        OkHttpUtils.get().url(getResources().getString(R.string.get_disaster_point))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(getApplicationContext(), "网络连接失败！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<DisasterPoint>>() {
                        }.getType();
                        disasterPoints = gson.fromJson(response, type);
                        Log.d("WSD", "集合大小：" + disasterPoints.size() + "\n数据:" + disasterPoints);
                        setOverlay();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_slt) {
            toolbar.setTitle("矢量图");
            mMapView.getGraphicsOverlays().clear();
            scene.getOperationalLayers().clear();
            scene.getBaseSurface().getElevationSources().clear();
            scene.getOperationalLayers().add(shiLiangLayer);
            Camera camera = new Camera(28.969167, 106.950399, 50000.0, 295.0, 20, 0.0);
            mMapView.setViewpointCamera(camera);
        } else if (id == R.id.nav_dzdt) {
            toolbar.setTitle("电子地图");
            mMapView.getGraphicsOverlays().clear();
            scene.getOperationalLayers().clear();
            scene.getBaseSurface().getElevationSources().clear();
            scene.getOperationalLayers().add(dianZiLayer);
            Camera camera = new Camera(28.969167, 106.950399, 50000.0, 295.0, 20, 0.0);
            mMapView.setViewpointCamera(camera);
        } else if (id == R.id.nav_yxt) {
            toolbar.setTitle("影像图");
            mMapView.getGraphicsOverlays().add(graphicsOverlay);
            initData();
            scene.getOperationalLayers().clear();
            ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
                    getResources().getString(R.string.elevation_url));
            scene.getBaseSurface().getElevationSources().add(elevationSource);
            scene.getOperationalLayers().add(imageLayer);
            Camera camera = new Camera(28.969167, 106.950399, 50000.0, 295.0, 20, 0.0);
            mMapView.setViewpointCamera(camera);
        } else if (id == R.id.nav_qxsy) {
            toolbar.setTitle("倾斜摄影");
            scene.getOperationalLayers().clear();
            scene.getBaseSurface().getElevationSources().clear();
            scene.getOperationalLayers().add(sceneLayer);
            Camera camera = new Camera(30.07010,102.75326, 1289.0, 295.0, 20, 0.0);
            mMapView.setViewpointCamera(camera);
        } else if(id==R.id.nav_dzx){
            toolbar.setTitle("雨量等值线");
            scene.getOperationalLayers().clear();
            mMapView.getGraphicsOverlays().clear();
            scene.getBaseSurface().getElevationSources().clear();
            scene.getOperationalLayers().add(dengZXLayer);
            Camera camera = new Camera(28.969167, 106.950399, 50000.0, 295.0, 20, 0.0);
            mMapView.setViewpointCamera(camera);
        }else if (id==R.id.nav_ssyl){
            toolbar.setTitle("实时雨量");
            scene.getOperationalLayers().clear();
            mMapView.getGraphicsOverlays().clear();
            scene.getBaseSurface().getElevationSources().clear();
            scene.getOperationalLayers().add(shiLiangLayer);
            scene.getOperationalLayers().add(ssylLayer);
            Camera camera = new Camera(28.969167, 106.950399, 50000.0, 295.0, 20, 0.0);
            mMapView.setViewpointCamera(camera);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }
}

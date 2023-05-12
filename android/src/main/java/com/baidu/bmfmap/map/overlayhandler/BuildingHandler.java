package com.baidu.bmfmap.map.overlayhandler;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.bmfmap.BMFMapController;
import com.baidu.bmfmap.utils.Constants;
import com.baidu.bmfmap.utils.Env;
import com.baidu.bmfmap.utils.converter.FlutterDataConveter;
import com.baidu.bmfmap.utils.converter.TypeConverter;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.Building;
import com.baidu.mapapi.map.BuildingOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.BuildingInfo;

import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * 3.1.0 3D棱柱
 */
public class BuildingHandler extends OverlayHandler {
    private static final String TAG = "PrismHandler";

    public BuildingHandler(BMFMapController bmfMapController) {
        super(bmfMapController);
    }

    @Override
    public void handlerMethodCall(MethodCall call, MethodChannel.Result result) {
        super.handlerMethodCall(call, result);

        if (Env.DEBUG) {
            Log.d(TAG, "handlerMethodCall enter");
        }

        if (null == result) {
            return;
        }
        Map<String, Object> argument = call.arguments();
        if (null == argument) {
            if (Env.DEBUG) {
                Log.d(TAG, "argument is null");
            }
            result.success(false);
            return;
        }

        String methodId = call.method;
        boolean ret = false;
        switch (methodId) {
            case Constants.MethodProtocol.PrismProtocol.MAP_ADD_PRISM_OVERLAY_METHOD:
                ret = addPrismOverlay(argument);
                break;
            case Constants.MethodProtocol.PrismProtocol.MAP_UPDATE_PRISM_OVERLAY_MEMBER_METHOD:
                ret = updatePrismOverlay(argument);
                break;
            default:
                break;
        }

        result.success(ret);
    }

    private boolean updatePrismOverlay(Map<String, Object> argument) {
        if (null == mMapController || null == mMapController.mOverlayIdMap) {
            return false;
        }
        String id = new TypeConverter<String>().getValue(argument, "id");
        if (TextUtils.isEmpty(id)) {
            if (Env.DEBUG) {
                Log.d(TAG, "id is null");
            }
            return false;
        }
        Building building = (Building) mMapController.mOverlayIdMap.get(id);
        if (null == building) {
            if (Env.DEBUG) {
                Log.d(TAG, "building is null");
            }
            return false;
        }

        String member = new TypeConverter<String>().getValue(argument, "member");
        if (TextUtils.isEmpty(member)) {
            if (Env.DEBUG) {
                Log.d(TAG, "member is null");
            }
            return false;
        }

        boolean isSuccess = false;
        switch (member) {
            case "coordinates":
                isSuccess = updateCoordinates(argument, building);
                break;
            case "buildInfo":
                isSuccess = updateBuildInfo(argument, building);
                break;
            case "topFaceColor":
                isSuccess = updateTopFaceColor(argument, building);
                break;
            case "sideFaceColor":
                isSuccess = updateSideFaceColor(argument, building);
                break;
            case "sideFacTexture":
                isSuccess = updateSideFacTexturer(argument, building);
                break;
            default:
                break;
        }

        return isSuccess;
    }

    private boolean updateSideFacTexturer(Map<String, Object> argument, Building building) {
        String sideFacImage = new TypeConverter<String>().getValue(argument, "value");
        if (TextUtils.isEmpty(sideFacImage)) {
            return false;
        }
        BitmapDescriptor icon = FlutterDataConveter.getIcon(sideFacImage);
        if (null == icon) {
            return false;
        }
        building.setCustomSideImage(icon);
        return true;
    }

    private boolean updateSideFaceColor(Map<String, Object> argument, Building building) {
        String sideFaceColor = new TypeConverter<String>().getValue(argument, "value");
        if (TextUtils.isEmpty(sideFaceColor)) {
            return false;
        }
        Integer color = FlutterDataConveter.getColor(sideFaceColor);
        if (null == color) {
            return false;
        }
        building.setSideFaceColor(color);
        return true;
    }

    private boolean updateTopFaceColor(Map<String, Object> argument, Building building) {
        String topFaceColor = new TypeConverter<String>().getValue(argument, "value");
        if (TextUtils.isEmpty(topFaceColor)) {
            return false;
        }
        Integer color = FlutterDataConveter.getColor(topFaceColor);
        if (null == color) {
            return false;
        }
        building.setTopFaceColor(color);
        return true;
    }

    private boolean updateBuildInfo(Map<String, Object> argument, Building building) {
        Map<String, Object> buildInfoMap = new TypeConverter<Map<String, Object>>()
                .getValue(argument, "value");
        if (null == buildInfoMap) {
            return false;
        }
        BuildingInfo buildingInfo = building.getBuildingInfo();
        if (buildingInfo == null) {
            buildingInfo = new BuildingInfo();
        }
        if (buildInfoMap.containsKey("height")) {
            Double height = (Double) buildInfoMap.get("height");
            if (null != height) {
                buildingInfo.setHeight(height.floatValue());
            }
        }
        if (buildInfoMap.containsKey("accuracy")) {
            Double accuracy = (Double) buildInfoMap.get("accuracy");
            if (null != accuracy) {
                buildingInfo.setAccuracy(accuracy.intValue());
            }
        }

        if (buildInfoMap.containsKey("paths")) {
           String paths = (String) buildInfoMap.get("paths");
            if (!TextUtils.isEmpty(paths)) {
                buildingInfo.setGeom(paths);
            }
        }

        if (buildInfoMap.containsKey("center")) {
            String center = (String) buildInfoMap.get("center");
            if (!TextUtils.isEmpty(center)) {
                buildingInfo.setCenter(center);
            }
        }

        building.setBuildingInfo(buildingInfo);
        return true;
    }

    private boolean updateCoordinates(Map<String, Object> argument, Building building) {
        List<Map<String, Double>> coordinates =
                new TypeConverter<List<Map<String, Double>>>().getValue(argument,
                        "value");

        if (null == coordinates) {
            return false;
        }

        List<LatLng> latLngList = FlutterDataConveter.mapToLatlngs(coordinates);
        if (null == latLngList) {
            return false;
        }
        building.setPoints(latLngList);
        return true;
    }


    private boolean addPrismOverlay(Map<String, Object> argument) {
        BaiduMap baiduMap = mMapController.getBaiduMap();
        if (baiduMap == null) {
            return false;
        }

        if (!argument.containsKey("id")
                || !argument.containsKey("coordinates")) {
            if (Env.DEBUG) {
                Log.d(TAG, "argument does not contain");
            }
            return false;
        }

        final String id = new TypeConverter<String>().getValue(argument, "id");
        if (TextUtils.isEmpty(id)) {
            if (Env.DEBUG) {
                Log.d(TAG, "id is null");
            }
            return false;
        }

        if (mOverlayMap.containsKey(id)) {
            return false;
        }

        BuildingOptions buildingOptions = new BuildingOptions();
        setOptions(argument, buildingOptions);

        final Overlay overlay = baiduMap.addOverlay(buildingOptions);
        if (null == overlay) {
            return false;
        }

        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        overlay.setExtraInfo(bundle);
        mOverlayMap.put(id, overlay);
        mMapController.mOverlayIdMap.put(id, overlay);
        
        return true;
    }

    private boolean setOptions(Map<String, Object> prismOptionsMap, BuildingOptions buildingOptions) {
        if (null == prismOptionsMap || null == buildingOptions) {
            return false;
        }

        List<Map<String, Double>> coordinates =
                (List<Map<String, Double>>) prismOptionsMap.get("coordinates");
        List<LatLng> latLngList = FlutterDataConveter.mapToLatlngs(coordinates);
        if (null == latLngList) {
            if (Env.DEBUG) {
                Log.d(TAG, "latLngList is null");
            }
            return false;
        }

        String topFaceColorStr = (String) prismOptionsMap.get("topFaceColor");
        if (TextUtils.isEmpty(topFaceColorStr)) {
            return false;
        }

        String sideFaceColorStr = (String) prismOptionsMap.get("sideFaceColor");
        String sideFacTextureStr = (String) prismOptionsMap.get("sideFacTexture");
        if (TextUtils.isEmpty(sideFaceColorStr) &&
                TextUtils.isEmpty(sideFacTextureStr)) {
            return false;
        }

        buildingOptions.setPoints(latLngList);

        Integer topFaceColor = FlutterDataConveter.getColor(topFaceColorStr);
        buildingOptions.setTopFaceColor(topFaceColor);

        if (!TextUtils.isEmpty(sideFaceColorStr)) {
            Integer sideFaceColor = FlutterDataConveter.getColor(sideFaceColorStr);
            buildingOptions.setSideFaceColor(sideFaceColor);
        }   

        /*
         * color和icon只能使用一个
         */
        if (!TextUtils.isEmpty(sideFacTextureStr)) {
            BitmapDescriptor sideFacTexture = FlutterDataConveter.getIcon(sideFacTextureStr);
            buildingOptions.customSideImage(sideFacTexture);
        }

        Map<String, Object> buildInfoMap = (Map<String, Object>) prismOptionsMap.get("buildInfo");
        BuildingInfo buildInfo = createBuildInfo(buildInfoMap);
        if (buildInfo != null) {
            buildingOptions.setBuildingInfo(buildInfo);
        }

        Integer height = new TypeConverter<Integer>().getValue(prismOptionsMap, "height");
        if (null != height) {
            buildingOptions.setHeight(height);
        }

        Boolean visible = new TypeConverter<Boolean>().getValue(prismOptionsMap, "visible");
        if (null != visible) {
            buildingOptions.visible(visible);
        }

        return true;
    }

    private BuildingInfo createBuildInfo(Map<String, Object> buildInfoMap) {
        if (buildInfoMap == null || buildInfoMap.size() <= 0) {
            return null;
        }

        BuildingInfo buildingInfo = new BuildingInfo();
        Double height = (Double) buildInfoMap.get("height");
        Double accuracy = (Double) buildInfoMap.get("accuracy");
        String paths = (String) buildInfoMap.get("paths");
        String center = (String) buildInfoMap.get("center");
        if (!TextUtils.isEmpty(paths)) {
            buildingInfo.setGeom(paths);
        }

        if (!TextUtils.isEmpty(center)) {
            buildingInfo.setCenter(center);
        }

        if (null != height) {
            buildingInfo.setHeight(height.floatValue());
        }

        if (null != accuracy) {
            buildingInfo.setAccuracy(accuracy.intValue());
        }

        return buildingInfo;
    }
}

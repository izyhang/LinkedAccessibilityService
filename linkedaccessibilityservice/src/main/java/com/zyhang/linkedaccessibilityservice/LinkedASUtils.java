package com.zyhang.linkedaccessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by zyhang on 2018/8/22.15:25
 */

public class LinkedASUtils {

    public static boolean isServiceOpen(@NonNull Context context, @NonNull Class<? extends AccessibilityService> cls) {
        try {
            int enable = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
            if (enable != 1)
                return false;
            String services = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (!TextUtils.isEmpty(services)) {
                TextUtils.SimpleStringSplitter split = new TextUtils.SimpleStringSplitter(':');
                split.setString(services);
                while (split.hasNext()) {
                    if (split.next().equalsIgnoreCase(context.getPackageName() + "/" + cls.getName()))
                        return true;
                }
            }
        } catch (Throwable e) {
            Log.e("LinkedASUtils", "isServiceOpen: " + e.getMessage());
        }
        return false;
    }

    /**
     * jump to accessibility setting page
     *
     * @param context the context
     */
    public static void toAccessibilitySetting(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * find node by text
     *
     * @param rootNode The root node if this service can retrieve window content.
     * @param text     target text
     * @return True if node exist and click
     */
    public static boolean isTextExist(AccessibilityNodeInfo rootNode, String text) {
        List<AccessibilityNodeInfo> list = null;
        if (null != rootNode) {
            list = rootNode.findAccessibilityNodeInfosByText(text);
        }
        return list != null && !list.isEmpty();
    }

    /**
     * find node by text
     *
     * @param rootNode The root node if this service can retrieve window content.
     * @param text     target text
     * @param fuzzy    fuzzy query
     * @return True if node exist and click
     */
    public static boolean isTextExist(AccessibilityNodeInfo rootNode, String text, boolean fuzzy) {
        if (!fuzzy) return isTextExist(rootNode, text);
        if (null == rootNode) return false;
        if (!TextUtils.isEmpty(rootNode.getText()) && rootNode.getText().toString().contains(text)) {
            return true;
        }
        for (int i = 0, size = rootNode.getChildCount(); i < size; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                continue;
            }
            boolean exist = isTextExist(nodeInfo, text, true);
            if (exist) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds Button {@link AccessibilityNodeInfo}s.
     *
     * @param rootNode The root node if this service can retrieve window content.
     * @return A list of node info.
     */
    public static List<AccessibilityNodeInfo> findAllButton(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return Collections.emptyList();
        }
        List<AccessibilityNodeInfo> list = new ArrayList<>();
        if ("android.widget.Button".equals(rootNode.getClassName().toString())) {
            list.add(rootNode);
        }
        for (int i = 0, size = rootNode.getChildCount(); i < size; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                continue;
            }
            List<AccessibilityNodeInfo> childList = findAllButton(nodeInfo);
            if (!childList.isEmpty()) {
                list.addAll(findAllButton(nodeInfo));
            }
        }
        return list;
    }

    /**
     * find node by text and click
     *
     * @param rootNode The root node if this service can retrieve window content.
     * @param text     target text
     * @param reverse  True if reverse
     * @return True if node exist
     */
    public static boolean findAndClickByText(AccessibilityNodeInfo rootNode, String text, boolean reverse) {
        boolean find = false;
        if (null != rootNode) {
            List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText(text);
            if (reverse) {
                find = findAndClickByTextReverse(list);
            } else {
                find = findAndClickByTextOrder(list);
            }
        }
        return find;
    }

    private static boolean findAndClickByTextOrder(List<AccessibilityNodeInfo> list) {
        boolean find = false;
        for (AccessibilityNodeInfo node : list) {
            if (node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                find = true;
                break;
            } else {
                AccessibilityNodeInfo parent = node.getParent();
                while (null != parent) {
                    if (parent.isClickable()) {
                        find = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                    parent = parent.getParent();
                }
                if (find) {
                    break;
                }
            }
        }
        return find;
    }

    private static boolean findAndClickByTextReverse(List<AccessibilityNodeInfo> list) {
        boolean find = false;
        for (int size = list.size(), i = size - 1; i >= 0; i--) {
            AccessibilityNodeInfo node = list.get(i);
            if (node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                find = true;
                break;
            } else {
                AccessibilityNodeInfo parent = node.getParent();
                while (null != parent) {
                    if (parent.isClickable()) {
                        find = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                    parent = parent.getParent();
                }
                if (find) {
                    break;
                }
            }
        }
        return find;
    }
}

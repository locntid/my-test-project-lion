/*
 * CopyRight (C) 2013 NewTech CORP LTD.
 * ProcessInfo.java
 */
package com.newtech.taskmanager;

import java.util.ArrayList;
import java.util.Comparator;

import com.newtech.taskmanager.util.Constants;
import com.newtech.taskmanager.util.SystemProcess;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ProcessInfo implements Comparator<Object> {

    private static Comparator<Object> mInstnceComparator;

    private RunningAppProcessInfo mRunningInfo;

    private ApplicationInfo mAppInfo;

    private ArrayList<RunningServiceInfo> mServiceList;

    private String mProcessName;
//    private String mServiceName;

    private int mMemory;

    // Loading them in updateBasicInfo() to improve the performance
    private Drawable mIcon;
    private Intent mIntent;
    private String mName;

    private boolean mIsSystemProcess = false;

    ProcessInfo(String packageName) {
        mProcessName = packageName;
    }

	@Override
	public int compare(Object arg0, Object arg1) {
		if (arg0 instanceof ProcessInfo && arg1 instanceof ProcessInfo) {
			ProcessInfo info0 = (ProcessInfo) arg0;
			ProcessInfo info1 = (ProcessInfo) arg1;
			if (info0.mMemory > info1.mMemory) {
				return -1;
			} else if (info0.mMemory < info1.mMemory) {
				return 1;
			}
		}
		return 0;
	}

	public int getUid() {
		return mRunningInfo.uid;
	}

	public int getPid() {
		if (mRunningInfo != null) {
			return mRunningInfo.pid;
		}
		return -1;
	}

	public Drawable getIcon(PackageManager pm) {
		return mIcon;
	}

	public String getName(PackageManager pm) {
		return mName;
	}

	public int getImportance() {
		if (mRunningInfo != null) {
			return mRunningInfo.importance;
		}
		return -1;
	}

	public String getPackageName() {
		return mAppInfo.packageName;
	}

	public String getProcessName() {
	    return mProcessName;
	}

	public void setMemory(int memory) {
		mMemory = memory;
	}

	public int getMemory() {
		return mMemory;
	}

	private void setSystemProcess(boolean isSystemProcess) {
	    mIsSystemProcess = isSystemProcess;
	}

	public boolean isSystemProcess() {
		// return (mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
		if ( getUid() < Constants.USER_PROCESS_ID) {
			return true;
		}
		return mIsSystemProcess;
	}

    public boolean isService() {
        if (getImportance() <= RunningAppProcessInfo.IMPORTANCE_SERVICE) {
            return true;
        } else {
            return false;
        }
    }

	public RunningAppProcessInfo getRunningInfo() {
		return mRunningInfo;
	}

	public void setRunningInfo(RunningAppProcessInfo mRunningInfo) {
		this.mRunningInfo = mRunningInfo;
	}

	public ApplicationInfo getAppInfo() {
		return mAppInfo;
	}

	public void setAppInfo(ApplicationInfo mAppInfo) {
		this.mAppInfo = mAppInfo;
	}

	public void addService(RunningServiceInfo serviceInfo) {
		if (mServiceList == null) {
			mServiceList = new ArrayList<RunningServiceInfo>();
		}
		mServiceList.add(serviceInfo);
	}

	public int getServicesSize() {
		if (mServiceList != null) {
			return mServiceList.size();
		}
		return 0;
	}

	public Intent getIntent() {
		return mIntent;
	}

	public void killSelf(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Activity.ACTIVITY_SERVICE);
		am.killBackgroundProcesses(getPackageName());
//        if (isService()) {
//            context.stopService(new Intent().setComponent());
//        }
		if (mServiceList != null) {
			for (RunningServiceInfo service : mServiceList) {
				context.stopService(new Intent().setComponent(service.service));
			}
		}
	}

	public void updateBasicInfo(PackageManager pm) {
		mName = mAppInfo.loadLabel(pm).toString();
		mIcon = mAppInfo.loadIcon(pm);
		setSystemProcess(SystemProcess.isSystemList(mProcessName));

		try {
			mIntent = pm.getLaunchIntentForPackage(mAppInfo.packageName);
			if (mIntent != null) {
				mIntent = mIntent.cloneFilter();
				mIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				return;
			}
			PackageInfo pkginfo = pm.getPackageInfo(mAppInfo.packageName,
					PackageManager.GET_ACTIVITIES);
			if (pkginfo != null && pkginfo.activities != null
					&& pkginfo.activities.length == 1) {
				mIntent = new Intent(Intent.ACTION_MAIN);
				mIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				mIntent.setClassName(pkginfo.packageName,
						pkginfo.activities[0].name);
			}
		} catch (NameNotFoundException e) {
			mIntent = null;
		}
	}

	public static Comparator<Object> getComparator() {
		if (mInstnceComparator == null) {
			mInstnceComparator = new ProcessInfo(null);
		}
		return mInstnceComparator;
	}
}
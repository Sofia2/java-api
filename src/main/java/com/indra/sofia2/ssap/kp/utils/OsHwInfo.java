package com.indra.sofia2.ssap.kp.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.sun.jna.Platform;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystemVersion;

public class OsHwInfo {
	public static Map<String, String> getFullStatus() {
		Map<String,String> status = new HashMap<String, String>();
		
		status.putAll(getRuntimeStatus());
		status.putAll(getOshiSummarizeStatus());
		
		return status;
	}
	
	public static Map<String, String> getSystemStatus() {
		Map<String,String> status = new HashMap<String, String>();
		Properties p = System.getProperties();
		
		for (Entry<Object, Object> entry : p.entrySet()) {
			String key = (String)entry.getKey();
		    String value = (String)entry.getValue();
		    status.put(key, value);
		}
		return status;
	}
	
	
	public static Map<String, String> getRuntimeStatus() {
		Map<String, String> status = new HashMap<String, String>();
		Runtime runtime = Runtime.getRuntime();
		
		status.put("java_runtime_freememory", String.valueOf(runtime.freeMemory()));
		status.put("java_runtime_totalmemory", String.valueOf(runtime.totalMemory()));
		status.put("java_runtime_maxmemory", String.valueOf(runtime.maxMemory()));
		status.put("java_runtime_availableprocessors", String.valueOf(runtime.availableProcessors()));
		
		return status;
	}
	
	public static Map<String, String> getOshiSummarizeStatus() {
		Map<String,String> status = new HashMap<String, String>();
		SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        GlobalMemory memory = hal.getMemory();
        
        
        status.put("total_meory", String.valueOf(memory.getTotal()));
        status.put("availabe_memory", String.valueOf(memory.getAvailable()));
        status.put("total_swap_meory", String.valueOf(memory.getSwapTotal()));
				
        OperatingSystem os = si.getOperatingSystem();
        status.put("os_family", os.getFamily());
        status.put("os_manufacturer", os.getManufacturer());
        
        OperatingSystemVersion version = os.getVersion();
        status.put("os_version",version.getVersion());
        status.put("os_codename",version.getCodeName());

        oshi.software.os.FileSystem filesystem = si.getHardware().getFileSystem();
        OSFileStore[] fs = filesystem.getFileStores();
		int i = 0;	
        for (OSFileStore store : fs) {
            status.put(String.format("disk_%d_type", i), store.getType());
            status.put(String.format("disk_%d_mount", i), store.getMount());
        }
        
        CentralProcessor p = si.getHardware().getProcessor();

        status.put("processor_name", p.getName());
        status.put("processor_identifier", p.getIdentifier());
        status.put("processor_logicalprocessorcount", String.valueOf(p.getLogicalProcessorCount()));
        
        return status;
        
	}
	
	public static Map<String, String> getOshiStatus() {
		Map<String,String> status = new HashMap<String, String>();
		SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        GlobalMemory memory = hal.getMemory();
        
        status.put("total_meory", String.valueOf(memory.getTotal()));
        status.put("availabe_memory", String.valueOf(memory.getAvailable()));
        
        status.put("total_swap_meory", String.valueOf(memory.getSwapTotal()));
        status.put("total_swap_meory", String.valueOf(memory.getSwapUsed()));
		
		 Sensors s = si.getHardware().getSensors();
	     status.put("cpu_temperature", String.valueOf(s.getCpuTemperature()));
		
		
		long timeStamp = System.currentTimeMillis();
		
        OperatingSystem os = si.getOperatingSystem();
        status.put("os_family", os.getFamily());
        status.put("os_manufacturer", os.getManufacturer());
        OperatingSystemVersion version = os.getVersion();

        status.put("os_version",version.getVersion());
        status.put("os_codename",version.getCodeName());
        status.put("os_buildnumber",version.getBuildNumber());

        oshi.software.os.FileSystem filesystem = si.getHardware().getFileSystem();
        status.put("filesystem_openfiledescriptors", String.valueOf(filesystem.getOpenFileDescriptors()));
        status.put("filesystem_maxfiledescriptors", String.valueOf(filesystem.getMaxFileDescriptors()));
        OSFileStore[] fs = filesystem.getFileStores();
		int i = 0;	
        for (OSFileStore store : fs) {
        	
            status.put(String.format("disk_%d_name", i), store.getName());
            status.put(String.format("disk_%d_volume", i), store.getVolume());
            status.put(String.format("disk_%d_description", i), store.getDescription());
            status.put(String.format("disk_%d_type", i), store.getType());
            status.put(String.format("disk_%d_mount", i), store.getMount());
            status.put(String.format("disk_%d_UUID", i), store.getUUID());
            status.put(String.format("disk_%d_totalspace", i), String.valueOf(store.getTotalSpace()));
            status.put(String.format("disk_%d_usablespace", i), String.valueOf(store.getUsableSpace()));
		}
		
        
		
        CentralProcessor p = si.getHardware().getProcessor();

        status.put("processor_vendor", p.getVendor());
        status.put("processor_vendorfreq", String.valueOf(p.getVendorFreq()));
        status.put("processor_name", p.getName());
        status.put("processor_identifier", p.getIdentifier());
        status.put("processor_is_64bit", String.valueOf(p.isCpu64bit()));
        status.put("processor_stepping", p.getStepping());
        status.put("processor_model", p.getModel());
        status.put("processor_familiy", p.getFamily());
        status.put("processor_cpuloadbtwticks", String.valueOf(p.getSystemCpuLoadBetweenTicks()));
        
        if (!(Platform.isFreeBSD() && Platform.iskFreeBSD() && Platform.isNetBSD() && Platform.isOpenBSD()) ) {
        	status.put("processor_cpuload", String.valueOf(p.getSystemCpuLoad()));
        }
        
        if (Platform.isMac() || Platform.isLinux()) {
        	status.put("processor_loadaverage", String.valueOf(p.getSystemLoadAverage()));
        }

        status.put("processor_uptime", String.valueOf(p.getSystemUptime()));
        status.put("processor_serialnumber", p.getSystemSerialNumber());
        status.put("processor_logicalprocessorcount", String.valueOf(p.getLogicalProcessorCount()));
        	
		return status;
	}
}

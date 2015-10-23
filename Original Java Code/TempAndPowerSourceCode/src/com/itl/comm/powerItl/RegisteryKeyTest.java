package com.itl.comm.powerItl;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

/**
 * Just a test of JNA's ability to get and set 
 * registry values. There's no reason to keep it 
 * around except for the notes here.
 * 
 * @author kgraba
 *
 */
public class RegisteryKeyTest {
	public static void main(String[] args) {
		System.out.println("OS: " + System.getenv("OS"));

		System.out.println(Advapi32Util.getUserName());

		// Note that while we can use System.getevn to get the
		// path environment variable, we can't alter it at all.
		// Fortunately, there's a registry setting somewhere
		// which contains the path. With JNA, we can of course
		// set that.
		System.out.println("Path: ");
		String[] tokens = System.getenv("Path").split(";");
		for (String s : tokens) {
			System.out.printf("  %s%n", s);
		}

		String regVal = Advapi32Util.registryGetStringValue(
				WinReg.HKEY_LOCAL_MACHINE, "Software\\7-Zip", "Path");
		System.out.println("7-zip path: " + regVal);

		String[] keys = Advapi32Util.registryGetKeys(
				WinReg.HKEY_LOCAL_MACHINE, 
				"SYSTEM\\CurrentControlSet\\Control\\ComputerName\\ComputerName");
		regVal = Advapi32Util.registryGetStringValue(
				WinReg.HKEY_LOCAL_MACHINE, 
				"SYSTEM\\CurrentControlSet\\Control\\ComputerName\\ComputerName", 
				"ComputerName");
		System.out.println("ComputerName: " + regVal);
		
		regVal = Advapi32Util.registryGetStringValue(
				WinReg.HKEY_LOCAL_MACHINE, 
				"Software\\JavaSoft\\Java Development Kit", 
				"CurrentVersion");
		System.out.println("JDK current version: " + regVal);
		
		try {
			String rootDir;
			rootDir = Advapi32Util.registryGetStringValue(
					WinReg.HKEY_LOCAL_MACHINE, 
					"Software\\Wow6432Node\\Universal Library", 
					"RootDir");
			System.out.println("Universal Library Root Dir: " + rootDir);
		} catch (Win32Exception e) {
			// this exception is thrown if the key doesn't exist
			System.out.printf("Problem getting ItemProperties %s at %s%n",
					"RootDir", 
					"Registry::HKEY_LOCAL_MACHINE\\Software\\Wow6432Node\\Universal Library");
			System.out.println(e.getMessage());
		}

		try {
			String configDir;
			configDir = Advapi32Util.registryGetStringValue(
					WinReg.HKEY_LOCAL_MACHINE, 
					"Software\\Wow6432Node\\Universal Library", 
					"ConfigDir");
			System.out.println("Universal Library Config Dir: " + configDir);
		} catch (Win32Exception e) {
			// this exception is thrown if the key doesn't exist
			System.out.printf("Problem getting ItemProperties %s at %s%n",
					"ConfigDir", 
					"Registry::HKEY_LOCAL_MACHINE\\Software\\Wow6432Node\\Universal Library");
			System.out.println(e.getMessage());
		}
	}
}

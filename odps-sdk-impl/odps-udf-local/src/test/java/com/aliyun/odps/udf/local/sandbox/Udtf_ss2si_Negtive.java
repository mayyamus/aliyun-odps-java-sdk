/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.aliyun.odps.udf.local.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import com.aliyun.odps.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.aliyun.odps.Odps;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.local.common.Constants;
import com.aliyun.odps.local.common.WareHouse;
import com.aliyun.odps.local.common.security.SecurityClient;
import com.aliyun.odps.udf.ExecutionContext;
import com.aliyun.odps.udf.UDFException;
import com.aliyun.odps.udf.UDTF;
import com.aliyun.odps.udf.annotation.Resolve;
import com.aliyun.odps.udf.local.LocalRunException;
import com.aliyun.odps.udf.local.runner.BaseRunner;
import com.aliyun.odps.udf.local.runner.UDTFRunner;

// TODO define input and output types, e.g., "d string,string->string,bigint".
@Resolve({"string,string->string,bigint"})
public class Udtf_ss2si_Negtive extends UDTF {
  public static String basePath;

  public native static int add(int x, int y);

  static Odps odps;

  @Before
  public void before() {
    Account account = new AliyunAccount("accessId", "accessKey");
    odps = new Odps(account);
    odps.setEndpoint("endpoint");
    odps.setDefaultProject("project_name");

    basePath = WareHouse.getInstance().getResourceDir(odps.getDefaultProject()).getAbsolutePath();
  }


  public void setup(ExecutionContext ctx) throws UDFException {


    Assert.assertNotNull(System.getSecurityManager());

    // case 1 permission java.io.FilePermission "." ,"read,write,delete";
    boolean flag = false;
    try {
      File f = new File(".");
      if (f.exists()) {
        System.out.println("hello world");
      }
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);

    // case 2 permission java.io.FilePermission "." ,"read,write,delete";
    flag = false;
    try {
      URL url = new URL("http://www.baidu.com");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      InputStreamReader reader = new InputStreamReader(conn.getInputStream());
      BufferedReader br = new BufferedReader(reader);
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
      }
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);

    // case 3 permission java.io.FilePermission "<<ALL FILES>>" ,"execute";
    flag = false;
    try {
      Runtime.getRuntime().exec("ls");
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);

    // case 4 permission java.lang.RuntimePermission "modifyThreadGroup";
    flag = false;
    try {
      new N().start();
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);

    // case 5 permission java.lang.RuntimePermission "createClassLoader";
    flag = false;
    try {
      URL[] urls = new URL[] {new URL("http://www.aliyun.com/")};
      new URLClassLoader(urls);
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);


    // case 6 permission java.net.SocketPermission "localhost:1024-", "listen,resolve";
    flag = false;
    try {
      DatagramSocket socket = new DatagramSocket();
      socket.getLocalAddress();
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);


    // case 7 permission java.lang.RuntimePermission "loadLibrary.test.so";"
    flag = false;
    try {
      System.load(basePath + "/test.so");
      Assert.assertEquals(3, add(1, 2));
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);

    // case 8: permission java.lang.RuntimePermission "getProtectionDomain";"
    flag = false;
    try {

      System.loadLibrary("test");
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);

    // case 9: permission java.lang.RuntimePermission "getProtectionDomain";"
    flag = false;
    try {

      System.out.println(UdfExampleNegtive.class.getProtectionDomain().getCodeSource()
          .getLocation().getPath());
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);


    // case 10: permission java.lang.RuntimePermission "readSystemProperty";
    flag = false;
    try {
      System.out.println(System.getProperty("java.class.path"));
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);

    // case 11: permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
    flag = false;
    try {
      Field field = UdfExampleNegtive.class.getDeclaredField("privateVar");
      field.setAccessible(true);
      Assert.assertEquals("odps", field.get(null));
    } catch (Exception exception) {
      flag = true;
    }
    Assert.assertTrue(flag);
  }

  public static class N extends Thread {
    public void run() {
      File f = new File(".");
      if (f.exists()) {
        System.out.println("OK");
      }
      Assert.assertNotNull(System.getSecurityManager());
    }
  }

  private static String privateVar = "odps";

  @Override
  public void process(Object[] args) throws UDFException {
    String a = (String) args[0];
    long b = args[1] == null ? 0 : ((String) args[1]).length();

    forward(a, b);

  }

  @Test
  public void test() throws LocalRunException, UDFException {
    SecurityClient.clear();

    // open security switch
    System.setProperty(Constants.LOCAL_SECURITY_ENABLE, "true");


    // runner = new UDTFRunner(null, "com.aliyun.odps.udf.local.examples.Udtf_ss2si");
    BaseRunner runner = new UDTFRunner(odps, new Udtf_ss2si_Negtive());
    runner.feed(new Object[] {"one", "one"}).feed(new Object[] {"three", "three"})
        .feed(new Object[] {"four", "four"});
    List<Object[]> out = runner.yield();
    Assert.assertEquals(3, out.size());
    Assert.assertEquals("one,3", StringUtils.join(out.get(0), ","));
    Assert.assertEquals("three,5", StringUtils.join(out.get(1), ","));
    Assert.assertEquals("four,4", StringUtils.join(out.get(2), ","));

  }
}

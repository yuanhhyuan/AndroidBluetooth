# Android BLE Library
对Android的蓝牙BLE相关API的简单封装，以实现两台Android设备通过蓝牙BLE来交换数据。仅支持5.0以上的系统。

------

# Demo模块
两台Android设备，其中一台启动GattServer（需要先测试是否支持BLE广播），另外一台设备点击扫描即可在扫描结果中看到GattServer，点击连接之后可以互相发送文本消息。

------

# Android BLE Library
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)]()
[![GitHub release](https://img.shields.io/badge/release-1.1.0-brightgreen.svg)]()
[![author](https://img.shields.io/badge/author-czvn-orange.svg)](https://github.com/czvn)
## 引入依赖
You can get the compiled library on [Jcenter](https://bintray.com/czvn/maven/blelibrary/view).

在`build.gradle `中添加一行依赖


```
compile 'me.czvn:blelibrary:1.1.0'
```

## 用法
### 作为 Peripheral
* 开启BLE广播


```
BLEAdvertiser bleAdvertiser = BLEAdvertiser.getInstance(context, BLEAdvertiser.IAdvertiseResultListener);

bleAdvertiser.startAdvertise();
```


* 开启GattServer

```
BLEServer bleServer=BLEServer.getInstance(context, IBLECallback);

bleServer.startGattServer();
```
* 发送数据


```
bleServer.sendData(byte[] data);
```

### 作为 Central
* 扫描周围的Peripheral


```
BLEScanner bleScanner=BLEScanner.getInstance(context, BLEScanner.IScanResultListener);

bleScanner.startScan();
```
在Android M的设备上扫描之前需要先请求运行时权限`ACCESS_COARSE_LOCATION`;

* 连接


```
BLEClient bleClient=new BLEClient(context,IBLECallback);

bleClient.startConnect(String address);
```

* 发送数据

```
bleClient.sendData(byte[] data);

```

------

# Android BLE Library
A simple wrapper for Android bluetooth low energy API.It makes two Android Device to communicate with Bluetooth Low Energy. It can only run in  Lolipop devices(API 21+).

------

# Demo Module
## Usages
* Need two Android Device.
* One as Peripheral by click "StartGattServer", maybe  you should click "TestAdvertise" to make it works well.
* And the other device as Central by click "Scan" to scan the Peripherals, and then you can click the result to connect the Peripherals.
* After the toast "ConnectSuccess", you can send Message.

------

# Android BLE Library
## Installation
You can get the compiled library on [Jcenter](https://bintray.com/czvn/maven/blelibrary/view).

Throw this in your `build.gradle `.


```
compile 'me.czvn:blelibrary:1.1.0'
```

## Usages
### As Peripheral
* Start Advertise


```
BLEAdvertiser bleAdvertiser = BLEAdvertiser.getInstance(context, BLEAdvertiser.IAdvertiseResultListener);

bleAdvertiser.startAdvertise();
```


* Start GattServer

```
BLEServer bleServer=BLEServer.getInstance(context, IBLECallback);

bleServer.startGattServer();
```
* Send Data


```
bleServer.sendData(byte[] data);
```

### As Central
* Start Scan


```
BLEScanner bleScanner=BLEScanner.getInstance(context, BLEScanner.IScanResultListener);

bleScanner.startScan();
```
On Android M device,you need required the `ACCESS_COARSE_LOCATION` permission before the scan;
* Connect


```
BLEClient bleClient=new BLEClient(context,IBLECallback);

bleClient.startConnect(String address);
```

* Send Data

```
bleClient.sendData(byte[] data);

```



License
------

    Copyright 2015 czvn.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.






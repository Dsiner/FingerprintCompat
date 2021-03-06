# FingerprintCompat for Android

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![API](https://img.shields.io/badge/API-9%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=9)

> FingerprintCompat is a library for using registered fingerprints to authenticate the user in your app.

## Set up
Maven:
```xml
<dependency>
  <groupId>com.dsiner.lib</groupId>
  <artifactId>fingerprintcompat</artifactId>
  <version>1.0.0</version>
</dependency>
```
or Gradle:
```groovy
implementation 'com.dsiner.lib:fingerprintcompat:1.0.0'
```

## Screenshots

<p>
   <img src="https://github.com/Dsiner/Resouce/blob/master/lib/FingerprintCompat/fingerprintcompat.png" width="320" alt="Screenshot"/>
</p>

## Getting started

#### 1. Authenticate

```java
        FingerprintCompat.create(mContext).authenticate(new IFingerprint.Callback() {

            @Override
            public void onHelp(int helpMsgId, CharSequence helpString) { }

            @Override
            public void onSuccess(String value) { }

            @Override
            public void onError(@NonNull FingerprintException e) { }
        });
```

#### 2. Encrypt

```java
        FingerprintCompat.create(mContext).encrypt(keyName, value, new IFingerprint.Callback() { });
```

#### 3. Decrypt

```java
        FingerprintCompat.create(mContext).decrypt(keyName, value, new IFingerprint.Callback() { });
```

#### 4. Cancel

```java
        fingerprintCompat.cancel();
```

## Licence

```txt
Copyright 2018 D

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

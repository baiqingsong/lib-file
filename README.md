# lib-file

Android file util library

## 引用

Step 1. Add the JitPack repository to your build file

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```groovy
dependencies {
    implementation 'com.github.baiqingsong:lib-file:Tag'
}
```

## 权限

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 类说明

`com.dawn.library.LFileUtil` 文件工具类，所有方法均为静态方法。

| 方法 | 说明 |
|------|------|
| `isFileExist(String)` | 判断文件是否存在 |
| `isFile(String)` | 判断是否是文件 |
| `isDirectory(String)` | 判断是否是文件夹 |
| `createFile(String)` | 创建文件（若已存在则不创建，自动创建父目录） |
| `createDirectory(String)` | 递归创建多级目录 |
| `deleteFile(String)` | 删除单个文件 |
| `deleteDirectory(String)` | 递归删除文件夹及其内容 |
| `clearDirectory(String)` | 清空文件夹内容（不删除自身） |
| `copyFile(String, String)` | 复制单个文件 |
| `copyDirectory(String, String)` | 递归复制文件夹 |
| `move(String, String)` | 移动文件或文件夹 |
| `getFileSize(String)` | 获取文件大小（字节） |
| `getFileSizeFormatted(String)` | 获取文件大小格式化字符串（KB/MB/GB） |
| `getDirectorySize(String)` | 获取文件夹总大小（字节） |
| `getDirectorySizeFormatted(String)` | 获取文件夹总大小格式化字符串 |
| `formatSize(long)` | 将字节数格式化为可读字符串 |
| `getFileMD5(String)` | 获取文件 MD5 值 |
| `getFileExtension(String)` | 获取文件后缀名 |
| `getFileNameWithoutExtension(String)` | 获取文件名（不含后缀） |
| `getParentPath(String)` | 获取文件所在目录路径 |
| `joinPath(String, String)` | 拼接路径（防止重复斜杠） |
| `readFileToString(String)` | 读取文本文件内容到字符串 |
| `writeStringToFile(String, String, boolean)` | 将字符串写入文件（覆盖/追加） |
| `unzip(String, String)` | 解压 zip 文件到指定目录 |
| `closeIO(Closeable...)` | 安全关闭 IO 流 |

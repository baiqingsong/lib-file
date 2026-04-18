package com.dawn.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件工具类
 * <p>
 * 提供文件与文件夹的创建、删除、复制、移动、读写、压缩解压等常用操作。
 * 所有方法均为静态方法，内部做了空安全处理与异常捕获，不会抛出未检查异常。
 * </p>
 *
 * @author dawn
 * @version 1.0
 */
@SuppressWarnings("unused")
public class LFileUtil {

    private LFileUtil() {
        // 工具类，禁止实例化
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 判断字符串是否为空
     *
     * @param str 待判断的字符串
     * @return 如果为 null 或空白字符串则返回 true
     */
    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 安全关闭一个或多个 IO 流
     *
     * @param closeables 需要关闭的流对象（可变参数）
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (cb != null) {
                    cb.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ==================== 判断类 ====================

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件存在返回 true，路径为空或文件不存在返回 false
     */
    public static boolean isFileExist(String filePath) {
        if (isEmpty(filePath)) return false;
        return new File(filePath).exists();
    }

    /**
     * 判断指定路径是否是文件
     *
     * @param path 路径
     * @return 是文件返回 true，否则返回 false
     */
    public static boolean isFile(String path) {
        if (isEmpty(path)) return false;
        return new File(path).isFile();
    }

    /**
     * 判断指定路径是否是文件夹
     *
     * @param path 路径
     * @return 是文件夹返回 true，否则返回 false
     */
    public static boolean isDirectory(String path) {
        if (isEmpty(path)) return false;
        return new File(path).isDirectory();
    }

    // ==================== 创建类 ====================

    /**
     * 创建文件（若已存在则不创建）
     * <p>会自动创建所需的父目录。</p>
     *
     * @param filePath 文件的绝对路径
     * @return 创建成功或文件已存在返回 true，失败返回 false
     */
    public static boolean createFile(String filePath) {
        if (isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                return false;
            }
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建文件夹（递归创建多级目录）
     *
     * @param dirPath 文件夹路径
     * @return 创建成功或文件夹已存在返回 true，失败返回 false
     */
    public static boolean createDirectory(String dirPath) {
        if (isEmpty(dirPath)) {
            return false;
        }
        File dir = new File(dirPath);
        if (dir.exists()) {
            return dir.isDirectory();
        }
        return dir.mkdirs();
    }

    // ==================== 删除类 ====================

    /**
     * 删除单个文件
     *
     * @param filePath 文件路径
     * @return 删除成功返回 true，文件不存在或删除失败返回 false
     */
    public static boolean deleteFile(String filePath) {
        if (isEmpty(filePath)) return false;
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 递归删除文件夹及其所有子文件与子目录
     *
     * @param dirPath 文件夹路径
     * @return 删除成功返回 true，失败返回 false
     */
    public static boolean deleteDirectory(String dirPath) {
        if (isEmpty(dirPath)) return false;
        File dir = new File(dirPath);
        return deleteDirectoryInternal(dir);
    }

    /**
     * 递归删除文件夹（内部实现）
     *
     * @param file 文件或文件夹
     * @return 删除成功返回 true
     */
    private static boolean deleteDirectoryInternal(File file) {
        if (file == null || !file.exists()) return true;
        if (file.isFile()) {
            return file.delete();
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                if (!deleteDirectoryInternal(child)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    /**
     * 清空文件夹内容（不删除自身文件夹）
     *
     * @param dirPath 文件夹路径
     * @return 清空成功返回 true，路径无效或非文件夹返回 false
     */
    public static boolean clearDirectory(String dirPath) {
        if (isEmpty(dirPath)) return false;
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }
        File[] children = dir.listFiles();
        if (children == null) return true;
        boolean success = true;
        for (File child : children) {
            if (!deleteDirectoryInternal(child)) {
                success = false;
            }
        }
        return success;
    }

    // ==================== 复制类 ====================

    /**
     * 复制单个文件
     *
     * @param srcPath  源文件路径
     * @param destPath 目标文件路径
     * @return 复制成功返回 true，失败返回 false
     */
    public static boolean copyFile(String srcPath, String destPath) {
        if (isEmpty(srcPath) || isEmpty(destPath)) return false;
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        return copyFile(srcFile, destFile);
    }

    /**
     * 复制单个文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @return 复制成功返回 true，失败返回 false
     */
    public static boolean copyFile(File srcFile, File destFile) {
        if (srcFile == null || !srcFile.exists() || !srcFile.isFile()) return false;
        if (destFile == null) return false;
        // 自动创建目标父目录
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(inChannel, outChannel, fis, fos);
        }
    }

    /**
     * 递归复制文件夹（复制所有内容）
     *
     * @param srcDirPath  源文件夹路径
     * @param destDirPath 目标文件夹路径
     * @return 复制成功返回 true，失败返回 false
     */
    public static boolean copyDirectory(String srcDirPath, String destDirPath) {
        if (isEmpty(srcDirPath) || isEmpty(destDirPath)) return false;
        File srcDir = new File(srcDirPath);
        File destDir = new File(destDirPath);
        if (!srcDir.exists() || !srcDir.isDirectory()) return false;
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File[] children = srcDir.listFiles();
        if (children == null) return true;
        boolean success = true;
        for (File child : children) {
            File dest = new File(destDir, child.getName());
            if (child.isFile()) {
                if (!copyFile(child, dest)) {
                    success = false;
                }
            } else if (child.isDirectory()) {
                if (!copyDirectory(child.getAbsolutePath(), dest.getAbsolutePath())) {
                    success = false;
                }
            }
        }
        return success;
    }

    // ==================== 移动类 ====================

    /**
     * 移动文件或文件夹
     * <p>优先尝试 rename（同一分区更高效），失败则复制后删除源。</p>
     *
     * @param srcPath  源路径
     * @param destPath 目标路径
     * @return 移动成功返回 true，失败返回 false
     */
    public static boolean move(String srcPath, String destPath) {
        if (isEmpty(srcPath) || isEmpty(destPath)) return false;
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        if (!srcFile.exists()) return false;
        // 自动创建目标父目录
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        // 优先尝试 rename
        if (srcFile.renameTo(destFile)) return true;
        // 不同分区则复制后删除
        boolean copied;
        if (srcFile.isFile()) {
            copied = copyFile(srcFile, destFile);
        } else {
            copied = copyDirectory(srcPath, destPath);
        }
        if (copied) {
            if (srcFile.isFile()) {
                return srcFile.delete();
            } else {
                return deleteDirectoryInternal(srcFile);
            }
        }
        return false;
    }

    // ==================== 大小与信息类 ====================

    /**
     * 获取文件大小（字节数）
     *
     * @param filePath 文件路径
     * @return 文件大小（字节），路径无效或文件不存在返回 -1
     */
    public static long getFileSize(String filePath) {
        if (isEmpty(filePath)) return -1;
        File file = new File(filePath);
        return (file.exists() && file.isFile()) ? file.length() : -1;
    }

    /**
     * 获取文件大小并格式化为可读字符串（B / KB / MB / GB）
     *
     * @param filePath 文件路径
     * @return 格式化后的大小字符串，如 "1.50 MB"；路径无效返回 "0 B"
     */
    public static String getFileSizeFormatted(String filePath) {
        long size = getFileSize(filePath);
        return formatSize(size);
    }

    /**
     * 获取文件夹总大小（递归计算所有子文件）
     *
     * @param dirPath 文件夹路径
     * @return 文件夹总大小（字节），路径无效返回 0
     */
    public static long getDirectorySize(String dirPath) {
        if (isEmpty(dirPath)) return 0;
        return getDirectorySizeInternal(new File(dirPath));
    }

    /**
     * 获取文件夹总大小（递归计算所有子文件，内部实现）
     *
     * @param directory 文件夹
     * @return 文件夹总大小（字节）
     */
    private static long getDirectorySizeInternal(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return 0;
        }
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += getDirectorySizeInternal(file);
                }
            }
        }
        return size;
    }

    /**
     * 获取文件夹总大小并格式化为可读字符串
     *
     * @param dirPath 文件夹路径
     * @return 格式化后的大小字符串
     */
    public static String getDirectorySizeFormatted(String dirPath) {
        long size = getDirectorySize(dirPath);
        return formatSize(size);
    }

    /**
     * 将字节大小格式化为可读字符串（B / KB / MB / GB）
     *
     * @param size 字节数
     * @return 格式化后的字符串，如 "2.30 GB"
     */
    public static String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size;
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        return String.format(Locale.getDefault(), "%.2f %s", fileSize, units[unitIndex]);
    }

    // ==================== MD5 ====================

    /**
     * 获取文件的 MD5 值
     *
     * @param filePath 文件路径
     * @return 32 位小写 MD5 字符串，失败返回 null
     */
    public static String getFileMD5(String filePath) {
        if (isEmpty(filePath)) return null;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return null;
        FileInputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            byte[] md5Bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeIO(fis);
        }
    }

    // ==================== 路径与名称类 ====================

    /**
     * 根据路径获取文件后缀名（不含点号）
     *
     * @param filePath 文件路径
     * @return 文件后缀名，如 "txt"；无后缀返回空字符串
     */
    public static String getFileExtension(String filePath) {
        if (isEmpty(filePath)) return "";
        int lastDot = filePath.lastIndexOf('.');
        int lastSep = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        if (lastDot > lastSep && lastDot < filePath.length() - 1) {
            return filePath.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * 根据路径获取文件名（不含后缀）
     *
     * @param filePath 文件路径
     * @return 不含后缀的文件名，路径为空返回空字符串
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (isEmpty(filePath)) return "";
        int lastSep = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        String fileName = (lastSep == -1) ? filePath : filePath.substring(lastSep + 1);
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }

    /**
     * 获取文件所在目录路径
     *
     * @param filePath 文件路径
     * @return 目录路径，路径无效返回空字符串
     */
    public static String getParentPath(String filePath) {
        if (isEmpty(filePath)) return "";
        File file = new File(filePath);
        File parent = file.getParentFile();
        return parent != null ? parent.getAbsolutePath() : "";
    }

    /**
     * 拼接路径（防止重复斜杠）
     *
     * @param basePath 基础路径
     * @param subPath  子路径
     * @return 拼接后的完整路径
     */
    public static String joinPath(String basePath, String subPath) {
        if (isEmpty(basePath)) return subPath != null ? subPath : "";
        if (isEmpty(subPath)) return basePath;
        // 去除 basePath 末尾的分隔符和 subPath 开头的分隔符
        String base = basePath.endsWith(File.separator) || basePath.endsWith("/")
                ? basePath.substring(0, basePath.length() - 1) : basePath;
        String sub = subPath.startsWith(File.separator) || subPath.startsWith("/")
                ? subPath.substring(1) : subPath;
        return base + File.separator + sub;
    }

    // ==================== 读写类 ====================

    /**
     * 读取文本文件内容到字符串
     *
     * @param filePath 文件路径
     * @return 文件内容字符串，文件不存在或读取失败返回 null
     */
    public static String readFileToString(String filePath) {
        if (isEmpty(filePath)) return null;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (!first) {
                    content.append("\n");
                }
                content.append(line);
                first = false;
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeIO(reader);
        }
    }

    /**
     * 将字符串写入文本文件（支持覆盖与追加模式）
     *
     * @param filePath 文件路径
     * @param content  待写入的内容
     * @param append   true 为追加模式，false 为覆盖模式
     * @return 写入成功返回 true，失败返回 false
     */
    public static boolean writeStringToFile(String filePath, String content, boolean append) {
        if (isEmpty(filePath) || content == null) return false;
        // 确保父目录存在
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filePath, append));
            writer.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(writer);
        }
    }

    // ==================== 压缩解压类 ====================

    /**
     * 解压 zip 文件到指定目录
     *
     * @param zipFilePath zip 文件路径
     * @param destDirPath 解压目标目录路径
     * @return 解压成功返回 true，失败返回 false
     */
    public static boolean unzip(String zipFilePath, String destDirPath) {
        if (isEmpty(zipFilePath) || isEmpty(destDirPath)) return false;
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists() || !zipFile.isFile()) return false;
        File destDir = new File(destDirPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        ZipInputStream zis = null;
        FileOutputStream fos = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                File outFile = new File(destDir, entryName);
                // 防止 Zip Slip 漏洞：校验解压路径不超出目标目录
                String canonicalDest = destDir.getCanonicalPath();
                String canonicalOut = outFile.getCanonicalPath();
                if (!canonicalOut.startsWith(canonicalDest + File.separator)
                        && !canonicalOut.equals(canonicalDest)) {
                    return false;
                }
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    fos = new FileOutputStream(outFile);
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    closeIO(fos);
                    fos = null;
                }
                zis.closeEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(fos, zis);
        }
    }
}

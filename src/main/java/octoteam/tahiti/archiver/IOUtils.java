package octoteam.tahiti.archiver;

import java.io.*;
import java.util.zip.ZipInputStream;

class IOUtils {

    private static byte[] buffer = new byte[4096];

    /**
     * 将 input 流所有内容传到 output 流
     * @param input 输入流
     * @param output 输出流
     * @throws IOException
     */
    static void pipe(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(buffer))!= -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    /**
     * 判断一个输入流是否是 zip 文件
     * @param file 文件名
     * @return 是否是 zip
     * @throws IOException
     */
    static boolean isZipFile(File file) throws IOException {
        boolean result = new ZipInputStream(new FileInputStream(file)).getNextEntry() != null;
        return result;
    }

}

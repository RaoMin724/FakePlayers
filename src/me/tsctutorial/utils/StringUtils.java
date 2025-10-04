/*    */
package me.tsctutorial.utils;
/*    */
/*    */

import java.io.File;
/*    */ import java.io.IOException;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import java.nio.file.Files;
/*    */ import java.util.stream.Stream;

///*    */ import net.badbird5907.anticombatlog.relocate.blib.util.CC;
/*    */
/*    */ public final class StringUtils
        /*    */ {
    /*    */
    private StringUtils() {
        /* 13 */
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        /*    */
    }

    public static String format(String in, String... placeholders) {
        /* 15 */
        if (in == null || in.isEmpty())
            /* 16 */ return null;
        /* 17 */
        String a = replacePlaceholders(in, (Object[]) placeholders);
///* 18 */     return CC.translate(a);
        /*    */
        return a;
    }

    public static String replacePlaceholders(String str, Object... replace) {
        if (replace != null && replace.length != 0) {
            int i = 0;
            String finalReturn = str;
            if (replace != null && replace.length != 0) {
                Object[] var4 = replace;
                int var5 = replace.length;

                for (int var6 = 0; var6 < var5; var6++) {
                    Object s = var4[var6];
                    if (s != null) {


                        i++;
                        String toReplace = "%" + i;
                        finalReturn = finalReturn.replace(toReplace, s.toString());
                    }
                }
                return finalReturn;
            }
            return str;
        }

        return str;
    }


    public static String readFile(File file) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8);
            try {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
                stream.close();
            } catch (Throwable throwable) {
                if (stream != null)
                    try {
                        stream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}


/* Location:              D:\Desktop\AntiCombatLog-2.6.0.jar!\net\badbird5907\anticombatlo\\utils\StringUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */
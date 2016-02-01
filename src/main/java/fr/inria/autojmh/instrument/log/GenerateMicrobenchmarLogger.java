package fr.inria.autojmh.instrument.log;

/**
 * A class to printf the code of the microbenchmarkloggers. Is not in generator becuase is for internal use
 *
 * Created by marodrig on 16/09/2015.
 */
@Deprecated
public class GenerateMicrobenchmarLogger {
    static String[] upcased = {
            "Byte",
            "Short",
            "Int",
            "Long",
            "Float",
            "Double",
            "Char",
            "Chars",
            "Boolean"};

    static String[] primitives = {"byte", "short", "int", "long", "float", "double", "char", "String", "boolean"};

    static String[] read = {
            "Byte",
            "Short",
            "Int",
            "Long",
            "Float",
            "Double",
            "Char",
            "UTF",
            "Boolean"};

    public static void main(String[] args) {

        for (int i = 0; i < primitives.length; i++) {
            System.out.println("public static void log" + primitives[i] + "(" + primitives[i] + " data, String name, boolean after) {");
            System.out.println("    getLog().log" + primitives[i] + "(data, name, after);");
            System.out.println("}");
        }

        for (int i = 0; i < primitives.length; i++) {
            System.out.println("public  static void logArray" + primitives[i] + "(" + primitives[i] + "[] data, String name, boolean after) {");
            System.out.println("    getLog().logArray" + primitives[i] + "(data, name, after);");
            System.out.println("}");
        }

        System.out.println("--------------------------------------------------------------");

        for (int i = 0; i < primitives.length; i++) {
            System.out.println("public void log" + primitives[i] + "(" + primitives[i] + " data, String name, boolean after) {");
            System.out.println(
                    "if (after) name = \"after-\" + name; \n" +
                    "if (varRegistered.contains(name)) return;\n" +
                            "            varRegistered.add(name);");
            System.out.println("try {\n" +
                    "            DataOutputStream stream = getStream(name);   " +
                    //"            stream.writeByte(" + PRIMITIVES[i] + "_type);\n " +
                    "            stream.write" + upcased[i] + "(data);\n " +
                    "        } catch (Exception e) {\n" +
                    "            throw new RuntimeException(e);\n" +
                    "        }" +
                    "};");

            System.out.println("public void logArray" + primitives[i] + "(" + primitives[i] + "[] data, String name, boolean after) {");
            System.out.println(
                    "if ( data == null ) data = new " + primitives[i] + "[0]; \n"+
                    "if (after) name = \"after-\" + name; \n" +
                    "if (varRegistered.contains(name)) return;\n" +
                            "            varRegistered.add(name);");
            System.out.println("try {\n" +
                    "            DataOutputStream stream = getStream(name);   " +
                    //"            stream.writeByte(" + PRIMITIVES[i] + "_type_array);\n " +
                    "            stream.writeInt(data.length);\n " +
                    "            for (int i = 0; i < data.length; i++) " +
                    "                stream.write" + upcased[i] + "(data[i]);\n " +
                    "        } catch (Exception e) {\n" +
                    "            throw new RuntimeException(e);\n" +
                    "        }" +
                    "};");



        }
        System.out.println("--------------------------------------------------------------");
        System.out.println("--------------------------------------------------------------");
        System.out.println("--------------------------------------------------------------");

        for (int i = 0; i < primitives.length; i++) {
            System.out.println("public static " + primitives[i] + "[] readArray" + primitives[i] + "(DataInputStream s) {");
            System.out.println("try {\n" +
                            "             int length = s.readInt(); \n " +
                            "           " + primitives[i] + "[] result = new "+primitives[i] + "[length];\n"+
                    "            for (int k = 0; k < length; k++) result[k] = s.METHODS_NAME" + read[i] + "();\n"+
                            "        return result;\n" +
                            "        } catch (Exception e) {\n" +
                            "            throw new RuntimeException(e);\n" +
                            "        }" +
                            "}");

            System.out.println("public static " + primitives[i] + " METHODS_NAME" + primitives[i] + "(DataInputStream s) {");
            System.out.println("try {\n" +
                            "        return s.METHODS_NAME" + read[i] + "();\n"+
                            "        } catch (Exception e) {\n" +
                            "            throw new RuntimeException(e);\n" +
                            "        }" +
                            "}");
        }
    }

}
